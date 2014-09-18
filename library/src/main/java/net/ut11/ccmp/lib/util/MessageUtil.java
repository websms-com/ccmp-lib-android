package net.ut11.ccmp.lib.util;

import android.content.Intent;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.AttachmentsDb;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.db.MessagesDb;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;

import java.util.List;

public class MessageUtil {

	public static final String INTENT_MESSAGE_INSERTED = "net.ut11.ccmp.lib.MESSAGE_INSERTED";
	public static final String INTENT_MESSAGE_UPDATED = "net.ut11.ccmp.lib.MESSAGE_UPDATED";
	public static final String INTENT_EXTRA_MESSAGE_ID = "msgId";
	public static final String INTENT_EXTRA_MESSAGE_READ = "read";

	public static Message getMessageFrom(DeviceInboxResponse resp) {
		if (resp == null) {
			return null;
		}

		Message msg = new Message();
		msg.setMessageId(resp.getId());
		msg.setAccountId(resp.getAccountId());
		msg.setAttachmentId(resp.getAttachmentId());
		msg.setMessage(resp.getContent());
		msg.setAddress(resp.getSender());
		msg.setDateSent(resp.getCreatedOn());
		msg.setIncoming(true);
		msg.setRead(false);
		msg.setIsSms(false);
		msg.setPushParameter(resp.getAdditionalPushParameter());
        msg.setExpired(resp.getExpired());

		return msg;
	}

	public static boolean insertMessage(Message msg) {
		if (msg == null) {
			return false;
		}

		MessageHandler mh = LibApp.getMessageHandler();

		// check incoming messages for duplicates
		if (msg.isIncoming()) {
			Message dbMsg = MessagesDb.getMessage(msg.getDateSent(), msg.getAddress(), msg.getMessage());
			if (dbMsg != null && (dbMsg.getMessageId() == 0 || msg.getMessageId() == 0 || msg.getMessageId() == dbMsg.getMessageId())) {
				if (dbMsg.getMessageId() == 0 && msg.getMessageId() > 0) {
					dbMsg.setMessageId(msg.getMessageId());

					MessagesDb.saveMessage(dbMsg);
					broadcastMessageUpdated(dbMsg.getId(), dbMsg.isRead());
				}

				return false;
			}

			mh.handleIncomingMessage(msg);
		}

		MessagesDb.saveMessage(msg);

		if (msg.isIncoming()) {
			mh.handleIncomingMessageInserted(msg);
		}

		broadcastMessageInserted(msg.getId(), msg.isRead());

		return true;
	}

	public static void updateMessage(Message msg) {
		if (msg == null) {
			return;
		}

		MessagesDb.saveMessage(msg);
		broadcastMessageUpdated(msg.getId(), msg.isRead());
	}

	/**
	 * retrieves the specified message
	 * @param id the message id
	 * @return the message
	 */
	public static Message getMessage(long id) {
		return MessagesDb.getMessage(id);
	}

	/**
	 * gives all stored messages
	 * @return messages
	 */
	public static List<Message> getMessages() {
		return MessagesDb.getMessages();
	}

	/**
	 * gives all stored unread messages
	 * @return messages
	 */
	public static List<Message> getUnreadMessages() {
		return MessagesDb.getUnreadMessages();
	}

	/**
	 * gets all outgoing messages
	 * @return messages
	 */
	public static List<Message> getOutgoingMessages() {
		return MessagesDb.getOutgoingMessages();
	}

	/**
	 * gives all stored responses for a given messages
	 * @param id message id
	 * @return response messages
	 */
	public static List<Message> getResponseMessages(long id) {
		return MessagesDb.getResponseMessages(id);
	}

	/**
	 * marks a message as read
	 * @param msg the message to be marked as read
	 */
	public static void readMessage(Message msg) {
		if (msg == null || msg.isRead()) {
			return;
		}

		msg.setRead(true);
		MessagesDb.saveMessage(msg);

		broadcastMessageUpdated(msg.getId(), true);
	}

	/**
	 * deletes a message
	 * @param msg the message to be deleted
	 */
	public static void deleteMessage(Message msg) {
		if (msg == null) {
			return;
		}

		deleteResponseMessages(msg.getId());
		if (msg.getAttachmentId() > 0) {
			AttachmentsDb.deleteAttachment(msg.getAttachmentId());
			AttachmentCache cache = AttachmentCache.getInstance(LibApp.getContext());

			if (cache != null) {
				cache.deleteAttachmentFromCache(msg.getAttachmentId());
			}
		}
		MessagesDb.deleteMessage(msg);

		broadcastMessageUpdated(msg.getId(), true);
	}

	private static void deleteResponseMessages(long msgId) {
		for (Message msg : MessagesDb.getResponseMessages(msgId)) {
			deleteResponseMessages(msg.getId());
			MessagesDb.deleteMessage(msg);
		}
	}

	/**
	 * sends and stores a message.
	 * @param recipient the recipient
	 * @param message the message
	 * @param asSms send message as sms
	 * @param msgToRespond an existing message to be referenced as response
	 * @return the id of the stored outgoing message or -1 if sending failed
	 */
	public static Message sendMessage(String recipient, String message, boolean asSms, Message msgToRespond) {
		return sendMessage(recipient, message, asSms, null, msgToRespond);
	}

	/**
	 * sends and stores a message.
	 * @param recipient the recipient
	 * @param message the message
	 * @param asSms send message as sms
	 * @param attachmentId id of attachment (if any)
	 * @param msgToRespond an existing message to be referenced as response
	 * @return the id of the stored outgoing message or -1 if sending failed
	 */
	public static Message sendMessage(String recipient, String message, boolean asSms, Integer attachmentId, Message msgToRespond) {
		boolean success = false;

		if (attachmentId != null && asSms) {
			throw new IllegalArgumentException("sms cannot be sent with attachment!");
		}

		if (asSms) {
			success = SmsUtil.sendSms(recipient, message);
		} else {
			try {
				DeviceEndpoint.sendMessage(recipient, message, attachmentId);
				success = true;
			} catch (ApiException e) {
				Logger.warn("failed to send ip message: " + e.getResponseCode());
			}
		}

		if (success) {
			Message msg = new Message();
			msg.setIncoming(false);
			msg.setIsSms(asSms);
			msg.setDateSent(System.currentTimeMillis());
			msg.setRead(true);
			msg.setAddress(recipient);
			msg.setMessage(message);
			msg.setResponseForId(msgToRespond == null ? 0 : msgToRespond.getId());

			MessagesDb.saveMessage(msg);
			broadcastMessageInserted(msg.getId(), true);

			return msg;
		}

		return null;
	}

	private static void broadcastMessageInserted(long msgId, boolean read) {
		Intent i = new Intent(INTENT_MESSAGE_INSERTED);
		i.putExtra(INTENT_EXTRA_MESSAGE_ID, msgId);
		i.putExtra(INTENT_EXTRA_MESSAGE_READ, read);
		LibApp.getContext().sendBroadcast(i);
	}

	private static void broadcastMessageUpdated(long msgId, boolean read) {
		Intent i = new Intent(INTENT_MESSAGE_UPDATED);
		i.putExtra(INTENT_EXTRA_MESSAGE_ID, msgId);
		i.putExtra(INTENT_EXTRA_MESSAGE_READ, read);
		LibApp.getContext().sendBroadcast(i);
	}
}
