package com.example.spencer_symington.a16bitsoundtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

public class Pop extends Activity {
    private SeekBar droneVolumeControl;
    private int droneVolume;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drone_settings);


        //getWindow().setLayout((int)(width*0.5f),(int)(height*0.5f));
        droneVolumeControl = (SeekBar)findViewById(R.id.drone_vol_control);
        droneVolumeControl.setMax(50);
        droneVolumeControl.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;
                        //tempoText.setText("" + progressValue);
                        //playButton.setText("" + progressValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        //tempoText.setText("" + progressValue);
                        //playButton.setText("" + progressValue);
                        //duration = (double)(60.0 /progressValue);
                        droneVolume = progressValue;
                        Log.d("assmass", "drone volume = " + droneVolume);



                    }
                }
        );
    }
    public void CloseWindow(View v){
        Log.d("assmass", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        intent.putExtra("drone_volume",droneVolume);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        finish();
    }

}
