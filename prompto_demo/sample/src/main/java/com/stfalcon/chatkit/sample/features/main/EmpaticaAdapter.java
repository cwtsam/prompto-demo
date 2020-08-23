package com.stfalcon.chatkit.sample.features.main;

import android.content.Context;
import android.util.Log;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.stfalcon.chatkit.sample.utils.RelaxInterface;
import com.stfalcon.chatkit.sample.features.main.org.ahlab.FileWriter;
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity;
import com.stfalcon.chatkit.sample.utils.AppUtils;
import android.os.Handler;

import java.util.ArrayList;

public class EmpaticaAdapter extends DemoMessagesActivity implements EmpaDataDelegate, EmpaStatusDelegate  {
	private AppUtils appUtils;
	private static final String TAG = "[EmpaticaAdapter]";
	private static final String EMPATICA_API_KEY = "62e322cb9dac410e9041afc08d977669"; //enter your Empatica API key here. Has to be from the same account that you purchased the E4 from
	private static EmpaticaAdapter instance;

	private Runnable displayEmpaticaConnected;
	private Runnable displayEmpaticaDisconnected;
	private Handler messageHandler = new Handler();

	EMPStatus empStatus;

	private EmpaDeviceManager deviceManager = null;

	private EmpaticaAdapter(final Context context, EMPStatus listner) {
		empStatus = listner;
		try {
			initEmpaticaDeviceManager(context);
		} catch (Exception e) {
			Log.e(TAG, "EmpaticaAdapter: Error while authenticating", e);
		}

		this.displayEmpaticaConnected = new Runnable() {
			public void run() {
				appUtils.showToast(context, "Empatica Connected!", true);
			}
		};

		this.displayEmpaticaDisconnected = new Runnable() {
			public void run() {
				appUtils.showToast(context, "Empatica Disconnected!", true);
			}
		};
	}

	private void initEmpaticaDeviceManager(Context context) {
		deviceManager = new EmpaDeviceManager(context, this, this);
		deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
		Log.i(TAG, "initEmpaticaDeviceManager: successful");
	}


	public static EmpaticaAdapter getInstance() {
		return instance;
	}

	public static EmpaticaAdapter getInstance(Context context, EMPStatus listner) {

		if (instance == null) {
			try {
				instance = new EmpaticaAdapter(context, listner);
			} catch (Exception e) {
				Log.e(TAG, "getInstance: ", e);
			}
		}
		return instance;
	}

	public void accessGranted(Context context) {
		try {
			if (deviceManager == null) {
				initEmpaticaDeviceManager(context);
			}

		} catch (Exception e) {
			Log.e(TAG, "accessGranted: ", e);
		}
	}

	@Override
	public void didReceiveGSR(float gsr, double timestamp) {
		//Log.i(TAG, "didReceiveGSR: [timestamp: " + timestamp + ", value: " + gsr + "]");
		EmpaticaData.getInstance().pushEDA(gsr, timestamp);
	}

	@Override
	public void didReceiveBVP(float bvp, double timestamp) {
		//Log.i(TAG, "didReceiveBVP: [timestamp: " + timestamp + ", value: " + bvp + "]");
		//good to get from BVP then convert to IBI post-hoc (for analysis), as sampling rate is 64Hz, IBI is only 1/64 Hz
		EmpaticaData.getInstance().pushBVP(bvp, timestamp);
	}

	@Override
	public void didReceiveIBI(float ibi, double timestamp) {
	//	Log.i(TAG, "didReceiveIBI: [timestamp: " + timestamp + ", value: " + ibi + "]");
		EmpaticaData.getInstance().pushIBI(ibi, timestamp);
	}

	@Override
	public void didReceiveTemperature(float t, double timestamp) {
		//Log.i(TAG, "didReceiveTemperature: [timestamp: " + timestamp + ", value: " + t + "]");
	}

	@Override
	public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
		//Log.i(TAG, "didReceiveAcceleration: [timestamp: " + timestamp + ", value: " + x + "," + y + "," + z + "]");
		EmpaticaData.getInstance().pushACC(x, y, z, timestamp);
	}

	@Override
	public void didReceiveBatteryLevel(float level, double timestamp) {
	//	Log.i(TAG, "didReceiveBatteryLevel: [timestamp: " + timestamp + ", value: " + level + "]");
	}

	@Override
	public void didReceiveTag(double timestamp) {
	//	Log.i(TAG, "didReceiveTag: [timestamp: " + timestamp + "]");
	}

	@Override
	public void didUpdateStatus(EmpaStatus status) {
		Log.i(TAG, "didUpdateStatus: " + status.toString());

		switch (status) {
			case READY:
				empStatus.OnStatusUpdate("Ready");
				deviceManager.startScanning();
				break;
			case CONNECTED:
				empStatus.OnStatusUpdate("Connected");
				messageHandler.post(displayEmpaticaConnected);
				break;
			case DISCOVERING:
				empStatus.OnStatusUpdate("Discovering");
				break;
			case CONNECTING:
				empStatus.OnStatusUpdate("Connecting");
				break;
			case DISCONNECTED:
				empStatus.OnStatusUpdate("Disconnected");
				messageHandler.post(displayEmpaticaDisconnected);
				break;
			case DISCONNECTING:
				empStatus.OnStatusUpdate("Disconnecting");
				break;
		}

	}

	@Override
	public void didEstablishConnection() {
		Log.i(TAG, "didEstablishConnection: Connection established");

	}

	@Override
	public void didUpdateSensorStatus(int status, EmpaSensorType type) {
		//Log.i(TAG, "didUpdateSensorStatus: [sensorType: " + type.toString() + ", status: " + status + "]");
	}

	@Override
	public void didDiscoverDevice(EmpaticaDevice device, String deviceLabel, int rssi, boolean allowed) {
		//Log.i(TAG, "didDiscoverDevice: " + device.toString() + " allowed: " + allowed);
		if (allowed) {
			deviceManager.stopScanning();
			try {
				deviceManager.connectDevice(device);
			} catch (ConnectionNotAllowedException e) {
				Log.e(TAG, "didDiscoverDevice: error while connecting to the device", e);
			}
		}
	}

	@Override
	public void didRequestEnableBluetooth() {
		// already handled in previous step.
	}

	@Override
	public void didUpdateOnWristStatus(int status) {
		Log.i(TAG, "didUpdateOnWristStatus: " + status);
	}
}
