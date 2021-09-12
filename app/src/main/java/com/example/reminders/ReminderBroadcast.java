package com.example.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class ReminderBroadcast extends BroadcastReceiver {

    // Constants for storing reminder items in internal memory.
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String REMINDER_ITEM = "reminderItem";
    private Context mContext;
    ArrayList<ReminderItem> reminderItemsCopy = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            loadData();
            cleanSweep();
            restartAllReminders();
        }
        else {
            Bundle bundle = intent.getExtras();
            assert bundle != null;
            String reminderDescription = bundle.getString("description");
            int notificationID = bundle.getInt("ReminderCode");
            NotificationHelper notificationHelper = new NotificationHelper(context, reminderDescription);
            NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
            notificationHelper.getManager().notify(notificationID, nb.build());
        }
    }

    // Loads the data from sharedPreferences.
    private void loadData() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonItem = sharedPreferences.getString(REMINDER_ITEM, null);
        Type typeItem = new TypeToken<ArrayList<ReminderItem>>() {}.getType();
        reminderItemsCopy = gson.fromJson(jsonItem, typeItem);

        // In case reminderItems is null, reassign it.
        if (reminderItemsCopy == null) {
            reminderItemsCopy = new ArrayList<>();
        }
    }

    // Sweeps the recycler clean for any expired reminders.
    private void cleanSweep(){
        // Corrects the reminder dates for those reminders which will be repeating.
        refreshAllRepeatReminderDates();

        Calendar timeRightNow = Calendar.getInstance();
        boolean cleanList;
        for (int outerCounter=0; outerCounter<reminderItemsCopy.size();outerCounter++) {
            cleanList=true;
            for (int counter = 0; counter < reminderItemsCopy.size(); counter++) {
                int year = reminderItemsCopy.get(counter).getReminderYear();
                int month = reminderItemsCopy.get(counter).getReminderMonth();
                int day = reminderItemsCopy.get(counter).getReminderDay();
                int hour = reminderItemsCopy.get(counter).getReminderHour();
                int minute = reminderItemsCopy.get(counter).getReminderMinute();
                int second = 0;

                Calendar timeOfDeletion = Calendar.getInstance();
                timeOfDeletion.set(year, month, day, hour, minute, second);

                if (timeOfDeletion.before(timeRightNow)) {
                    deleteReminderItem(counter);
                    cleanList=false;
                    break;
                }
            }
            if(cleanList){
                break;
            }
        }
    }

    // Refreshes the dates for all repeat reminders with expired dates.
    public void refreshAllRepeatReminderDates(){
        for(int position=0;position<reminderItemsCopy.size();position++){
            refreshRepeatReminder(position);
        }
    }

    // Refreshes the date for a particular repeat reminder.
    public void refreshRepeatReminder(int position){
        // If repeat code=0, then do nothing.
        if (reminderItemsCopy.get((position)).getReminderRepeatCode()!=0) {
            // Retrieve all reminder item information.
            Calendar alarmCalendar = Calendar.getInstance();
            String title = reminderItemsCopy.get(position).getReminderTitle();
            String date;
            String time = reminderItemsCopy.get(position).getReminderDescriptionTime();
            String repeat = reminderItemsCopy.get(position).getReminderDescriptionRepeat();
            int reminderCode = reminderItemsCopy.get(position).getReminderCode();
            int repeatCode = reminderItemsCopy.get(position).getReminderRepeatCode();
            int year = reminderItemsCopy.get(position).getReminderYear();
            int month = reminderItemsCopy.get(position).getReminderMonth();
            int day = reminderItemsCopy.get(position).getReminderDay();
            int hour = reminderItemsCopy.get(position).getReminderHour();
            int minute = reminderItemsCopy.get(position).getReminderMinute();
            int second = 0;
            alarmCalendar.set(year, month, day, hour, minute, second);

            // Only makes modifications if date is in the past.
            if (isPast(alarmCalendar)){
                // Makes necessary modifications based on the repeat code.
                if(repeatCode==1){
                    alarmCalendar.add(Calendar.DAY_OF_MONTH,1);
                }
                else if (repeatCode==2){
                    alarmCalendar.add(Calendar.DAY_OF_MONTH,7);
                }

                // Assigning the new year, month and day.
                year=alarmCalendar.get(Calendar.YEAR);
                month=alarmCalendar.get(Calendar.MONTH);
                day=alarmCalendar.get(Calendar.DAY_OF_MONTH);

                // Setting the new date.
                date= day + "/" + (month+1) + "/" + year;
                if(isToday(alarmCalendar)){
                    date="Today";
                }
                else if (isTomorrow(alarmCalendar)){
                    date="Tomorrow";
                }

                // Updating the reminder item.
                reminderItemsCopy.set(position,new ReminderItem(
                        title,
                        year,
                        month,
                        day,
                        hour,
                        minute,
                        reminderCode,
                        repeatCode,
                        date,
                        time,
                        repeat));

                // Restarting the reminder alarm.
                restartReminder(position);
            }
        }
    }

    private boolean isPast(Calendar currentCal){
        Calendar presentCal = Calendar.getInstance();
        presentCal.set(Calendar.SECOND,59);
        return currentCal.before(presentCal);
    }

    private boolean isToday(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    private boolean isTomorrow(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)+1
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    // Extracts the required data from reminderItemsCopy and sets the alarm of that designated position.
    private void restartReminder(int position) {
        Calendar alarmCalendar = Calendar.getInstance();
        String title = reminderItemsCopy.get(position).getReminderTitle();
        int reminderCode = reminderItemsCopy.get(position).getReminderCode();
        int year = reminderItemsCopy.get(position).getReminderYear();
        int month = reminderItemsCopy.get(position).getReminderMonth();
        int day = reminderItemsCopy.get(position).getReminderDay();
        int hour = reminderItemsCopy.get(position).getReminderHour();
        int minute = reminderItemsCopy.get(position).getReminderMinute();
        int second = 0;
        int repeatCode = reminderItemsCopy.get(position).getReminderRepeatCode();

        alarmCalendar.set(year,month,day,hour,minute,second);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, ReminderBroadcast.class);
        intent.putExtra("description",title);
        intent.putExtra("ReminderCode",reminderCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, reminderCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmCalendar.before(Calendar.getInstance())) {
            alarmCalendar.add(Calendar.DATE, 1);
        }
        assert alarmManager != null;
        if (repeatCode==0) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
        }
        else if (repeatCode==1){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pendingIntent);
        }
        else if (repeatCode==2){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), 1000 * 60 * 60 * 24 * 7, pendingIntent);
        }
    }

    // Restarts all reminders.
    public void restartAllReminders(){
        for(int position=0;position<reminderItemsCopy.size();position++){
            restartReminder(position);
        }
    }

    // Deletes a reminder item in that given position.
    public void deleteReminderItem(int position){
        reminderItemsCopy.remove(position);
    }

}
