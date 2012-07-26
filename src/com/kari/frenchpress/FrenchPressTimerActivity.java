/**
 *  Copyright 2012 Kari Wilhelm
 * 
 *  This file is part of French Press Timer.
 *  
 *  French Press Timer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  French Press Timer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with French Press Timer.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.kari.frenchpress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class FrenchPressTimerActivity extends Activity {

	private SharedPreferences preferences;

	// handler - for showing clock ticks and stopping the timer display
	Handler handler = new Handler();

	// ticker - runnable that keeps the clock ticking and updates the display until the time is up
	private Runnable ticker;

	private Button startButton;
	private TextView clockText;

	long timerEndUptime = 0;
	long timerEndRealtime = 0;

	int brewTime;

	private OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View v) {
			if (timerEndUptime == 0) {

				// Start timer
				updateBrewTime(getBrewTime());
				startTimer(brewTime);

				startButton.setText(R.string.stop_timer);
			} else {

				// Stop timer
				stopTimer();
				updateBrewTime(getBrewTime());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.main);

		// in case the value has been given by service

		if (getIntent() != null && getIntent().getExtras() != null) {
			timerEndRealtime = getIntent().getExtras().getLong(
					"endTimeRealtime", timerEndRealtime);
			
			if (timerEndRealtime < SystemClock.elapsedRealtime()) {
				timerEndRealtime = 0;
			}
		}

		preferences = PreferenceManager
				.getDefaultSharedPreferences(FrenchPressTimerActivity.this);

		clockText = (TextView) findViewById(R.id.clock_text);
		startButton = (Button) findViewById(R.id.button);
		startButton.setOnClickListener(buttonListener);

		updateBrewTime(getBrewTime());

		LicenseNotice.check(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.preferences:

			startActivity(new Intent(this, SettingsActivity.class));

			break;

		case R.id.about:

			this.showAboutDialog();

			break;

		}
		return true;
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {

		// save values if timer is running
		if (timerEndUptime != 0) {
			savedInstanceState.putLong("timerEndRealtime", timerEndRealtime);
		}

		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		updateBrewTime(getBrewTime());

		timerEndRealtime = savedInstanceState.getLong("timerEndRealtime");
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (ticker != null) {
			handler.removeCallbacks(ticker);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If the brew time has changed the preferences were changed

		int newBrewTime = getBrewTime();

		if (brewTime != newBrewTime) {
			stopTimer();

			updateBrewTime(newBrewTime);
		}
		
		// See if there is a countdown to resume

		long resumeTime = SystemClock.elapsedRealtime();

		if (timerEndRealtime != 0) {

			long timeLeft = timerEndRealtime - resumeTime;

			timerEndUptime = System.currentTimeMillis() + timeLeft;

			if (timeLeft > 0) {
				timerEndUptime = SystemClock.uptimeMillis() + timeLeft;

				ticker = new TimerTicker();

				handler.post(ticker);

				startButton.setText(R.string.stop_timer);

			} else {
				this.finish();
			}
		}

	}

	private void startTimer(int timerLengthSeconds) {
		
		long startRealtime = SystemClock.elapsedRealtime();
		long startUptime = SystemClock.uptimeMillis();

		timerEndRealtime = startRealtime + (timerLengthSeconds * 1000);
		timerEndUptime = startUptime + (timerLengthSeconds * 1000);

		ticker = new TimerTicker();
		handler.postAtTime(ticker, startUptime + 1000);

		Intent intent = new Intent(FrenchPressTimerActivity.this,TimerService.class);
		intent.putExtra("endTimeRealtime", timerEndRealtime);

		startService(intent);

	}

	private void updateBrewTime(int newBrewTime) {
		brewTime = newBrewTime;
		clockText.setText(TimeUtil.format((long) brewTime));
	}

	private int getBrewTime() {
		int newBT = Integer.valueOf(preferences.getString("brewTime", "4"));

		newBT = newBT * 60;
		// newBT = newBT + 20;

		return newBT;
	}

	private void stopTimer() {
		Intent intent = new Intent(FrenchPressTimerActivity.this,TimerService.class);
		stopService(intent);

		handler.removeCallbacks(ticker);

		timerEndUptime = 0;
		timerEndRealtime = 0;
		startButton.setText(R.string.start_timer);
	}

	private void showAboutDialog() {

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final Activity activity = this;

		builder.setTitle(R.string.about_title);

		builder.setCancelable(true);

		builder.setPositiveButton(R.string.about_done,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		builder.setNegativeButton(R.string.about_read,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(activity,
								LicenseActivity.class);
						startActivity(intent);

					}
				});

		// Construct about dialog content
		String version = "";

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;

		} catch (NameNotFoundException e) {
			// well nothing to really do here, if it didn't work we can't show
			// it
		}

		StringBuilder content = new StringBuilder();

		content.append(getString(R.string.about_version)).append(": ").append(version).append("\n\n");
		content.append(getString(R.string.about_copyright)).append(" \u00A9 ").append("2012 Kari Wilhelm");

		builder.setMessage(content);
		builder.create().show();
	}

	private class TimerTicker implements Runnable {

		@Override
		public void run() {
			
			long timeLeft = timerEndRealtime - SystemClock.elapsedRealtime();
			
			long now = SystemClock.uptimeMillis();

			long secs = timeLeft / 1000;			
			
			clockText.setText(TimeUtil.format(secs));

			if (secs > 0) {
				handler.postAtTime(this, now + 1000);
			}
			else {
				finish();
			}

		}
	}

}