package mobiledev.unb.ca.cs2063team14project;

import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;

import java.io.IOException;
import java.net.URI;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    static Button startButton;
    Button settingButton;
    protected static boolean serviceActive;
    protected static boolean shouldRing;
    static GoogleApiClient apiClient;
    static String TAG ="Main Activity";
    final int MY_PERMISSIONS_MODIFY_AUDIO_SETTINGS = 12;
    protected static PendingIntent pendingIntent;
    protected static PendingIntent pendingIntentARS;
    protected static MediaPlayer mediaPlayer;
    protected static ActivityRecognizedService.Waiter waiter;
    protected volatile static int timerValue;
    final static String PREFTAG="CS2063Project";
    protected static Uri uri;
    protected static AudioManager audioManager;
    NotificationManager mNotificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceActive=false;
        startButton=(Button) findViewById(R.id.startButton);
        startButton.setBackgroundColor(Color.GREEN);
        settingButton=(Button) findViewById(R.id.settingButton);
        Log.i(TAG,"Value of timerValue "+timerValue);
        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences myPrefs=getSharedPreferences(PREFTAG,0);
        String uriString=myPrefs.getString("uri","Nothing saved");
        timerValue=myPrefs.getInt("TIMER",45);
        if(uriString.equals("Nothing saved")){
            Log.i(TAG,"No uri were saved");
            uri= RingtoneManager.getValidRingtoneUri(getApplicationContext());
            if(uri==null){
                Toast.makeText(getApplicationContext(),"I see",Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.i(TAG,"an uri was saved");
            uri= Uri.parse(uriString);
        }

        askForPermission();
        NotificationManager notificationManage =(NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(!notificationManage.isNotificationPolicyAccessGranted()){
            Toast.makeText(getApplicationContext(),"We need \"Do not disturb\" access to ring if your phone is on do not disturb",Toast.LENGTH_LONG).show();
            Intent intent=new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        //audioManager.setStreamVolume(AudioManager.STREAM_RING,audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),AudioManager.FLAG_ALLOW_RINGER_MODES);
        //audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
        audioManager.setSpeakerphoneOn(true);
        AudioManager.OnAudioFocusChangeListener afChangeListner=new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

            }
        };
        audioManager.requestAudioFocus(afChangeListner,AudioManager.STREAM_RING,AudioManager.AUDIOFOCUS_GAIN);
        mediaPlayer= new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        try{
            mediaPlayer.setDataSource(getApplicationContext(),uri);
            mediaPlayer.prepareAsync();
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                if(shouldRing){
                    mp.start();
                }else {
                    mp.pause();
                    mp.seekTo(0);

                }
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"Start Button Pressed");
                shouldRing=false;



                if(serviceActive){
                    Intent intent2 = new Intent(getApplicationContext(), ActionReceiver.class);
                    intent2.putExtra("action","start");
                    PendingIntent pIntent = pendingIntent.getBroadcast(getApplicationContext(), 1, intent2, pendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("CS2063 Final Project")
                                    .setContentIntent(pIntent)
                                    /*.setContentIntent(pIntent3)*/
                                    .setAutoCancel(false)
                                    .addAction(android.R.drawable.ic_menu_compass, "start", pIntent)
                                    /*.addAction(android.R.drawable.ic_menu_compass, "snooze", pIntent3)*/
                                    .setContentText("Service is stoped");
                    mNotificationManager =
                            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());

                    stopService();
                }else{
                    Intent intent2 = new Intent(getApplicationContext(), ActionReceiver.class);
                    intent2.putExtra("action","stop");
                    PendingIntent pIntent = pendingIntent.getBroadcast(getApplicationContext(), 1, intent2, pendingIntent.FLAG_UPDATE_CURRENT);
                    //Intent intent3 = new Intent(getApplicationContext(), ActionReceiver.class);
                    //intent3.putExtra("action","snooze");
                   // PendingIntent pIntent3 = pendingIntent.getBroadcast(getApplicationContext(), 1, intent3, pendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("CS2063 Final Project")
                                    .setContentIntent(pIntent)
                                    //.setContentIntent(pIntent3)
                                    .setAutoCancel(false)
                                    .addAction(android.R.drawable.ic_menu_compass, "stop", pIntent)
                                    //.addAction(android.R.drawable.ic_menu_compass, "snooze", pIntent3)
                                    .setContentText("Service is running");
                    mNotificationManager =
                            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(1, mBuilder.build());
                    Log.i(TAG, "NOTIFICATION START BUTTON PRESSED");
                    startService();
                }

            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        pendingIntentARS= PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    @Override
    protected void onDestroy(){

        Log.i(TAG, "onDestroy called");
        mNotificationManager.cancelAll();
        if(mediaPlayer!=null)mediaPlayer.release();
        try {
            stopService();
        }catch (IllegalStateException e){

        }



        super.onDestroy();
    }

    synchronized  static public boolean getShouldRing(){return shouldRing;}
    synchronized static public void setShouldRing(boolean value){shouldRing=value;}
    synchronized static public boolean getServiceActive(){
        return serviceActive;
    }

    protected static void startService(){
        shouldRing=false;
        serviceActive=true;
        startButton.setText("Stop");
        startButton.setBackgroundColor(Color.RED);
        //TODO start the service
        apiClient.connect();
        Log.i(TAG, "Service Start");
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }

    }

    protected static void stopService(){
        serviceActive=false;
        startButton.setText("Start");
        startButton.setBackgroundColor(Color.GREEN);

        //TODO stop the service
        Log.i(TAG,"ShouldRing value is="+shouldRing);
        if(true){//used to check for shouldRing
            try {
                Log.i(TAG, "cancel called from stop");
                waiter.cancel(true);
                setShouldRing(false);
            }catch(Exception e){
                Log.i(TAG,"Caught this exeption:" +e.getMessage());
            }
        }
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(apiClient,pendingIntentARS);
        apiClient.disconnect();
        Log.i(TAG, "stop service");
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }

    }
    protected static void snooze(){
        setShouldRing(false);
        if(mediaPlayer.isPlaying()){
            waiter.cancel(true);
            mediaPlayer.pause();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle){

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( apiClient, 1000, pendingIntentARS );
        Log.i(TAG,"Update requested");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void askForPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"don't have permission");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                Log.i(TAG,"Asking for permission with explanation");
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                        MY_PERMISSIONS_MODIFY_AUDIO_SETTINGS);

            } else {
                Log.i(TAG,"Asking for permission without explanation");
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                        MY_PERMISSIONS_MODIFY_AUDIO_SETTINGS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Log.i(TAG,"Already had permission");
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_MODIFY_AUDIO_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG,"Permission granted");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }



}
