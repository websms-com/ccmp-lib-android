package net.ut11.ccmp.lib.util;

import net.ut11.ccmp.lib.db.Message;

public class MessageHandler {

	public void handleIncomingMessage(Message message) {
		// to be overridden
	}

	public void handleIncomingMessageInserted(Message message) {
		// to be overridden
	}
}
