package islam.adhanalarm;

import java.util.Calendar;

import islam.adhanalarm.service.NotifierService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class WakeUpAndDoSomething extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		WakeLock.acquire(VARIABLE.applicationContext);
		
		short timeIndex = intent.getShortExtra("timeIndex", (short)-1);
		long actualTime = intent.getLongExtra("actualTime", System.currentTimeMillis());
		if(timeIndex >= CONSTANT.FAJR && timeIndex <= CONSTANT.NEXT_FAJR) {
			NotifierService.start(timeIndex, actualTime);
		} else {
			WakeLock.release(); // Only need this to prevent notification being cut off
		}
		Schedule today = new Schedule();
		timeIndex = today.nextTimeIndex();
		setNotificationTime(context, intent, timeIndex, today.getTodaysTimes()[timeIndex]);
		
		if(VARIABLE.mainActivityIsRunning) { // Update the gui marker to show the next prayer
			Intent i = new Intent(context, AdhanAlarm.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

	public static void setNotificationTime(Context context, Intent intent, short nextNotificationTime, Calendar actualTime) {
		intent.removeExtra("timeIndex");
		intent.removeExtra("actualTime");
		if(Calendar.getInstance().after(actualTime)) return; // Somehow current time is greater than the prayer time
		
		int notificationMethod = VARIABLE.settings.getInt("notificationMethodIndex", CONSTANT.DEFAULT_NOTIFICATION);
		if(notificationMethod == CONSTANT.NO_NOTIFICATIONS) return;
		
		if(!VARIABLE.alertSunrise() && nextNotificationTime == CONSTANT.SUNRISE) nextNotificationTime = CONSTANT.DHUHR;
		
		Intent i = new Intent(context.getApplicationContext(), WakeUpAndDoSomething.class);
		i.putExtra("timeIndex", nextNotificationTime);
		i.putExtra("actualTime", actualTime.getTimeInMillis());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, actualTime.getTimeInMillis(), PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
	}
}