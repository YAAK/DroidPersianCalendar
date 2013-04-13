package com.byagowi.persiancalendar.daemon;

import java.util.Calendar;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import calendar.CivilDate;
import calendar.DateConverter;
import calendar.PersianDate;

import com.byagowi.persiancalendar.MainActivity;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.utils.Utils;
import com.google.android.apps.dashclock.api.ExtensionData;

/**
 * Updating logics comes here.
 * 
 * It is not very clear and clean piece of code because my tend was on
 * optimizing this part by reusing commons things
 * 
 * @author ebraminio
 */
public class UpdateUtils {
	private static UpdateUtils myInstance;

	public static UpdateUtils getInstance() {
		if (myInstance == null) {
			myInstance = new UpdateUtils();
		}
		return myInstance;
	}

	private UpdateUtils() {
	}

	//

	private final Utils utils = Utils.getInstance();

	// Updater
	public void update(Context context) {
		updateCommonValues(context);
		updateNotification(context);
		updateWidgets(context);
	}

	// Notification Update
	private static final int NOTIFICATION_ID = 1001;
	private NotificationManager mNotificationManager;
	private Bitmap largeIcon;

	private void updateNotification(Context context) {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		if (prefs.getBoolean("NotifyDate", true)) {
			if (largeIcon == null) {
				largeIcon = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.launcher_icon);
			}

			Notification notification = new NotificationCompat.Builder(context)
					.setPriority(NotificationCompat.PRIORITY_LOW)
					.setOngoing(true).setLargeIcon(largeIcon)
					.setSmallIcon(dayIcon)
					.setContentIntent(launchAppPendingIntent)
					.setContentText(utils.textShaper(getNotificationBody()))
					.setContentTitle(utils.textShaper(getNotificationTitle()))
					.build();

			mNotificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
	}

	// Widgets Update
	private void updateWidgets(Context context) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(),
				R.layout.widget1x1);
		RemoteViews remoteViews4 = new RemoteViews(context.getPackageName(),
				R.layout.widget4x1);
		int color = prefs.getInt("WidgetTextColor", Color.WHITE);

		// Widget 1x1
		remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
		remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
		remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
				utils.formatNumber(persian.getDayOfMonth(), digits));
		remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
				utils.textShaper(persian.getMonthName()));
		remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1,
				launchAppPendingIntent);
		manager.updateAppWidget(new ComponentName(context, Widget1x1Listener.class),
				remoteViews1);

		// Widget 4x1
		remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
		remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
		remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

		String text1 = utils.getDayOfWeekName(civil.getDayOfWeek());
		String dayTitle = utils.dateToString(persian, digits);
		String text2 = dayTitle + utils.PERSIAN_COMMA + " "
				+ utils.dateToString(civil, digits);
		String text3 = "";

		if (prefs.getBoolean("WidgetClock", true)) {
			text2 = text1 + " " + text2;
			boolean in24 = prefs.getBoolean("WidgetIn24", true);
			text1 = utils.getPersianFormattedClock(calendar, digits, in24);
			if (iranTime) {
				text3 = "(" + utils.irdt + ")";
			}
		}

		remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1,
				utils.textShaper(text1));
		remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1,
				utils.textShaper(text2));
		remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1,
				utils.textShaper(text3));

		remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1,
				launchAppPendingIntent);
		manager.updateAppWidget(new ComponentName(context, Widget4x1Listener.class),
				remoteViews4);
	}

	// DashClock needed data
	public ExtensionData getDashClockUpdatedData(Context context) {
		updateCommonValues(context);
		return new ExtensionData().visible(true).icon(dayIcon)
				.status(utils.textShaper(persian.getMonthName()))
				.expandedTitle(utils.textShaper(getNotificationTitle()))
				.expandedBody(utils.textShaper(getNotificationBody()))
				.clickIntent(new Intent(context, MainActivity.class));
	}

	// Common values and methods
	private SharedPreferences prefs;
	private char[] digits;
	private boolean iranTime;
	private Calendar calendar;
	private CivilDate civil;
	private PersianDate persian;
	private PendingIntent launchAppPendingIntent;
	private int dayIcon;

	private void updateCommonValues(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		digits = utils.preferredDigits(context);
		iranTime = prefs.getBoolean("IranTime", false);
		calendar = utils.makeCalendarFromDate(new Date(), iranTime);
		civil = new CivilDate(calendar);
		persian = DateConverter.civilToPersian(civil);
		persian.setDari(utils.isDariVersion(context));

		launchAppPendingIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		dayIcon = utils.getDayIconResource(persian.getDayOfMonth());

		updateWidgets(context);
		updateNotification(context);
	}

	private String getNotificationBody() {
		return utils.dateToString(civil, digits)
				+ utils.PERSIAN_COMMA
				+ " "
				+ utils.dateToString(DateConverter.civilToIslamic(civil),
						digits);
	}

	private String getNotificationTitle() {
		return utils.getDayOfWeekName(civil.getDayOfWeek()) + " "
				+ utils.dateToString(persian, digits);
	}
}
