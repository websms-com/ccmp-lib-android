package net.ut11.ccmp.lib.db;

import android.test.AndroidTestCase;

import java.util.List;

public class MessageTest extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MessagesDb.clear();
	}

	public void testInsert() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setIncoming(false);

		handleMessage(msg);
	}

	public void testInsertIncomingSms() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setIncoming(true);
		msg.setRead(true);
		msg.setIsSms(true);

		handleMessage(msg);
	}

	public void testInsertStatus() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setIncoming(false);

		handleMessage(msg);
	}

	public void testInsertMultiple() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setIncoming(false);

		handleMessage(msg);

		msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender2");
		msg.setMessage("Testmessage2");
		msg.setIncoming(false);

		handleMessage(msg);

		List<Message> msgs = MessagesDb.getMessages();
		assertEquals(2, msgs.size());

		assertEquals("Testsender2", msgs.get(0).getAddress());
		assertEquals("Testsender", msgs.get(1).getAddress());
	}

	public void testUpdate() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");
		msg.setIncoming(false);

		handleMessage(msg);
		long id = msg.getId();

		msg.setIncoming(false);
		handleMessage(msg);

		assertEquals(id, msg.getId());
	}

	public void testFindDuplicate() {
		Message msg = new Message();
		msg.setDateSent(System.currentTimeMillis());
		msg.setIncoming(true);
		msg.setAddress("Testsender");
		msg.setMessage("Testmessage");

		handleMessage(msg);

		Message dbMsg = MessagesDb.getMessage(System.currentTimeMillis(), msg.getAddress(), msg.getMessage());
		assertNotNull(dbMsg);

		assertEquals(msg.getId(), dbMsg.getId());
	}

	private void handleMessage(Message msg) {
		MessagesDb.saveMessage(msg);
		assertTrue(msg.getId() > 0);

		Message dbMsg = MessagesDb.getMessage(msg.getId());
		assertEquals(msg.getId(), dbMsg.getId());
		assertEquals(msg.getMessageId(), dbMsg.getMessageId());
		assertEquals(msg.getMessage(), dbMsg.getMessage());
		assertEquals(msg.getAddress(), dbMsg.getAddress());
		assertEquals(msg.getDateSent(), dbMsg.getDateSent());
		assertEquals(msg.isIncoming(), dbMsg.isIncoming());
		assertEquals(msg.isRead(), dbMsg.isRead());
		assertEquals(msg.isSms(), dbMsg.isSms());
	}
}
