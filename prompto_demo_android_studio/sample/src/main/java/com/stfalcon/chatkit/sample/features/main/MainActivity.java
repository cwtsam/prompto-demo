package com.stfalcon.chatkit.sample.features.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.features.demo.custom.holder.CustomHolderDialogsActivity;
import com.stfalcon.chatkit.sample.features.demo.custom.layout.CustomLayoutDialogsActivity;
import com.stfalcon.chatkit.sample.features.demo.custom.media.CustomMediaMessagesActivity;
import com.stfalcon.chatkit.sample.features.demo.def.DefaultDialogsActivity;
import com.stfalcon.chatkit.sample.features.demo.styled.StyledDialogsActivity;
import com.stfalcon.chatkit.sample.features.demo.styled.StyledMessagesActivity;
import com.stfalcon.chatkit.sample.features.main.adapter.DemoCardFragment;
import com.stfalcon.chatkit.sample.features.main.adapter.MainActivityPagerAdapter;

public class MainActivity extends AppCompatActivity
		implements DemoCardFragment.OnActionListener, EMPStatus {

	private static final String API_KEY = "8d071b71294342019170d75f9b47b7d7";
	private static final String TAG = "[MainActivity]";

	private boolean isAudio;
	private boolean isLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		EmpaticaAdapter.getInstance(this, this);

		validatePermissions();
	}

	private void validatePermissions() {
		if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0);
		} else {
			isAudio = true;
		}

		if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
		} else {
			isLocation = true;
		}

		if (isAudio && isLocation) {
			EmpaticaAdapter.getInstance(this, this).accessGranted(this);
			StyledMessagesActivity.open(this);
		}
	}

	@Override
	public void onAction(int id) {
		switch (id) {
			case MainActivityPagerAdapter.ID_DEFAULT:
				DefaultDialogsActivity.open(this);
				break;
			case MainActivityPagerAdapter.ID_STYLED:
				StyledDialogsActivity.open(this);
				break;
			case MainActivityPagerAdapter.ID_CUSTOM_LAYOUT:
				CustomLayoutDialogsActivity.open(this);
				break;
			case MainActivityPagerAdapter.ID_CUSTOM_VIEW_HOLDER:
				CustomHolderDialogsActivity.open(this);
				break;
			case MainActivityPagerAdapter.ID_CUSTOM_CONTENT:
				CustomMediaMessagesActivity.open(this);
				break;
		}
	}

	@Override
	public void OnStatusUpdate(String status) {
		//
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		validatePermissions();
	}
}
