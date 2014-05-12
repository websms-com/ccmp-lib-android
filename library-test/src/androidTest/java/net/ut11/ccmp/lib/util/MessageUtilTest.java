package net.ut11.ccmp.lib.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AndroidTestCase;

import net.ut11.ccmp.api.domain.DeviceInboxResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Message;
import net.ut11.ccmp.lib.db.MessagesDb;

public class MessageUtilTest extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MessagesDb.clear();
	}

	public void testFromDeviceInboxResponse() {
		DeviceInboxResponse resp = new DeviceInboxResponse();
		resp.setId(4711);
		resp.setContent("Testmessage");
		resp.setSender("Testsender");
		resp.setCreatedOn(System.currentTimeMillis());

		Message msg = MessageUtil.getMessageFrom(null);
		assertNull(msg);

		msg = MessageUtil.getMessageFrom(resp);
		assertEquals(resp.getId(), msg.getMessageId());
		assertEquals(resp.getContent(), msg.getMessage());
		assertEquals(resp.getSender(), msg.getAddress());
		assertEquals(resp.getCreatedOn(), msg.getDateSent());
		assertTrue(msg.isIncoming());
		assertFalse(msg.isRead());
		assertFalse(msg.isSms());
	}

	public void testInsertAndRead() {
		Message msg = new Message();
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setRead(false);
		msg.setDateSent(System.currentTimeMillis());
		assertEquals(msg.getId(), 0);

		MessageUtil.insertMessage(msg);
		assertTrue(msg.getId() > 0);

		msg = MessagesDb.getMessage(msg.getId());
		assertTrue(msg.getId() > 0);
		assertFalse(msg.isRead());

		MessageUtil.readMessage(msg);
		assertTrue(msg.isRead());

		msg = MessagesDb.getMessage(msg.getId());
		assertTrue(msg.isRead());
	}

	public void testInsertDuplicate() {
		Message msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setRead(false);
		msg.setDateSent(System.currentTimeMillis());

		assertTrue(MessageUtil.insertMessage(msg));
		msg.setDateSent(msg.getDateSent() + 10000);
		assertFalse(MessageUtil.insertMessage(msg));
	}

	public void testInsertApiDuplicate() {
		Message msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setRead(false);
		msg.setMessageId(120);
		msg.setDateSent(System.currentTimeMillis());

		assertTrue(MessageUtil.insertMessage(msg));
		msg.setDateSent(msg.getDateSent() + 10000);
		assertFalse(MessageUtil.insertMessage(msg));
	}

	public void testInsertApiNoDuplicate() {
		Message msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setRead(false);
		msg.setMessageId(120);
		msg.setDateSent(System.currentTimeMillis());

		assertTrue(MessageUtil.insertMessage(msg));

		msg.setDateSent(msg.getDateSent() + 10000);
		msg.setMessageId(121);

		assertTrue(MessageUtil.insertMessage(msg));
	}

	public void testGetResponses() {
		Message msg = new Message();
		msg.setAddress("Testsender1");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		assertTrue(MessageUtil.insertMessage(msg));

		msg = new Message();
		msg.setAddress("Testsender2");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		assertTrue(MessageUtil.insertMessage(msg));

		long msgId = msg.getId();

		msg = new Message();
		msg.setAddress("Testsender3");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		msg.setResponseForId(msgId);
		assertTrue(MessageUtil.insertMessage(msg));

		assertEquals(3, MessageUtil.getMessages().size());
		assertEquals(1, MessageUtil.getResponseMessages(msgId).size());
		assertEquals(0, MessageUtil.getResponseMessages(4711).size());
	}

	public void testGetNewestOutgoing() {
		Message msg = new Message();
		msg.setIncoming(false);
		msg.setAddress("Testsender1");
		msg.setMessage("Testmessage1");
		msg.setDateSent(System.currentTimeMillis());
		assertTrue(MessageUtil.insertMessage(msg));

		assertEquals("Testmessage1", MessageUtil.getOutgoingMessages().get(0).getMessage());

		msg = new Message();
		msg.setIncoming(false);
		msg.setAddress("Testsender2");
		msg.setMessage("Testmessage2");
		msg.setDateSent(System.currentTimeMillis());
		assertTrue(MessageUtil.insertMessage(msg));

		assertEquals("Testmessage2", MessageUtil.getOutgoingMessages().get(0).getMessage());

		msg = new Message();
		msg.setIncoming(false);
		msg.setAddress("Testsender3");
		msg.setMessage("Testmessage3");
		msg.setDateSent(System.currentTimeMillis() - 60000);
		assertTrue(MessageUtil.insertMessage(msg));

		assertEquals("Testmessage2", MessageUtil.getOutgoingMessages().get(0).getMessage());
	}

	public void testDelete() {
		Message msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());

		assertTrue(MessageUtil.insertMessage(msg));
		assertNotNull(MessageUtil.getMessage(msg.getId()));

		MessageUtil.deleteMessage(msg);
		assertNull(MessageUtil.getMessage(msg.getId()));
	}

	public void testDeleteResponse() {
		Message msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Alarm");
		msg.setDateSent(System.currentTimeMillis());
		assertTrue(MessageUtil.insertMessage(msg));
		long id1 = msg.getId();

		msg = new Message();
		msg.setIncoming(false);
		msg.setAddress("Testsender");
		msg.setMessage("JA");
		msg.setDateSent(System.currentTimeMillis());
		msg.setResponseForId(id1);
		assertTrue(MessageUtil.insertMessage(msg));
		long id2 = msg.getId();

		msg = new Message();
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Alarm inaktiv");
		msg.setDateSent(System.currentTimeMillis());
		msg.setResponseForId(id2);
		assertTrue(MessageUtil.insertMessage(msg));
		long id3 = msg.getId();

		assertNotNull(MessageUtil.getMessage(id1));
		assertNotNull(MessageUtil.getMessage(id2));
		assertNotNull(MessageUtil.getMessage(id3));

		MessageUtil.deleteMessage(MessageUtil.getMessage(id1));

		assertNull(MessageUtil.getMessage(id1));
		assertNull(MessageUtil.getMessage(id2));
		assertNull(MessageUtil.getMessage(id3));
	}

	public void testSendInsertBroadcast() {
		final Object lockObj = new Object();
		final BroadcastReceiverThread t = new BroadcastReceiverThread(lockObj);
		t.start();

		BroadcastReceiver r = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				t.onReceive(intent);
			}
		};
		LibApp.getContext().registerReceiver(r, new IntentFilter(MessageUtil.INTENT_MESSAGE_INSERTED));

		Message msg = new Message();
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		MessageUtil.insertMessage(msg);

		synchronized (lockObj) {
			// just wait for lock
		}

		LibApp.getContext().unregisterReceiver(r);

		assertTrue(t.hasReceived());
		assertFalse(t.isRead());
	}

	public void testSendReadBroadcast1() {
		Message msg = new Message();
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		msg.setRead(false);
		MessageUtil.insertMessage(msg);

		final Object lockObj = new Object();
		final BroadcastReceiverThread t = new BroadcastReceiverThread(lockObj);
		t.start();

		BroadcastReceiver r = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				t.onReceive(intent);
			}
		};
		LibApp.getContext().registerReceiver(r, new IntentFilter(MessageUtil.INTENT_MESSAGE_UPDATED));

		MessageUtil.readMessage(msg);

		synchronized (lockObj) {
			// just wait for lock
		}

		LibApp.getContext().unregisterReceiver(r);

		assertTrue(t.hasReceived());
		assertTrue(t.isRead());
	}

	public void testSendReadBroadcast2() {
		Message msg = new Message();
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setDateSent(System.currentTimeMillis());
		msg.setRead(true);
		MessageUtil.insertMessage(msg);

		final Object lockObj = new Object();
		final BroadcastReceiverThread t = new BroadcastReceiverThread(lockObj);
		t.start();

		BroadcastReceiver r = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				t.onReceive(intent);
			}
		};
		LibApp.getContext().registerReceiver(r, new IntentFilter(MessageUtil.INTENT_MESSAGE_UPDATED));

		MessageUtil.readMessage(msg);

		synchronized (lockObj) {
			// just wait for lock
		}

		LibApp.getContext().unregisterReceiver(r);

		assertFalse(t.hasReceived());
		assertNull(t.isRead());
	}

	private class BroadcastReceiverThread extends Thread {

		private Object lockObj;
		private boolean received = false;
		private Boolean read = null;

		public BroadcastReceiverThread(Object lockObj) {
			this.lockObj = lockObj;
		}

		public void onReceive(Intent intent) {
			received = true;
			if (intent.hasExtra(MessageUtil.INTENT_EXTRA_MESSAGE_READ)) {
				read = intent.getBooleanExtra(MessageUtil.INTENT_EXTRA_MESSAGE_READ, false);
			}

			interrupt();
		}

		public boolean hasReceived() {
			return received;
		}

		public Boolean isRead() {
			return read;
		}

		@Override
		public void run() {
			synchronized (lockObj) {
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
