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

	
	long timerEndRealtime = 0;
	
	
	private NotificationManager notificationManager;	
	
	private Notification notification;
	
	private AlarmManager alarmManager;
	
	private PendingIntent alarmIntent;
	
	private PendingIntent notificationIntent;
	
	private boolean stopTimerOnDestroy = true;


	// Unique notification number
	
	private int NOTIFICATION = 42;	
	
	
	private Handler handler = new Handler();
	
			
	private Runnable notificationUpdater = new Runnable() {
		@Override
		public void run() {

			long timeLeft = timerEndRealtime - SystemClock.elapsedRealtime();
			
			long now = SystemClock.uptimeMillis();

			long secs = timeLeft / 1000;

			String timeString = TimeUtil.format(secs);
			
			notification.setLatestEventInfo(TimerService.this, "French Press Timer", timeString, notificationIntent);
			notificationManager.notify(NOTIFICATION, notification);	

			if (secs > 0) {
				handler.postAtTime(this, now + 1000);
			}	
			else {
				stopTimerOnDestroy = false;
				
				stopSelf();
			}
			
		}
	
	};
			

    @Override
    public void onCreate() {
    	notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
 
    	timerEndRealtime = intent.getExtras().getLong("endTimeRealtime");
		
		// Set up the notification
    	
	    notification = new Notification(R.drawable.frenchpress_notification, getString(R.string.app_name) , 0);
	    
	    Intent activityIntent = new Intent(this, FrenchPressTimerActivity.class);
	    activityIntent.putExtra("endTimeRealtime", timerEndRealtime);
	    
	    notificationIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
	  
	    notification.setLatestEventInfo(this, getString(R.string.app_name), "", notificationIntent);
	    notification.flags |= Notification.FLAG_ONGOING_EVENT;
	    notification.flags |= Notification.FLAG_NO_CLEAR;
	    
	    notificationManager.notify(NOTIFICATION, notification);		
	        
	    
	    // Set up notification updates
	    
		handler.post(notificationUpdater); 
	    
		
		// Setup alarm
		
		alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 123, new Intent(this, AlarmBroadcastReciever.class), 0);
    	alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timerEndRealtime, alarmIntent);
		
		
        return START_STICKY;
    }
    
  

    @Override
    public void onDestroy() {
    	
    	// cancel the alarm
    	if (stopTimerOnDestroy) {
    		alarmManager.cancel(alarmIntent);
    	}
    	
    	// empty the Handler queue
    	handler.removeCallbacks(notificationUpdater);
 
    	// cancel the notification.
    	notificationManager.cancel(NOTIFICATION);
    }

    
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 
    
 
}
