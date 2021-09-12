package com.example.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Defining the reminder title and description.
    public String newReminderTitle;
    public int newReminderYear;
    public int newReminderMonth;
    public int newReminderDay;
    public int newReminderHour;
    public int newReminderMinute;
    public int newReminderSecond;
    public int newReminderCode;
    public int newReminderRepeatCode;
    public String newReminderDescriptionDate;
    public String newReminderDescriptionTime;
    public String newReminderDescriptionRepeat;

    // Recycler variables.
    public int recyclerPosition=0;
    public RecyclerAdapter recyclerAdapter;
    private RecyclerAdapter.RecyclerViewClickListener listener;

    // This is an array list containing all the reminder item information (titles, descriptions, time, alarm code, etc).
    public static ArrayList<ReminderItem> reminderItems = new ArrayList<>();

    // Constants for storing reminder items in internal memory.
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String REMINDER_ITEM = "reminderItem";

    // Defining a new handler which allows us to execute the runnable.
    public Handler handler = new Handler();

    // Defining a runnable called deletion search which will repeat every second. It searches for reminder items which are past their alarm time and deletes them.
    Runnable deletionSearch = new Runnable() {
        @Override
        public void run() {
            cleanSweep();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loads stored data on creation of activity.
        loadData();

        // Building the recycler view.
        buildRecyclerView();

        // Sweeps the recycler clean for any expired reminders.
        cleanSweep();

        // Restarts all reminders on creation.
        restartAllReminders();

        // Repeats a clean sweep every second.
        startRepeatingDeletionSearch();

        // What happens when floating action button is clicked.
        final FloatingActionButton newReminder = findViewById(R.id.newReminder);
        newReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bundle containing only the reminder code is sent over to the "New Reminder" activity.
                Bundle emptyBundle = new Bundle();
                emptyBundle.putBoolean("isEmpty",true);
                emptyBundle.putInt("ReminderCode",returnNewReminderCode());
                Intent resultIntent=new Intent(MainActivity.this,NewReminder.class);
                resultIntent.putExtras(emptyBundle);
                startActivityForResult(resultIntent,0);
            }
        });

    }


    // What happens once the user exits from the "New Reminder" activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent)
    {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        // Repeated clean sweeps can begin again now.
        cleanSweep();
        startRepeatingDeletionSearch();

        // This is the case when a new reminder is created.
        if (resultCode==0) {
            //Retrieve result bundle from result intent.
            Bundle resultBundle = resultIntent.getBundleExtra("Bundle");
            assert resultBundle != null;

            //Retrieve all reminder item information. Including time, titles, description and codes.
            newReminderSet(resultBundle);

            // Inserting the new reminder item into the recycler.
            recyclerPosition = reminderItems.size();
            insertReminderItem(recyclerPosition);
        }

        // This is the case when an existing reminder is edited.
        else if (resultCode==1){
            //Retrieve result bundle from result intent.
            Bundle resultBundle = resultIntent.getBundleExtra("Bundle");
            assert resultBundle != null;

            //Retrieve all reminder item information. Including time, titles, description and codes.
            newReminderSet(resultBundle);

            // Edits the reminderItem and updates the recycler.
            int pos = resultBundle.getInt("Position");
            editReminderItem(pos);
        }

        // This is the case when an existing reminder is deleted.
        else if (resultCode==3){
            //Retrieve result bundle from result intent.
            Bundle resultBundle = resultIntent.getBundleExtra("Bundle");
            assert resultBundle != null;

            // Deleted the reminderItem and updates the recycler.
            int position = resultBundle.getInt("Position");
            deleteReminderItem(position);
        }
    }


    // When app is stopped, the data is saved and the repeated clean sweeps are halted.
    @Override
    protected void onStop() {
        saveData();
        stopRepeatingDeletionSearch();
        super.onStop();
    }


    // When app is started, a clean sweep is initiated for expired reminders and the repeated sweeps are initiated.
    protected void onStart() {
        cleanSweep();
        startRepeatingDeletionSearch();
        super.onStart();
    }


    // When app is destroyed, the data is saved and the repeated clean sweeps are halted.
    @Override
    protected void onDestroy() {
        saveData();
        stopRepeatingDeletionSearch();
        super.onDestroy();
    }


    // Method for building the recycler view with the items from ReminderItems.
    public void buildRecyclerView(){
        setOnClickListener();
        RecyclerView reminderRecycler = findViewById(R.id.reminderRecycler);
        reminderRecycler.setHasFixedSize(false);
        RecyclerView.LayoutManager reminderLayoutManager = new LinearLayoutManager(this);
        recyclerAdapter = new RecyclerAdapter(reminderItems, listener);
        reminderRecycler.setLayoutManager(reminderLayoutManager);
        reminderRecycler.setAdapter(recyclerAdapter);
        reminderRecycler.addItemDecoration(new MemberItemDecoration(300));
    }

    // What happens when a reminder item is clicked on. A new bundle containing all the reminder item information is sent to the New Reminder activity.
    private void setOnClickListener() {
        listener = new RecyclerAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                // Clean sweeps are stopped.
                stopRepeatingDeletionSearch();

                // Intent to go to New Reminder is created.
                Intent resultIntent = new Intent(getApplicationContext(),NewReminder.class);

                // createNewReminderBundle creates a new bundle containing all the necessary information to recreate the New Reminder activity.
                resultIntent.putExtras(createNewReminderBundle(position));

                // New Reminder activity is started.
                startActivityForResult(resultIntent,1);
            }
        };
    }

    // Inserts a new reminder item into the designated position.
    public void insertReminderItem(int position){
        reminderItems.add(position,new ReminderItem(
                newReminderTitle,
                newReminderYear,
                newReminderMonth,
                newReminderDay,
                newReminderHour,
                newReminderMinute,
                newReminderCode,
                newReminderRepeatCode,
                newReminderDescriptionDate,
                newReminderDescriptionTime,
                newReminderDescriptionRepeat));
        recyclerAdapter.notifyDataSetChanged();
    }

    // Edits a reminder item at the designated position.
    public void editReminderItem(int position){
        reminderItems.set(position,new ReminderItem(
                newReminderTitle,
                newReminderYear,
                newReminderMonth,
                newReminderDay,
                newReminderHour,
                newReminderMinute,
                newReminderCode,
                newReminderRepeatCode,
                newReminderDescriptionDate,
                newReminderDescriptionTime,
                newReminderDescriptionRepeat));
        recyclerAdapter.notifyDataSetChanged();
    }

    // Deleted a reminder item at the designated position.
    public void deleteReminderItem(int position){
        reminderItems.remove(position);
        recyclerAdapter.notifyDataSetChanged();
    }

    // Creates a reminder code (which will act as a alarm manager request code).
    public int returnNewReminderCode(){
        // If reminderItems is empty, it assigns a code of 0.
        if (reminderItems.size()==0){
            return(0);
        }
        // Else it takes the reminder code of the last item and adds 1.
        else{
            int lastPosition = reminderItems.size()-1;
            return(reminderItems.get(lastPosition).getReminderCode()+1);
        }
    }

    // Starts clean sweeping every second.
    void startRepeatingDeletionSearch() {
        handler.postDelayed(deletionSearch,1000);
    }

    // Stops clean sweeping.
    void stopRepeatingDeletionSearch() {
        handler.removeCallbacks(deletionSearch);
    }

    // Saves the data using sharedPreferences.
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonItem = gson.toJson(reminderItems);
        editor.putString(REMINDER_ITEM, jsonItem);
        editor.apply();
    }

    // Loads the data from sharedPreferences.
    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonItem = sharedPreferences.getString(REMINDER_ITEM, null);

        Type typeItem = new TypeToken<ArrayList<ReminderItem>>() {}.getType();

        reminderItems = gson.fromJson(jsonItem, typeItem);

        // In case reminderItems is null, reassign it.
        if (reminderItems == null) {
            reminderItems = new ArrayList<>();
        }
    }

    // Creates a new reminder bundle to be sent to the New Reminder activity. Requires item position.
    private Bundle createNewReminderBundle(int position){
        Bundle newBundle = new Bundle();
        newBundle.putString("ReminderTitle", reminderItems.get(position).getReminderTitle());
        newBundle.putInt("ReminderCode", reminderItems.get(position).getReminderCode());
        newBundle.putInt("Year", reminderItems.get(position).getReminderYear());
        newBundle.putInt("Month", reminderItems.get(position).getReminderMonth());
        newBundle.putInt("Day", reminderItems.get(position).getReminderDay());
        newBundle.putInt("Hour", reminderItems.get(position).getReminderHour());
        newBundle.putInt("Minute", reminderItems.get(position).getReminderMinute());
        newBundle.putInt("Position",position);
        newBundle.putInt("RepeatCode", reminderItems.get(position).getReminderRepeatCode());
        newBundle.putBoolean("isEmpty", false);
        return(newBundle);
    }

    // Extracts all the reminder item information from an input bundle.
    private void newReminderSet(Bundle resultBundle){
        newReminderTitle=resultBundle.getString("ReminderTitle");
        newReminderYear = resultBundle.getInt("Year");
        newReminderMonth = resultBundle.getInt("Month");
        newReminderDay = resultBundle.getInt("Day");
        newReminderHour = resultBundle.getInt("Hour");
        newReminderMinute = resultBundle.getInt("Minute");
        newReminderSecond = 0;
        newReminderCode = resultBundle.getInt("ReminderCode");
        newReminderRepeatCode = resultBundle.getInt("RepeatCode");
        newReminderDescriptionDate=resultBundle.getString("ReminderDescriptionDate");
        newReminderDescriptionTime=resultBundle.getString("ReminderDescriptionTime");
        newReminderDescriptionRepeat=resultBundle.getString("ReminderDescriptionRepeat");

    }

    // Sweeps the recycler clean for any expired reminders.
    private void cleanSweep(){
        // Corrects the reminder dates for those reminders which will be repeating.
        refreshAllRepeatReminderDates();

        // Starts removing expired reminders.
        Calendar timeRightNow = Calendar.getInstance();
        boolean cleanList;
        for (int outerCounter=0; outerCounter<reminderItems.size();outerCounter++) {
            cleanList=true;
            for (int counter = 0; counter < reminderItems.size(); counter++) {
                int year = reminderItems.get(counter).getReminderYear();
                int month = reminderItems.get(counter).getReminderMonth();
                int day = reminderItems.get(counter).getReminderDay();
                int hour = reminderItems.get(counter).getReminderHour();
                int minute = reminderItems.get(counter).getReminderMinute();
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

        // Refreshes the reminder date descriptions (correcting "Today" and "Tomorrow" depending on the date).
        refreshAllDateDescriptions();
    }

    // Restarts the reminder at the designated position.
    public void restartReminder(int position) {
        // Extracting required reminder item information.
        Calendar alarmCalendar = Calendar.getInstance();
        String title = reminderItems.get(position).getReminderTitle();
        int reminderCode = reminderItems.get(position).getReminderCode();
        int year = reminderItems.get(position).getReminderYear();
        int month = reminderItems.get(position).getReminderMonth();
        int day = reminderItems.get(position).getReminderDay();
        int hour = reminderItems.get(position).getReminderHour();
        int minute = reminderItems.get(position).getReminderMinute();
        int second = 0;
        int repeatCode = reminderItems.get(position).getReminderRepeatCode();
        alarmCalendar.set(year,month,day,hour,minute,second);

        // Restarting the alarm.
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);
        intent.putExtra("description",title);
        intent.putExtra("ReminderCode",reminderCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminderCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmCalendar.before(Calendar.getInstance())) {
            alarmCalendar.add(Calendar.DATE, 1);
        }
        assert alarmManager != null;

        // Alarm repeats depending on the repeat code.
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
        for(int position=0;position<reminderItems.size();position++){
            restartReminder(position);
        }
    }

    // Refreshes the dates of a repeat reminder with an expired date.
    public void refreshRepeatReminder(int position){
        // If repeat code=0, then do nothing.
        if (reminderItems.get((position)).getReminderRepeatCode()!=0) {
            // Retrieve all reminder item information.
            Calendar alarmCalendar = Calendar.getInstance();
            String title = reminderItems.get(position).getReminderTitle();
            String date;
            String time = reminderItems.get(position).getReminderDescriptionTime();
            String repeat = reminderItems.get(position).getReminderDescriptionRepeat();
            int reminderCode = reminderItems.get(position).getReminderCode();
            int repeatCode = reminderItems.get(position).getReminderRepeatCode();
            int year = reminderItems.get(position).getReminderYear();
            int month = reminderItems.get(position).getReminderMonth();
            int day = reminderItems.get(position).getReminderDay();
            int hour = reminderItems.get(position).getReminderHour();
            int minute = reminderItems.get(position).getReminderMinute();
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
                reminderItems.set(position,new ReminderItem(
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

                // Refreshing recycler.
                recyclerAdapter.notifyDataSetChanged();
            }
        }
    }

    // Returns true if the input calendar has the date information of today.
    private boolean isToday(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    // Returns true if the input calendar has the date information of tomorrow.
    private boolean isTomorrow(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)+1
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    // Refreshes the expired dates of all repeat reminders.
    public void refreshAllRepeatReminderDates(){
        for(int position=0;position<reminderItems.size();position++){
            refreshRepeatReminder(position);
        }
    }

    // Refreshes the date description for a reminder (updating today/tomorrow information).
    public void refreshDateDescription(int position){
        String date= reminderItems.get(position).getReminderDescriptionDate();
        if (date.equals("Today")||date.equals("Tomorrow")) {
            // Retrieve all reminder item information.
            Calendar alarmCalendar = Calendar.getInstance();
            String title = reminderItems.get(position).getReminderTitle();
            String time = reminderItems.get(position).getReminderDescriptionTime();
            String repeat = reminderItems.get(position).getReminderDescriptionRepeat();
            int reminderCode = reminderItems.get(position).getReminderCode();
            int repeatCode = reminderItems.get(position).getReminderRepeatCode();
            int year = reminderItems.get(position).getReminderYear();
            int month = reminderItems.get(position).getReminderMonth();
            int day = reminderItems.get(position).getReminderDay();
            int hour = reminderItems.get(position).getReminderHour();
            int minute = reminderItems.get(position).getReminderMinute();
            int second = 0;
            alarmCalendar.set(year, month, day, hour, minute, second);

            // Setting the new date.
            date = day + "/" + (month + 1) + "/" + year;
            if (isToday(alarmCalendar)) {
                date = "Today";
            } else if (isTomorrow(alarmCalendar)) {
                date = "Tomorrow";
            }

            // Resetting the reminder item.
            reminderItems.set(position, new ReminderItem(
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

            // Refreshing recycler.
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    // Refreshes the reminder date for all reminders.
    public void refreshAllDateDescriptions(){
        for(int position=0;position<reminderItems.size();position++){
            refreshDateDescription(position);
        }
    }

    // Returns true if the calendar contains the date of the past.
    private boolean isPast(Calendar currentCal){
        Calendar presentCal = Calendar.getInstance();
        presentCal.set(Calendar.SECOND,59);
        return currentCal.before(presentCal);
    }

}
