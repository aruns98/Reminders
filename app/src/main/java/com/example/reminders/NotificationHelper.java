package com.example.reminders;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    private static final String channelID = "channelID";
    private static final String channelName = "Reminders";
    private String reminderDesc;
    private NotificationManager mManager;


    public NotificationHelper(Context base, String reminderDescription) {
        super(base);
        reminderDesc=reminderDescription;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }
    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
    public NotificationCompat.Builder getChannelNotification() {
        // No content if oreo or above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setContentTitle(reminderDesc)
                    .setSmallIcon(R.drawable.ic_reminderlogo)
                    .setColor(Color.parseColor("#282524"));
        }
        // Title + content present if below oreo.
        else{
            return new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setContentTitle("Reminder:")
                    .setContentText(reminderDesc)
                    .setSmallIcon(R.drawable.ic_reminderlogo)
                    .setColor(Color.parseColor("#282524"));
        }
    }
}
