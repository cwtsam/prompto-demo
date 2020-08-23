package com.stfalcon.chatkit.sample.features.demo.styled;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures;
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity;
import com.stfalcon.chatkit.sample.features.main.EmpaticaAdapter;
import com.stfalcon.chatkit.sample.features.main.EmpaticaData;
import com.stfalcon.chatkit.sample.features.main.org.ahlab.FileWriter;
import com.stfalcon.chatkit.sample.utils.AppUtils;
import com.stfalcon.chatkit.sample.utils.DFUtil;
import com.stfalcon.chatkit.sample.utils.ResponseInterface;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class StyledMessagesActivity extends DemoMessagesActivity
		implements MessageInput.InputListener,
		MessageInput.AttachmentsListener,
		DateFormatter.Formatter, ResponseInterface {
	private static final String TAG = "[StyledMessagesActivity]";
	private final int REQ_CODE_SPEECH_INPUT = 100;
	private boolean isOnTop;
	private DFUtil dfUtil;
	private MessagesList messagesList;
	private int count = 0;
	private boolean isStartRequest;
	private String toWrite;
	private AppUtils appUtils;
	private boolean setAlarm = false;
	private String text; // for toast messages
	private boolean isAudio = false;

	public static void open(Context context) {
		context.startActivity(new Intent(context, StyledMessagesActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_styled_messages);
		dfUtil = new DFUtil(this, this);
		EmpaticaData.getInstance().setRelaxInterface(dfUtil);
		messagesList = findViewById(R.id.messagesList);
		initAdapter();

		MessageInput input = findViewById(R.id.input);
		input.setInputListener(this);
		input.setAttachmentsListener(this);

		initListeners();

		createNotificationChannel();

		FileWriter fileWriter = FileWriter.getInstance();
		fileWriter.initSession("ConversationLog","24",2,1); /// change folder and info. text stuff here
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "prompto";
			String description = "Reminder notifications of prompto";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("PROMPTO01", name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}

		}
	}


	private void initListeners() {
		FloatingActionButton fabTalk = findViewById(R.id.fab_listen);

 		fabTalk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listen();
			}
		});

	}

	/*

	private void AlarmListenerAlreadySet(){
		String msg = "Alarms have already been set!";
		appUtils.showToast(getApplicationContext(), msg, true);
	}

	private void generateAlarm(){

		setAlarm = true; // flag to mark that alarm has been set
		// generate a random time interval
		// display with a popup for the set alarm

		final int min1 = 40;
		final int min2 = 300;
		final int max1 = 70;
		final int max2 = 330;
		final int randomTimeDelay1 = new Random().nextInt((max1 - min1) + 1) + min1; // for generating a random number within a range
		final int randomTimeDelay2 = new Random().nextInt((max2 - min2) + 1) + min2; // for generating a random number within a range

		if ((((randomTimeDelay1/60)%60) == 0) | (((randomTimeDelay2/60)%60) == 0)){
			text = "Reminder alarms set for " + randomTimeDelay1 + " and " + randomTimeDelay2 + " seconds";
		}
		else {
			text = "Reminder alarms set for " + ((randomTimeDelay1/60)%60) + " minute and " + (randomTimeDelay1%60) + " seconds" + ", and " + ((randomTimeDelay2/60)%60) + " minute and " + (randomTimeDelay2%60) + " seconds";
		}


		appUtils.showToast(getApplicationContext(), text, true);

		// add a scheduled task to run after a certain time interval
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				dfUtil.onAlarm();
			}
		}, randomTimeDelay1 * 1000); // add delay in miliseconds and change later to randomTimeDelay

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				dfUtil.onAlarm();
				setAlarm = false;
			}
		}, randomTimeDelay2 * 1000); // add delay in miliseconds and change later to randomTimeDelay

	}
	*/


	private void listen() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void initAdapter() {
		super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
		super.messagesAdapter.enableSelectionMode(this);
		super.messagesAdapter.setLoadMoreListener(this);
		super.messagesAdapter.setDateHeadersFormatter(this);
		messagesList.setAdapter(super.messagesAdapter);
	}


	@Override
	public boolean onSubmit(CharSequence input) {
		messagesAdapter.addToStart(
				MessagesFixtures.getTextMessage(input.toString(), 0), true);

		dfUtil.SendMessage(input.toString());

		toWrite = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "," + "user" + "," + input.toString();
		FileWriter.getInstance().appendMessageHistory(toWrite); //this helps to record message sent
		System.out.println(toWrite);
		///
		return true;


	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQ_CODE_SPEECH_INPUT: {
				if (resultCode == RESULT_OK && null != data) {

					ArrayList<String> result = data
							.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					String recognized = result.get(0);

					messagesAdapter.addToStart(MessagesFixtures.getUserTextMessage(recognized), true);
					dfUtil.SendMessage(recognized);
				}
				break;
			}

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isOnTop = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isOnTop = true;
	}

	@Override
	public void onAddAttachments() {
		//messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
	}

	@Override
	public String format(Date date) {
		if (DateFormatter.isToday(date)) {
			return getString(R.string.date_header_today);
		} else if (DateFormatter.isYesterday(date)) {
			return getString(R.string.date_header_yesterday);
		} else {
			return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
		}
	}

	@Override
	public void OnMessage(String message) {
		messagesAdapter.addToStart(MessagesFixtures.getTextMessage(message), true);

		//
		if (!isOnTop) { // if app is not open on screen
			sendNotification(message); // show the message in a notification
			if (message.toLowerCase().contains("would you like to start practice")) {
				isStartRequest = true;
			}
		}

		toWrite = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "," + "prompto" + "," + message;
		FileWriter.getInstance().appendMessageHistory(toWrite); // logs conversation
		System.out.println(toWrite);

	}

	private void sendNotification(String message) {
		Intent mainIntent = new Intent(this, StyledMessagesActivity.class);
		PendingIntent pendingMain = PendingIntent.getActivity(this, 1, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent dummyIntent = PendingIntent.readPendingIntentOrNullFromParcel(Parcel.obtain());
		Log.i(TAG, "sendNotification: " + message);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "PROMPTO01")
				.setContentTitle("Prompto")
				.setContentText(message)
				.setPriority(Notification.PRIORITY_MAX)
				.setAutoCancel(false)
				.setContentIntent(pendingMain)
				.setDefaults(Notification.DEFAULT_ALL)
				.setSmallIcon(R.drawable.ic_message_black_24dp);

		if (isStartRequest) {
			builder.addAction(R.drawable.ic_play_for_work_black_24dp, "Start Now", dummyIntent)
					.addAction(R.drawable.ic_do_not_disturb_alt_black_24dp, "Maybe Later", dummyIntent)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
			isStartRequest = false;
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (notificationManager != null) {
			notificationManager.notify(++count, builder.build());
		}

	}
}
