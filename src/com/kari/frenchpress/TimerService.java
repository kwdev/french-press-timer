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


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;


/**
 * 
 * This service keeps track of the timer progress and associated notifications.
 * 
 * @author kari
 *
 */
public class TimerService extends Service {

	
	long timerEndTime = 0;
	
	long timerStartTime;

	
	private NotificationManager notificationManager;	
	
	private Notification notification;
	
	private AlarmManager alarmManager;
	
	private PendingIntent alarmIntent;
	
	private PendingIntent activityIntent;


	// Unique notification number
	
	private int NOTIFICATION = 42;	
	
	
	private Handler handler = new Handler();
	
	
	private Runnable serviceStopper = new Runnable() {
		public void run() {
			// Timer's done, stop this service...
			//Log.i("TimerService", "Stopping timer service");
			stopSelf();
		}
	};
			
			
	private Runnable notificationUpdater = new Runnable() {
		@Override
		public void run() {
			long now = SystemClock.uptimeMillis();

			long secs = (timerEndTime - now) / 1000;

			String timeString = TimeFormatter.format(secs);
			
			notification.setLatestEventInfo(TimerService.this, "French Press Timer", timeString, activityIntent);
			notificationManager.notify(NOTIFICATION, notification);	

			if (secs > 0) {
				handler.postAtTime(this, now + 1000);
			}				
			
		}
	
	};
			

    @Override
    public void onCreate() {
    	notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
 
    	timerEndTime = intent.getExtras().getLong("endTimeUptimeMilis");
       	timerStartTime = intent.getExtras().getLong("startTimeUptimeMilis");
        		
    	
    	// Stop this service when time's up
		handler.postAtTime(serviceStopper, timerEndTime);
		
		
		// Set up the notification
	    notification = new Notification(R.drawable.frenchpress_notification, getString(R.string.app_name) , 0); //System.currentTimeMillis());
	    
	    activityIntent = PendingIntent.getActivity(this, 0, new Intent(this, FrenchPressTimerActivity.class), 0);
	    
	    notification.setLatestEventInfo(this, getString(R.string.app_name), "", activityIntent);
	    notification.flags |= Notification.FLAG_ONGOING_EVENT;
	    notification.flags |= Notification.FLAG_NO_CLEAR;
	    
	    notificationManager.notify(NOTIFICATION, notification);		
	        
	    // Set up notification updates
		handler.postAtTime(notificationUpdater, timerStartTime + 1000); 
	    
		
		// Setup alarm
		long endTime = intent.getExtras().getLong("endTimeRealtime");
		
		alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 123, new Intent(this, AlarmBroadcastReciever.class), 0);
		
    	alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, endTime, alarmIntent);
		
		
        return START_STICKY;
    }
    
  

    @Override
    public void onDestroy() {
    	
    	// cancel the alarm
    	alarmManager.cancel(alarmIntent);
    	
    	// empty the Handler queue
    	handler.removeCallbacks(serviceStopper);
    	handler.removeCallbacks(notificationUpdater);
 
    	// cancel the notification.
    	notificationManager.cancel(NOTIFICATION);
    }

    
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 
    
 
}
