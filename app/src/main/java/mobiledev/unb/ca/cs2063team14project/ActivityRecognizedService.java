package mobiledev.unb.ca.cs2063team14project;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by win10-ads on 2/26/2018.
 * Code adapted from https://code.tutsplus.com/tutorials/how-to-recognize-user-activity-with-activity-recognition--cms-25851
 */

public class ActivityRecognizedService extends IntentService {
    Waiter waiter= MainActivity.waiter;

    String TAG= "ARS";
    public ActivityRecognizedService(){
        super("ActivityRecognizedService");

    }
    public ActivityRecognizedService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent){
                Log.i(TAG,"onHandleIntent called");
        Log.i(TAG,"grrr"+MainActivity.getServiceActive());
        if(!MainActivity.getServiceActive()){
            Log.i(TAG,"Stopping");

            stopSelf();
        } else  if(ActivityRecognitionResult.hasResult(intent)&& MainActivity.getServiceActive()) {
            Log.i(TAG,"got an Activity and "+ MainActivity.getServiceActive());
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }else{
            Log.i(TAG,"didn't handle" + MainActivity.getServiceActive());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Log.i(TAG,"shouldRing is :" +MainActivity.getShouldRing());
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.i( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                    //Toast.makeText(getApplicationContext(),"In Vehicle:",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is in a vehicle, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.i( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    //Toast.makeText(getApplicationContext(),"On Bicycle:",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is On Bicycle, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.i( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    //Toast.makeText(,"On Foot:",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is On Foot, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.i( "ActivityRecogition", "Running: " + activity.getConfidence() );
                   // Toast.makeText(getApplicationContext(),"Running",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is Running, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.i( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    if( activity.getConfidence()>60) {
                        //Toast.makeText(getApplicationContext(),"Still",Toast.LENGTH_SHORT).show();
                        if(!MainActivity.getShouldRing()) {
                            Log.i(TAG,"Starting the waiting thread");
                            MainActivity.setShouldRing(true);
                            MainActivity.waiter = new ActivityRecognizedService.Waiter();
                            waiter=MainActivity.waiter;
                            waiter.execute();
                        }else{
                            Log.i(TAG,"Device hasn't moved");
                        }
                    }
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.i( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                   // Toast.makeText(getApplicationContext(),"Tilting",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is Tilting, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.i( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    //Toast.makeText(getApplicationContext(),"Walking",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"Device is Walking, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.i( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    //Toast.makeText(getApplicationContext(),"Unknown",Toast.LENGTH_SHORT).show();
                    if(activity.getConfidence()>60) {
                        if (MainActivity.getShouldRing()) {
                            Log.i(TAG,"No idea what the device is doing, closing thread before it rings");
                            boolean cancelSuccess=waiter.cancel(true);
                            Log.i(TAG,"cancel was:"+cancelSuccess);
                            MainActivity.setShouldRing(false);
                        }
                    }
                    break;
                }
            }
        }
    }



    public static class Waiter extends AsyncTask <Void,Integer,String>{
        String TAG="Waiter";
        @Override
        protected String doInBackground(Void...params){
            MainActivity.setShouldRing(true);
           try {
               Log.i(TAG,"Starting to wait");
               sleep(MainActivity.timerValue*1000);
               Log.i(TAG,"Done waiting");
                Log.i(TAG,"isCancelled from doInBack"+ isCancelled());
               while(MainActivity.shouldRing){
                   if(!MainActivity.mediaPlayer.isPlaying()){

                       MainActivity.audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                       MainActivity.audioManager.setStreamVolume(AudioManager.STREAM_RING,MainActivity.audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),AudioManager.FLAG_ALLOW_RINGER_MODES);
                       //audioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
                       MainActivity.audioManager.setSpeakerphoneOn(true);
                       AudioManager.OnAudioFocusChangeListener afChangeListner=new AudioManager.OnAudioFocusChangeListener() {
                           @Override
                           public void onAudioFocusChange(int focusChange) {

                           }
                       };
                       MainActivity.audioManager.requestAudioFocus(afChangeListner,AudioManager.STREAM_RING,AudioManager.AUDIOFOCUS_GAIN);
                       MainActivity.mediaPlayer.start();
                   }

               }
               if(MainActivity.mediaPlayer.isPlaying()){
                   MainActivity.mediaPlayer.pause();
                   MainActivity.mediaPlayer.seekTo(0);

               }


           }catch(Exception e){
                Log.i(TAG,e.getMessage());
               //stopSelf();
           }
           return "derp";
        }
        @Override
        protected  void onPostExecute(String unsed){
            Log.i(TAG,"onPostExecuteCalled isCancelled returns:" + isCancelled());
            if(!isCancelled()){
                Log.i(TAG,"Make notification");

                try {
                   /* NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setContentText("Please work");
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("Hello");
                    NotificationManagerCompat.from(getApplicationContext()).notify(0, builder.build());*/
                }
                catch(Exception e){
                    Log.i(TAG,"Problem is:"+e.getMessage());
                }
            }else{
                Log.i(TAG,"Was cancel Yay");
            }
            //Toast.makeText(getApplicationContext(),"Ring ring",Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled(String unsed){
            Log.i(TAG,"onCancelled called :D");
            //stopSelf();
        }

    }


}
