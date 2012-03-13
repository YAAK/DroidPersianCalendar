package com.byagowi.persiancalendar;

import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;

import com.byagowi.persiancalendar.R;

import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

/**
 * Program widget on Android launcher.
 * 
 * @author ebraminio
 * 
 */
public class PersianCalendarWidget4x1 extends AppWidgetProvider {
	static private IntentFilter intentFilter = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		if (intentFilter == null) {
			intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
			context.getApplicationContext().registerReceiver(
					new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							updateTime(context);
						}
					}, intentFilter);
		}

		updateTime(context);
	}

	static public void updateTime(Context context) {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean persianDigit = prefs.getBoolean("PersianDigits", true);
		boolean gadgetClock = prefs.getBoolean("GadgetClock", true);

		AppWidgetManager manager = AppWidgetManager.getInstance(context);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget4x1);

		CivilDate civil = new CivilDate();
		PersianDate persian = DateConverter.civilToPersian(civil);

		String text1;
		String text2;

		if (gadgetClock) {
			text1 = PersianUtils.getPersianFormattedClock(new Date(),
					persianDigit);

			text2 = PersianUtils.getDayOfWeekName(civil.getDayOfWeek())
					+ PersianUtils.PERSIAN_COMMA + " "
					+ persian.toString(persianDigit);
		} else {
			text1 = PersianUtils.getDayOfWeekName(civil.getDayOfWeek());
			text2 = persian.toString(persianDigit);
		}

		remoteViews.setTextViewText(R.id.textPlaceholder1_4x1, text1);
		remoteViews.setTextViewText(R.id.textPlaceholder2_4x1, text2);

		Intent launchAppIntent = new Intent(context,
				PersianCalendarActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(
				context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_layout4x1,
				launchAppPendingIntent);

		ComponentName widget = new ComponentName(context,
				PersianCalendarWidget4x1.class);
		manager.updateAppWidget(widget, remoteViews);
	}
}
