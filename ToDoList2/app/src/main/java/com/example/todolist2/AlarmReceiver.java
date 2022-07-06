package com.example.todolist2;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String taskTitle = intent.getStringExtra("notificationTaskTitle");
        int taskId = intent.getIntExtra("notificationTaskId", -1);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra("NotificationStartup", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, taskId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "pinkpantherchannel")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setContentTitle("Pink Panther Reminder")
                .setContentText("One of your tasks is ending soon: " + taskTitle)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(100 + taskId, builder.build());

    }
}
