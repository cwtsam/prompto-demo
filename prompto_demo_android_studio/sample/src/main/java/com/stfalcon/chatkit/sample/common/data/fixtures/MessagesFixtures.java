package com.stfalcon.chatkit.sample.common.data.fixtures;

import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by troy379 on 12.12.16.
 */
public final class MessagesFixtures extends FixturesData {
	private MessagesFixtures() {
		throw new AssertionError();
	}

	public static Message getVoiceMessage() {
		Message message = new Message(getRandomId(), getUser(), null);
		message.setVoice(new Message.Voice("http://example.com", rnd.nextInt(200) + 30));
		return message;
	}

	private static User getUser() {
		return new User("1", names.get(0), avatars.get(0), true);
	}

	public static ArrayList<Message> getMessages(Date startDate) {
		ArrayList<Message> messages = new ArrayList<>();
		for (int i = 0; i < 1/*days count*/; i++) {
			int countPerDay = 1;

			for (int j = 0; j < countPerDay; j++) {
				Message message;
				if (i % 2 == 0 && j % 3 == 0) {
					message = getImageMessage();
				} else {
					message = getTextMessage();
				}

				Calendar calendar = Calendar.getInstance();
				if (startDate != null) calendar.setTime(startDate);
				calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

				message.setCreatedAt(calendar.getTime());
				messages.add(message);
			}
		}
		return messages;
	}

	public static Message getImageMessage() {
		Message message = new Message(getRandomId(), getUser(), null);
		//message.setImage(new Message.Image(getRandomImage()));
		return message;
	}

	public static Message getTextMessage() {
		return getTextMessage(getRandomMessage());
	}

	public static Message getTextMessage(String text) {
		return new Message(getRandomId(), getUser(), text);
	}

	public static Message getUserTextMessage(String text) {

		return new Message(getRandomId(), new User("0", names.get(0), avatars.get(0), true), text);
	}


	public static Message getTextMessage(String text,int user) {
		return new Message(getRandomId(), new User(String.valueOf(user),names.get(0),avatars.get(0),true), text);
	}
}
