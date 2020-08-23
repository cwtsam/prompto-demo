package com.stfalcon.chatkit.sample.features.main.org.ahlab;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileWriter {
	private static final String TAG = "[FileWriter]";
	private static FileWriter instance;
	private List<String> eda;
	private List<String> ibi;
	private List<String> acc;
	private List<String> bvp;
	private List<String> timestamp;
	private List<String> messageHistory;
	private String name;
	private int gender;
	private int session;
	private String age;
	private String basePath;

	private boolean ready;

	private FileWriter() {
		eda = new ArrayList<>();
		ibi = new ArrayList<>();
		acc = new ArrayList<>();
		bvp = new ArrayList<>();
		timestamp = new ArrayList<>();
		messageHistory = new ArrayList<>();
	}

	public static synchronized FileWriter getInstance() {
		if (instance == null) {
			instance = new FileWriter();
		}
		return instance;
	}

	public boolean initSession(String name, String age, int gender, int session) {
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.session = session;

		Log.i(TAG, "initSession: DocDir " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).exists());
		File baseDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Prompto"); // can change the directory from here
		if (!baseDir.exists()) {
			Log.i(TAG, "initSession: creating base dir");
			boolean status = baseDir.mkdir();
			Log.i(TAG, "initSession: base dir creation: " + status);
		}
		File user = new File(baseDir, name);
		if (!user.exists()) {
			boolean status = user.mkdir();
			Log.i(TAG, "user not exists, creating dir: " + status);
		} else {
			Log.i(TAG, "user exists: " + user.getAbsolutePath());
		}
		int i = 0;
		String attemptIdx = String.valueOf(i);
		File attempt = new File(user, attemptIdx);
		while (attempt.exists()) {
			attemptIdx = String.valueOf(++i);
			attempt = new File(user, attemptIdx);
			Log.i(TAG, "initFileStructure: session exist: " + attemptIdx);
		}
		attempt.mkdir();
		basePath = attempt.getPath();

		writeInfoFile();

		ready = true;
		return ready;
	}

	private void writeInfoFile() {
		String path = basePath + "/info.txt";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Name: ").append(name).append("\n");
		stringBuilder.append("Age: ").append(age).append("\n");
		stringBuilder.append("Gender: ").append(gender).append("\n");
		stringBuilder.append("Session: ").append(session);
		write(path, stringBuilder);
	}

	private void write(String path, StringBuilder stringBuilder) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new java.io.FileWriter(path, true))) {
			bufferedWriter.append(stringBuilder);
			bufferedWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "writeCSV: error while writing", e);
		}
	}

	private void writeFile(List<String> accel, String path) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String record : accel) {
			stringBuilder.append(record).append("\n");
		}
		write(path, stringBuilder);
	}

	public synchronized void appendEDA(String record) {

		eda.add(record);
		if (eda.size() > 10) {
			String path = basePath + "/eda.csv";
			writeFile(eda, path);
			eda = new ArrayList<>();
		}
	}

	public synchronized void appendIBI(String record) {

		ibi.add(record);
		if (ibi.size() > 10) {
			String path = basePath + "/ibi.csv";
			writeFile(ibi, path);
			ibi = new ArrayList<>();
		}
	}

	public synchronized void appendAcc(String record) {

		acc.add(record);
		if (acc.size() > 10) {
			String path = basePath + "/acc.csv";
			writeFile(acc, path);
			acc = new ArrayList<>();
		}
	}

	public synchronized void appendBVP(String record) {

		bvp.add(record);
		if (bvp.size() > 10) {
			String path = basePath + "/bvp.csv";
			writeFile(bvp, path);
			bvp = new ArrayList<>();
		}
	}

	public synchronized void appendTime(String record) {

		timestamp.add(record);
			String path = basePath + "/timestamp.csv";
			writeFile(timestamp, path);
			timestamp = new ArrayList<>();

	}

	public synchronized void appendMessageHistory(String record) {

		messageHistory.add(record);
		String path = basePath + "/msgHistory.csv";
		writeFile(messageHistory, path);
		messageHistory = new ArrayList<>();
	}


}
