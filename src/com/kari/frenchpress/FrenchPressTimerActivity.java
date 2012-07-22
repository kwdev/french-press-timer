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
import android.widget.Button;
import android.widget.TextView;

public class FrenchPressTimerActivity extends Activity {

	private SharedPreferences preferences;

	// handler - for showing clock ticks and stopping the timer display
	Handler handler = new Handler();
	
	// ticker - runnable that keeps the clock ticking and updates the display
	private Runnable ticker;
	
	// stopper - runnable that stops this activity when the time is up
	private Runnable stopper;

	
	private Button startButton;
	private TextView clockText;
	
	long timerEndTime = 0;
	long timerStartTime;
	int brewTime;



	private OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View v) {
			if (timerEndTime == 0) {
				
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

		setContentView(R.layout.main);

		preferences = PreferenceManager.getDefaultSharedPreferences(FrenchPressTimerActivity.this);	
		
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

		// save what's important - when the timer stops
		savedInstanceState.putLong("timerStartTime", timerStartTime);
		savedInstanceState.putLong("timerEndTime", timerEndTime);

		super.onSaveInstanceState(savedInstanceState);
	}


	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		updateBrewTime(getBrewTime());

		// restart ticker if it was running
		timerStartTime = savedInstanceState.getLong("timerStartTime");
		timerEndTime = savedInstanceState.getLong("timerEndTime");
		
		if (timerEndTime != 0) {
			ticker = new TimerTicker();
			handler.post(ticker);
			startButton.setText(R.string.stop_timer);
		}
	
	}

	

	@Override
	protected void onResume() {
		super.onResume();
		
		// If the brew time has changed the preferences were changed
		
		int newBrewTime = getBrewTime();
		
		if (brewTime != newBrewTime)
		{
			stopTimer();
			
			updateBrewTime(newBrewTime);
		}
		
	}


	private void startTimer(int timerLengthSeconds) {
		
		// get the current time - using both of these formats like this is precise enough for this application
		
		// required format for the alarm parameter
		long utcTime = SystemClock.elapsedRealtime();
		
		// required format for notifications
		timerStartTime = SystemClock.uptimeMillis();
		
		timerEndTime = timerStartTime + timerLengthSeconds * 1000;
		

		ticker = new TimerTicker();
		stopper = new TimerEndpoint();

		handler.postAtTime(stopper, timerEndTime);
		handler.postAtTime(ticker, timerStartTime + 1000);

		Intent intent = new Intent(FrenchPressTimerActivity.this, TimerService.class);
		intent.putExtra("startTimeUptimeMilis", timerStartTime);
		intent.putExtra("endTimeUptimeMilis", timerEndTime);
		intent.putExtra("endTimeRealtime", utcTime+ timerLengthSeconds * 1000);

		startService(intent);

	}
	

	private void updateBrewTime(int newBrewTime) {
		brewTime = newBrewTime;
		clockText.setText(TimeFormatter.format((long)brewTime));
	}
	
	
	private int getBrewTime() {
		 int newBT = Integer.valueOf(preferences.getString("brewTime", "4"));
		 
		 newBT = newBT * 60;
		 
		return newBT;
	}

	
	private void stopTimer() {
		Intent intent = new Intent(FrenchPressTimerActivity.this, TimerService.class);
		stopService(intent);

		handler.removeCallbacks(ticker);
		handler.removeCallbacks(stopper);	
		
		timerEndTime = 0;
		startButton.setText(R.string.start_timer);
	}
	
	
	private void showAboutDialog() {
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		final Activity activity = this;
		
		builder.setTitle(R.string.about_title);
		
		builder.setCancelable(true);
		
		builder.setPositiveButton(R.string.about_done, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
			}
		});
		
		builder.setNegativeButton(R.string.about_read, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//LicenseNotice.showLicenseNotice(activity);
				
				Intent intent = new Intent(activity, LicenseActivity.class);
				startActivity(intent);
				
			}
		});
		

		// Construct about dialog content
		String version = "";
		
		
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = packageInfo.versionName;
			
		} catch (NameNotFoundException e) {
			// well nothing to really do here, if it didn't work we can't show it
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
			long now = SystemClock.uptimeMillis();

			long secs = (timerEndTime - now) / 1000;

			clockText.setText(TimeFormatter.format(secs));

			if (secs > 0) {
				handler.postAtTime(this, now + 1000);
			}

		}
	}

	private class TimerEndpoint implements Runnable {

		@Override
		public void run() {

			finish(); // don't need ya anymore, coffee is done!!

		}

	}
}