package mobiledev.unb.ca.cs2063team14project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

public class SettingActivity extends AppCompatActivity {

    Button saveButton;
    protected static Button ringtoneButton;
    Spinner timerPicker;
    Boolean shouldToast;
    int code;
    String TAG = "Settings";
    protected static Ringtone ringtone;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sharedPreferences=getSharedPreferences(MainActivity.PREFTAG,0);
        sharedPreferencesEditor=sharedPreferences.edit();
        saveButton = (Button) findViewById(R.id.saveButton);
        ringtoneButton = (Button) findViewById(R.id.ringtoneButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO implement this
            }
        });
        code=1;
        shouldToast=false;
        ringtone=RingtoneManager.getRingtone(getApplicationContext(),MainActivity.uri);
        if(ringtone.getTitle(getApplicationContext()).equals(RingtoneManager.getRingtone(getApplicationContext(), Settings.System.DEFAULT_RINGTONE_URI).getTitle(getApplicationContext())))
        {
            ringtoneButton.setText("Ringtone: Default");
        }else{
            ringtoneButton.setText("Ringtone: " + ringtone.getTitle(getApplicationContext()));
        }
        ringtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO implement this
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_RINGTONE);
                startActivityForResult(intent,code);
                RingtoneManager ringtoneManager = new RingtoneManager(getApplicationContext());
                //MainActivity.ringtone= ringtoneManager.getRingtone());
            }
        });

        timerPicker =(Spinner) findViewById(R.id.spinner2);
        String[] times = new String[]{"5 seconds","30 seconds","45 seconds","1 min","3 min", "5 min", "10 min", "15 min"};
        ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,times);
        timerPicker.setAdapter(spinnerAdapter);
        Log.i(TAG, "Value of timerValue: "+MainActivity.timerValue);
        switch(MainActivity.timerValue){
            case 5:
                Log.i(TAG,"Got Called: "+MainActivity.timerValue);
                timerPicker.setSelection(0);
                break;
            case 30:
                Log.i(TAG,"Got Called: "+MainActivity.timerValue);
                timerPicker.setSelection(1);
                break;
            case 45:
                Log.i(TAG,"Got Called: "+MainActivity.timerValue);
                timerPicker.setSelection(2);
                break;
            case 60:
                Log.i(TAG,"Got Called: "+MainActivity.timerValue);
                timerPicker.setSelection(3);
                break;
            case 180:
                Log.i(TAG,"Got Called: "+MainActivity.timerValue);
                timerPicker.setSelection(4);
                break;
            case 300:
                timerPicker.setSelection(5);
                break;
            case 600:
                timerPicker.setSelection(6);
                break;
            case 900:
                timerPicker.setSelection(7);
                break;
            default:
                Log.i(TAG,"default");
        }


        timerPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value= adapterView.getItemAtPosition(i).toString();
                switch(value){
                    case "5 seconds":
                        Log.i(TAG,"Setting the timeer value to 5");
                        MainActivity.timerValue=5;
                        sharedPreferencesEditor.putInt("TIMER",5);
                        break;
                    case "30 seconds":
                        MainActivity.timerValue=30;
                        sharedPreferencesEditor.putInt("TIMER",30);
                        break;
                    case "45 seconds":
                        MainActivity.timerValue=45;
                        sharedPreferencesEditor.putInt("TIMER",45);
                        break;
                    case "1 min":
                        MainActivity.timerValue=60;
                        sharedPreferencesEditor.putInt("TIMER",60);
                        break;
                    case "3 min":
                        MainActivity.timerValue=60*3;
                        sharedPreferencesEditor.putInt("TIMER",60*3);
                        break;
                    case "5 min":
                        MainActivity.timerValue=60*5;
                        sharedPreferencesEditor.putInt("TIMER",60*5);
                        break;
                    case "10 min":
                        MainActivity.timerValue=60*10;
                        sharedPreferencesEditor.putInt("TIMER",60*10);
                        break;
                    case "15 min":
                        MainActivity.timerValue=60*15;
                        sharedPreferencesEditor.putInt("TIMER",60*15);
                        break;
                    default:
                        Log.i(TAG,"Default from setter");
                }
                if(shouldToast) {
                    Toast.makeText(getApplicationContext(), "Selected :" + adapterView.getItemAtPosition(i), Toast.LENGTH_SHORT).show();
                }else{
                    shouldToast=true;
                }
                sharedPreferencesEditor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(),"Derp",Toast.LENGTH_SHORT).show();
            }
        });
        Log.i(TAG,""+MainActivity.timerValue);

    }
    private class RingtoneGetter extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... uri) {

            return uri[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(MainActivity.uri==null)Log.i(TAG,"data is null");
            MainActivity.mediaPlayer.reset();
            MainActivity.mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            try{
                MainActivity.mediaPlayer.setDataSource(getApplicationContext(),MainActivity.uri);
                MainActivity.mediaPlayer.prepareAsync();
            }catch (IOException e){
                e.printStackTrace();
            }
            MainActivity.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp){
                    if(MainActivity.shouldRing){
                        mp.start();
                    }else {
                        mp.stop();
                        mp.release();
                    }
                }
            });
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), MainActivity.uri);


            sharedPreferencesEditor.putString("uri",MainActivity.uri.toString());
            sharedPreferencesEditor.apply();
            SettingActivity.ringtoneButton.setText("Ringtone: " + ringtone.getTitle(getApplicationContext()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(data==null)Log.i(TAG,"intent is null");
        if(requestCode==code&& resultCode==RESULT_OK){
            Uri test=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if(test==null) {
                Toast.makeText(getApplicationContext(),"Please select a valid ringtone",Toast.LENGTH_SHORT).show();
            }else{
                MainActivity.uri = test;

                RingtoneGetter ringtoneGetter = new RingtoneGetter();
                ringtoneGetter.execute(data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString());
            }
        }
    }
}
