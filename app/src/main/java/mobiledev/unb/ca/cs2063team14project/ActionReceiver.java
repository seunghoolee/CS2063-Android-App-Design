package mobiledev.unb.ca.cs2063team14project;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.media.Ringtone;
import android.widget.Button;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;
import static mobiledev.unb.ca.cs2063team14project.MainActivity.serviceActive;
import static mobiledev.unb.ca.cs2063team14project.R.id.startButton;

/**
 * Created by win10-ads on 3/30/2018.
 */

public class ActionReceiver extends BroadcastReceiver {
    Button startButton;
    PendingIntent pendingIntent;
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getStringExtra("action");
        Log.i(TAG,action);
        if(action.equals("stop"))
        {
            Log.i(TAG, "NOTIFICATION STOP BUTTON PRESSED");
            Intent intent2 = new Intent(context, ActionReceiver.class);
            intent2.putExtra("action","start");
            PendingIntent pIntent = pendingIntent.getBroadcast(context, 1, intent2, pendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("CS2063 Final Project")
                            .setContentIntent(pIntent)
                                    /*.setContentIntent(pIntent3)*/
                            .setAutoCancel(false)
                            .addAction(android.R.drawable.ic_menu_compass, "start", pIntent)
                                    /*.addAction(android.R.drawable.ic_menu_compass, "snooze", pIntent3)*/
                            .setContentText("Service is stoped");
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());


            MainActivity.stopService();
            //change button if using notification
        }

        if(action.equals("start"))
        {
            Intent intent2 = new Intent(context, ActionReceiver.class);
            intent2.putExtra("action","stop");
            PendingIntent pIntent = pendingIntent.getBroadcast(context, 1, intent2, pendingIntent.FLAG_UPDATE_CURRENT);
           // Intent intent3 = new Intent(context, ActionReceiver.class);
            //intent3.putExtra("action","snooze");
            //PendingIntent pIntent3 = pendingIntent.getBroadcast(context, 1, intent3, pendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("CS2063 Final Project")
                            .setContentIntent(pIntent)
                                   // .setContentIntent(pIntent3)
                            .setAutoCancel(false)
                            .addAction(android.R.drawable.ic_menu_compass, "stop", pIntent)
                                  //  .addAction(android.R.drawable.ic_menu_compass, "snooze", pIntent3)
                            .setContentText("Service is running");
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
            Log.i(TAG, "NOTIFICATION START BUTTON PRESSED");
            MainActivity.startService();
            //change button if using notification
        }
        if(action.equals("snooze")){

            MainActivity.snooze();
        }

        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);

    }
}
