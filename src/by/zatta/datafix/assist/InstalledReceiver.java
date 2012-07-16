package by.zatta.datafix.assist;

import by.zatta.datafix.BaseActivity;
import by.zatta.datafix.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class InstalledReceiver extends BroadcastReceiver {
	private NotificationManager mNotificationManager ;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (BaseActivity.DEBUG) System.out.println("Datafix received Broadcast");
		Boolean update = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
		mNotificationManager = (NotificationManager) context.getSystemService("notification");
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Boolean disabledNotifications = getPrefs.getBoolean("disableNotifications", false);
		if (!disabledNotifications && !update) makeNotification(context);		
	}
	
private void makeNotification(Context context) {
        CharSequence label = "First run the app";
        CharSequence text = "Don't forget your datafix for performance";
        CharSequence full = "then fix its data";
        final Notification notification = new Notification(R.drawable.ic_launcher,text,System.currentTimeMillis());
        notification.setLatestEventInfo(context,label,full,null);
        notification.defaults = Notification.DEFAULT_ALL;
        mNotificationManager.notify( 0, notification);
    }    

}
