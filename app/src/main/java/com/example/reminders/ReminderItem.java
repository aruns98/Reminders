package com.example.reminders;

public class ReminderItem {
    private String reminderTitle;
    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;
    private int reminderHour;
    private int reminderMinute;
    private int reminderCode;
    private int reminderRepeatCode;
    private String reminderDescriptionDate;
    private String reminderDescriptionTime;
    private String reminderDescriptionRepeat;

    public ReminderItem(String newReminderTitle,
                        int newReminderYear,
                        int newReminderMonth,
                        int newReminderDay,
                        int newReminderHour,
                        int newReminderMinute,
                        int newReminderCode,
                        int newReminderRepeatCode,
                        String newReminderDescriptionDate,
                        String newReminderDescriptionTime,
                        String newReminderDescriptionRepeat){

        reminderTitle=newReminderTitle;
        reminderYear=newReminderYear;
        reminderMonth=newReminderMonth;
        reminderDay=newReminderDay;
        reminderHour=newReminderHour;
        reminderMinute=newReminderMinute;
        reminderCode=newReminderCode;
        reminderRepeatCode= newReminderRepeatCode;
        reminderDescriptionDate=newReminderDescriptionDate;
        reminderDescriptionTime=newReminderDescriptionTime;
        reminderDescriptionRepeat=newReminderDescriptionRepeat;
    }

    public int getWhiteDotImage(){
        return R.drawable.ic_white_dot;
    }

    public String getReminderTitle(){
        return reminderTitle;
    }

    public int getReminderYear(){return reminderYear;}

    public int getReminderMonth(){return reminderMonth;}

    public int getReminderDay(){return reminderDay;}

    public int getReminderHour(){return reminderHour;}

    public int getReminderMinute(){return reminderMinute;}

    public int getReminderCode(){return reminderCode;}

    public int getReminderRepeatCode(){return reminderRepeatCode;}

    public String getReminderDescriptionDate(){return reminderDescriptionDate;}

    public String getReminderDescriptionTime(){return reminderDescriptionTime;}

    public String getReminderDescriptionRepeat(){return reminderDescriptionRepeat;}
}
