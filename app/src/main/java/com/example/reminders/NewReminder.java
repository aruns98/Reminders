package com.example.reminders;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;
import java.util.Objects;

public class NewReminder extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Toolbar toolbar;
    Spinner setRepeatSpinner;
    Button setDateBtn;
    Button setTimeBtn;

    Calendar alarmDateTime;
    Calendar oldAlarmDateTime;

    String reminderTitle;
    String oldReminderTitle;
    String today = "Today";
    String tomorrow = "Tomorrow";

    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
    int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int defaultMinute = defaultMinuteInt();
    String defaultMinuteStr = defaultMinute();
    int defaultSecond = 0;

    int datePickerYear = Calendar.getInstance().get(Calendar.YEAR);
    int datePickerMonth = Calendar.getInstance().get(Calendar.MONTH);
    int datePickerDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int timePickerHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int timePickerMinute = defaultMinuteInt();

    String time = formatHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))+":"+defaultMinuteStr+formatTimeAppend(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    String date = today;
    String repeat;

    public int reminderCode;
    public int repeatCode=0;
    public EditText reminderName;
    public boolean isReminderEdit = false;
    public int reminderPosition;


    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_reminder);

        // Populating the spinner.
        setRepeatSpinner = findViewById(R.id.setRepeatSpinner);
        populateSpinner();

        // Populating the toolbar.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        // Finding the reminderName EditText.
        reminderName = findViewById(R.id.reminderName);

        // Setting default text of setDateBtn.
        setDateBtn = findViewById(R.id.setDateBtn);
        setDateBtn.setText(today);

        // Opens date picker dialog on click.
        setDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Setting default text of setTimeBtn.
        setTimeBtn = findViewById(R.id.setTimeBtn);
        String time = currentHour()+":"+defaultMinute()+timeAppend();
        setTimeBtn.setText(time);

        // Setting default time of alarm.
        alarmDateTime = Calendar.getInstance();
        oldAlarmDateTime = Calendar.getInstance();
        alarmDateTime.set(currentYear,currentMonth,currentDay,currentHour,defaultMinute,defaultSecond);

        // Opens timePicker dialog when setTime button is clicked.
        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        // Handling incoming intent.
        Intent incomingIntent = getIntent();
        Bundle incomingBundle = incomingIntent.getExtras();
        isReminderEdit=false;
        assert incomingBundle != null;
        // If bundle is NOT empty, then set the new default values.
        reminderCode = incomingBundle.getInt("ReminderCode");
        if (!incomingBundle.getBoolean("isEmpty")){
            setIncomingDefaults(incomingBundle);
            isReminderEdit=true;
            stopReminder();
        }
    }

    // Populates spinner.
    private void populateSpinner(){
        String[] repeatOptions = getResources().getStringArray(R.array.repeat_options);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner, repeatOptions);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner);
        setRepeatSpinner.setAdapter(spinnerAdapter);
    }

    // Inflates menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // onClickListener for the toolbar items.
    public boolean onOptionsItemSelected(MenuItem item){
        String msg=" ";
        switch (item.getItemId()){
            case R.id.acceptBtn:
                setReminderName();
                // Prevents input of past date.
                if (isPast(alarmDateTime)){
                    msg="Enter a time in the future";
                }
                else {
                    // Prevents input of empty reminder name.
                    if (isValidReminderName()) {
                        startReminder(alarmDateTime, reminderTitle);
                        // isReminderEdit is true if an existing reminder is being modified.
                        if (isReminderEdit){
                            msg = "Reminder modified";
                            setResult(1,createResultIntent());
                        }
                        else{
                            msg = "Reminder created";
                            setResult(0,createResultIntent());
                        }
                        finish();
                    }
                    else{
                        msg= "Enter a valid reminder name";
                    }
                }
                break;
            case R.id.deleteBtn:
                if (isReminderEdit){
                    msg="Reminder deleted";
                    setResult(3,createDeleteIntent());
                }
                else{
                    msg="Reminder discarded";
                    setResult(2,createDeleteIntent());
                }
                finish();
                break;
        }
        // Displays the appropriate toast.
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        return super.onOptionsItemSelected(item);
    }

    // What happens when the back button is pressed.
    @Override
    public void onBackPressed() {
        if(isReminderEdit) {
            if (!isPast(oldAlarmDateTime)) {
                startReminder(oldAlarmDateTime, oldReminderTitle);
                setResult(2,createDeleteIntent());
            }
            else{
                setResult(3,createDeleteIntent());
            }
        }
        else{
            setResult(2,createDeleteIntent());
        }
        finish();
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, datePickerYear, datePickerMonth, datePickerDay);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Define what happens after time is picked.
                        alarmDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarmDateTime.set(Calendar.MINUTE, minute);

                        // Creating new default time picker values.
                        timePickerHour=hourOfDay;
                        timePickerMinute=minute;

                        // Setting setTimeBtn text.
                        time = formatHour(hourOfDay)+":"+formatMinute(minute)+formatTimeAppend(hourOfDay);
                        setTimeBtn.setText(time);
                    }
                },
                timePickerHour,
                timePickerMinute,
                false);

        timePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // Setting new default date picker values.
        datePickerYear=year;
        datePickerMonth=month;
        datePickerDay=dayOfMonth;

        // Setting time of alarm.
        alarmDateTime.set(year,month,dayOfMonth);

        // Setting setDateBtn text.
        date= dayOfMonth + "/" + (month+1) + "/" + year;
        if(isToday(alarmDateTime)){
            date=today;
        }
        else if (isTomorrow(alarmDateTime)){
            date=tomorrow;
        }
        setDateBtn.setText(date);
    }

    //Returns current hour.
    private String currentHour(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour>12){
            hour=hour-12;
        }
        if (hour==0){
            hour=12;
        }
        return(Integer.toString(hour));
    }

    // Returns the formatted input hour.
    private String formatHour(int hour){
        if (hour>12){
            hour=hour-12;
        }
        if (hour==0){
            hour=12;
        }
        return(Integer.toString(hour));
    }

    // Returns the default minute (5 min into the future).
    private String defaultMinute(){
        int minute = (Calendar.getInstance().get(Calendar.MINUTE));
        if(minute<55){
            minute=minute+5;
        }
        else{
            minute=59;
        }
        String minuteString = Integer.toString(minute);
        if (minute<10){
            minuteString = "0"+minute;
        }
        return(minuteString);
    }

    // Returns the default minute as an integer.
    private int defaultMinuteInt(){
        int minute = (Calendar.getInstance().get(Calendar.MINUTE));
        if(minute<55){
            minute=minute+5;
        }
        else{
            minute=59;
        }
        return(minute);
    }

    // Returns the formatted input minute.
    private String formatMinute(int minute){
        String minuteString = Integer.toString(minute);
        if (minute<10){
            minuteString = "0"+minute;
        }
        return(minuteString);
    }

    // Returns AM/PM depending on the hour.
    private String timeAppend(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour>11){
            return(" PM");
        }
        else{
            return(" AM");
        }
    }

    // Returns AM/PM for the input hour.
    private String formatTimeAppend(int hour){
        if (hour>11){
            return(" PM");
        }
        else{
            return(" AM");
        }
    }

    // Starts the reminder.
    private void startReminder(Calendar c, String title) {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);

        // Reminder title and the reminder code are sent using an intent to the ReminderBroadcast class.
        intent.putExtra("description",title);
        intent.putExtra("ReminderCode",reminderCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminderCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        assert alarmManager != null;

        // Sets an alarm based on the repeat code.
        if (repeatCode==0) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
        else if (repeatCode==1){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 1000 * 60 * 60 * 24, pendingIntent);
        }
        else if (repeatCode==2){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 1000 * 60 * 60 * 24 * 7, pendingIntent);
        }

    }

    // Stops a reminder.
    private void stopReminder() {
        //ReminderBroadcast.notificationID=reminderCode;
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);
        //intent.putExtra("description",reminderTitle);
        //intent.putExtra("ReminderCode",reminderCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), reminderCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

    private void setReminderName(){
        reminderTitle = reminderName.getText().toString();
    }

    // Checks whether the input calendar contains the date for today.
    private boolean isToday(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    // Checks whether the input calendar contains the date for tomorrow.
    private boolean isTomorrow(Calendar currentCal){
        return currentCal.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)+1
                && currentCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                && currentCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
    }

    // Checks whether the input calendar contains the date for the past.
    private boolean isPast(Calendar currentCal){
        Calendar presentCal = Calendar.getInstance();
        presentCal.set(Calendar.SECOND,59);
        return currentCal.before(presentCal);
    }

    // Checks whether the input reminder name is valid (non-empty).
    private boolean isValidReminderName(){
        return reminderTitle.replaceAll("\\s", "").length() > 0;
    }

    // Creates a result intent which can be sent back to the main activity.
    private Intent createResultIntent(){
        Bundle resultBundle = new Bundle();
        getRepeatInfo();
        resultBundle.putString("ReminderTitle",reminderTitle);
        resultBundle.putString("ReminderDescriptionDate",date);
        resultBundle.putString("ReminderDescriptionTime",time);
        resultBundle.putString("ReminderDescriptionRepeat",repeat);
        resultBundle.putInt("Year",alarmDateTime.get(Calendar.YEAR));
        resultBundle.putInt("Month",alarmDateTime.get(Calendar.MONTH));
        resultBundle.putInt("Day",alarmDateTime.get(Calendar.DAY_OF_MONTH));
        resultBundle.putInt("Hour",alarmDateTime.get(Calendar.HOUR_OF_DAY));
        resultBundle.putInt("Minute",alarmDateTime.get(Calendar.MINUTE));
        resultBundle.putInt("ReminderCode",reminderCode);
        resultBundle.putInt("RepeatCode",repeatCode);
        if(isReminderEdit){
            resultBundle.putInt("Position",reminderPosition);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("Bundle",resultBundle);
        return resultIntent;
    }

    // Creates a delete intent which can be sent back to the main activity (when delete/back button is pressed).
    private Intent createDeleteIntent(){
        Bundle resultBundle = new Bundle();
        if(isReminderEdit){
            resultBundle.putInt("Position",reminderPosition);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("Bundle",resultBundle);
        return resultIntent;
    }

    // Retrieves values from the incoming bundle from the main activity.
    private void setIncomingDefaults(Bundle incBundle){
        reminderTitle = incBundle.getString("ReminderTitle");
        oldReminderTitle= incBundle.getString("ReminderTitle");

        reminderName.setText(reminderTitle);

        int year = incBundle.getInt("Year");
        int month = incBundle.getInt("Month");
        int day = incBundle.getInt("Day");
        int hour = incBundle.getInt("Hour");
        int minute = incBundle.getInt("Minute");
        int second = 0;

        reminderPosition = incBundle.getInt("Position");
        reminderCode = incBundle.getInt("ReminderCode");
        repeatCode = incBundle.getInt("RepeatCode");

        datePickerYear=year;
        datePickerMonth=month;
        datePickerDay=day;
        timePickerHour=hour;
        timePickerMinute=minute;
        alarmDateTime.set(year,month,day,hour,minute,second);
        oldAlarmDateTime.set(year,month,day,hour,minute,second);


        // Setting the time.
        time = formatHour(hour)+":"+formatMinute(minute)+formatTimeAppend(hour);
        setTimeBtn.setText(time);

        // Setting the date.
        date= day + "/" + (month+1) + "/" + year;
        if(isToday(alarmDateTime)){
            date=today;
        }
        else if (isTomorrow(alarmDateTime)){
            date=tomorrow;
        }
        setDateBtn.setText(date);

        // Setting the spinner.
        setRepeatSpinner.setSelection(repeatCode);
    }

    // Gets the repeat information which forms a part of the reminder description.
    public void getRepeatInfo(){
        repeatCode = setRepeatSpinner.getSelectedItemPosition();

        if(repeatCode==0){
            repeat =" ";
        }
        else if (repeatCode==1){
            repeat="Repeats Daily";
        }
        else {
            repeat="Repeats Weekly";
        }
    }

}
