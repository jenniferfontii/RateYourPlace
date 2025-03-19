package com.example.rateyourplace;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseInstanceService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCM Message", "Message data payload: " +
                    remoteMessage.getData());
        }
        if (remoteMessage.getNotification() != null) {
            Log.d("FCM Message", "Message Notification Body: " +
                    remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage);
        }
    }

    private void handleNotification(RemoteMessage remoteMessage) {
        // Build a notification with the received message details
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(getApplicationContext(), "firebase")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        // Check for notification permission before displaying
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Display the notification
        notificationManager.notify(1, builder.build());
    }
}
