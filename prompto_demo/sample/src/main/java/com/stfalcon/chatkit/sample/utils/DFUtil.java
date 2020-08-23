package com.stfalcon.chatkit.sample.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import com.stfalcon.chatkit.sample.features.demo.styled.StyledMessagesActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIContext;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class DFUtil extends StyledMessagesActivity implements RelaxInterface, AlarmInterface, TextToSpeech.OnInitListener {
	private static final String API_KEY = "8c253081ae5e4dfe88c2074b8ff51d4c";
	private static final String TAG = "[DFUtil]";
	private AIDataService aiDataService;
	private ResponseInterface responseInterface;
	private TextToSpeech tts;
	private Context appContext;
	private String response;


	public DFUtil(Context context, ResponseInterface responseInterface) {

		AIConfiguration aiConfiguration = new AIConfiguration(API_KEY, AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
		this.responseInterface = responseInterface;
		aiDataService = new AIDataService(context, aiConfiguration);

		this.appContext = context;
		tts = new TextToSpeech(this.appContext, this, "com.google.android.tts");
	}

	public void SendMessage(String message) {
		Log.i(TAG, "SendMessage: " + message);
		if (message == null || message.length() < 1) {
			return;
		}

		final AIRequest aiRequest = new AIRequest();
		List<AIContext> aiContexts = new ArrayList<>();
		aiRequest.setQuery(message);
		aiRequest.setContexts(aiContexts);
		new AsyncTask<AIRequest, Void, AIResponse>() {
			@Override
			protected AIResponse doInBackground(AIRequest... aiRequests) {
				AIResponse aiResponse = null;
				try {
					aiResponse = aiDataService.request(aiRequest);
				} catch (AIServiceException e) {
					Log.e(TAG, "doInBackground: ", e);
				}
				return aiResponse;
			}

			@Override
			protected void onPostExecute(AIResponse aiResponse) {
				if (aiResponse != null) {
					response = aiResponse.getResult().getFulfillment().getSpeech();
					responseInterface.OnMessage(response);
					Log.i(TAG, "onPostExecute: " + response);
					speakOut(response);
				}
			}
		}.execute(aiRequest);
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			Set<String> voice=new HashSet<>();
			voice.add("male");//here you can give male if you want to select male voice.
			//Voice v=new Voice("en-us-x-sfg#female_2-local",new Locale("en","US"),400,200,true,a);
			Voice v=new Voice("en-us-x-sfg#male_3-local",new Locale("en","UK"),400,200,true,voice);
			tts.setVoice(v);
			tts.setSpeechRate(0.9f);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
	}

	@Override
	public void onRelax() {
		Log.i(TAG, "onRelax: Called relaxed method");
		final AIRequest aiRequest = new AIRequest();
		AIEvent aiEvent = new AIEvent();
		aiEvent.setName("calm_event");
		aiRequest.setEvent(aiEvent);
		new AsyncTask<AIRequest, Void, AIResponse>() {

			@Override
			protected AIResponse doInBackground(AIRequest... aiRequests) {
				try {
					AIResponse aiResponse = aiDataService.request(aiRequest);
					return aiResponse;
				} catch (AIServiceException e) {
					Log.e(TAG, "Event request: ", e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(AIResponse aiResponse) {
				super.onPostExecute(aiResponse);
				if (aiResponse != null) {
					response = aiResponse.getResult().getFulfillment().getSpeech();
					responseInterface.OnMessage(response);
					playNotificationSound();
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					speakOut(response);
				}
			}
		}.execute(aiRequest);
	}

	public void playNotificationSound()
	{
		try
		{

			Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + appContext.getPackageName() + "/raw/notification");
			Ringtone r = RingtoneManager.getRingtone(appContext, alarmSound);
			r.play();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onAlarm() {
		Log.i(TAG, "onAlarm: Called alarm method");
		final AIRequest aiRequest = new AIRequest();
		AIEvent aiEvent = new AIEvent();
		aiEvent.setName("time_event");
		aiRequest.setEvent(aiEvent);
		new AsyncTask<AIRequest, Void, AIResponse>() {

			@Override
			protected AIResponse doInBackground(AIRequest... aiRequests) {
				try {
					AIResponse aiResponse = aiDataService.request(aiRequest);
					return aiResponse;
				} catch (AIServiceException e) {
					Log.e(TAG, "Event request: ", e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(AIResponse aiResponse) {
				super.onPostExecute(aiResponse);
				if (aiResponse != null) {
					response = aiResponse.getResult().getFulfillment().getSpeech();
					responseInterface.OnMessage(response);
					playNotificationSound();
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					speakOut(response);
				}
			}
		}.execute(aiRequest);
	}
}
