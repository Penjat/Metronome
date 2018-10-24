package com.example.spencer_symington.a16bitsoundtest;


import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;

import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "assmass";

    private SeekBar tempoControl;


    private Button playButton;
    private LinearLayout con;

    private byte[] metronomeSound;
    private AudioTrack track;
    private MediaPlayer dronePlayer;
    private int droneVolume;
    private int clickVolume;
    private double duration = 1;


    private int maxBeats = 11;
    private int numBeats = 4;
    private BeatButton[] buttonArray;

    private int playbackPosition = 0;
    byte[] click1;
    byte[] click2;
    private boolean isPlaying = false;
    private boolean droneIsPlaying = false;

    private final int SAMPLE_SIZE = 44100;
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_SIZE,
            channelConfig, audioFormat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_main);


        //setTitle("Best Metronome");
        LoadClicks();
        SetUp();
        GetElements();
        CreateSound();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            int vol = intent.getIntExtra("drone_volume",25);
            SetDroneVolume(vol);
            Log.d("assmass", "Got message: " + message);
        }
    };
    public void SetUp(){
        Log.d(TAG,"setting up.");
        buttonArray = new BeatButton[maxBeats];
        con = (LinearLayout) findViewById(R.id.llCon);
        for(int i=0;i<maxBeats;i++) {

            final BeatButton button = new BeatButton(this);
            if(i==0){
                button.SetBeatType(0);
            }else{
                button.SetBeatType(1);
            }

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //beat.BeatPressed();
                    Log.d(TAG, "I was pressed");
                    button.pressed(v);
                }
            });
            //button.setBackgroundResource(R.drawable.reg);


            //con.setWeightSum(numBeats);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
            p.setMargins(2,0,2,0);
            button.setLayoutParams(p);
            //blinkText = (TextView)findViewById(R.id.tBlink);
            button.setText("" + (i+1));
            button.setTypeface(null, Typeface.BOLD);



            con.addView(button);

            buttonArray[i] = button;
            button.UpdateAppearance();
        }
        UpdateBeats();
    }



    public void UpdateBeats(){
        LinearLayout con = (LinearLayout) findViewById(R.id.llCon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(con);
        }
        con.setWeightSum(numBeats);
        for(int i=0;i<maxBeats;i++){
            if(i>= numBeats){
                buttonArray[i].setVisibility(View.GONE);
            }else{
                buttonArray[i].setVisibility(View.VISIBLE);
            }
        }
        if(isPlaying) {
            //only create sound if already playing
            CreateSound();
        }
    }

    public void GetElements(){
        playButton = (Button)findViewById(R.id.bPlay);
        tempoControl = (SeekBar)findViewById(R.id.tempoControl);
       // tempoText = (TextView)findViewById(R.id.tTempo);
       // tempoText.setText("" + tempoControl.getProgress());

        tempoControl.setMax(570);
        tempoControl.setProgress(60);
        playButton.setText("60");
       // tempoText.setText("60");

        tempoControl.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress + 30;
                        //tempoText.setText("" + progressValue);
                        playButton.setText("" + progressValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        //tempoText.setText("" + progressValue);
                        playButton.setText("" + progressValue);
                        duration = (double)(60.0 /progressValue);
                        Log.d(TAG, "duration = " + duration);
                        CreateSound();


                    }
                }
        );
    }

    public void LoadClicks(){
        //TODO: FIX FOR DIFFERENT TEMPOS
        InputStream isClick1 = getResources().openRawResource(R.raw.md_block);
        InputStream isClick2 = getResources().openRawResource(R.raw.wood_block_click);
        click1 = new byte[(int)(SAMPLE_SIZE*duration)*4];
        click2 = new byte[(int)(SAMPLE_SIZE*duration)*4];
        try {
            isClick1.read(click1);
        } catch (IOException e) {
            e.printStackTrace();
        }finally
        {
            try {
                isClick1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            isClick2.read(click2);
        } catch (IOException e) {
            e.printStackTrace();
        }finally
        {
            try {
                isClick2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void CreateSound() {
        if(isPlaying){

            track.stop();

        }
        InputStream isClick1 = getResources().openRawResource(R.raw.md_block);
        InputStream isClick2 = getResources().openRawResource(R.raw.wood_block_click);
        byte[] clickSound1 = new byte[(int)(SAMPLE_SIZE*duration)*4];
        byte[] clickSound2 = new byte[(int)(SAMPLE_SIZE*duration)*4];

        //Arrays.fill(clickSound1,Byte.MIN_VALUE);
        //Arrays.fill(clickSound2,Byte.MIN_VALUE);
        try {
            isClick1.skip(44);
            isClick1.read(clickSound1);
        } catch (IOException e) {
            e.printStackTrace();
        }finally
        {
            try {
                isClick1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            isClick2.skip(44);
            isClick2.read(clickSound2);
        } catch (IOException e) {
            e.printStackTrace();
        }finally
        {
            try {
                isClick2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, (int)(SAMPLE_SIZE*duration)*4*numBeats,
                AudioTrack.MODE_STATIC);

        byte[] ff = new byte[(int)(SAMPLE_SIZE*duration)*4*numBeats];


        for(int i=0;i<numBeats;i++) {


            if(buttonArray[i].GetBeatType() == 0){
                int writePos = (int)(SAMPLE_SIZE*duration)*4*i;
                System.arraycopy(clickSound2, 0, ff, writePos, (int)(SAMPLE_SIZE*duration)*4);
            }else if(buttonArray[i].GetBeatType() == 1){
                int writePos = (int)(SAMPLE_SIZE*duration)*4*i;
                System.arraycopy(clickSound1, 0, ff, writePos, (int)(SAMPLE_SIZE*duration)*4);
            }



        }
        track.write(ff, 0, ff.length);

        track.setLoopPoints (0,
                (int)(SAMPLE_SIZE*duration)*numBeats,
                -1);

        track.setPositionNotificationPeriod((int)(SAMPLE_SIZE*duration)*numBeats);

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // nothing to do
                Log.d(TAG, "click");
                SetBlinks();
            }
            @Override
            public void onMarkerReached(AudioTrack track) {

                //messageHandler.sendMessage(messageHandler.obtainMessage(PLAYBACK_END_REACHED));
            }
        });

        if(isPlaying){
            track.play();
            SetBlinks();
        }
    }
    public void CreateSound2(){
        if(isPlaying){

            track.stop();

        }

         // command line: wc -c click.wav

        //assert WAV_FILE_BYTE_SIZE > minBufferSize;

        int bufferSize = (int)(4*SAMPLE_SIZE*duration);// sampleSize*2Channels*2 for bit depth

        //TODO: maybe don't need to recreate this each time
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, (int)(SAMPLE_SIZE*duration)*4*numBeats,
                AudioTrack.MODE_STATIC);

        metronomeSound = new byte[(int)(SAMPLE_SIZE*duration)*4*numBeats];




        for(int i=0;i<numBeats;i++) {
            int beatType = buttonArray[i].GetBeatType();
            if(beatType==0) {
                AddClickSound(i, click2);
            }else{
                AddClickSound(i, click1);
            }
        }

        track.write(metronomeSound, 0, metronomeSound.length);

        track.setLoopPoints (0,
                (int)(SAMPLE_SIZE*duration)*numBeats,
                -1);

        track.setPositionNotificationPeriod((int)(SAMPLE_SIZE*duration)*numBeats);

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                // nothing to do
                Log.d(TAG, "click");
                SetBlinks();
            }
            @Override
            public void onMarkerReached(AudioTrack track) {

                //messageHandler.sendMessage(messageHandler.obtainMessage(PLAYBACK_END_REACHED));
            }
        });

        if(isPlaying){
            track.play();
            SetBlinks();
        }
    }

    public void AddClickSound(int pos,byte[] buffer){
        for(int i=44;i<(int)(SAMPLE_SIZE*duration)*4;i++){
            int writePos = i-44 + (int)(SAMPLE_SIZE*duration)*4*pos;//times 4 for 2 Channels and 16 bit
            if(i < buffer.length){
                metronomeSound[writePos] = buffer[i];
            }else {
                metronomeSound[writePos] = Byte.MIN_VALUE;
            }
        }

    }

    public void SetBlinks(){
        final LinearInterpolator interpolator = new LinearInterpolator();
        int animationTime = (int)(duration*1000);
        for(int i=0;i<numBeats;i++){
            BeatButton beat = buttonArray[i];
            int animRef = R.anim.blinkanim;
            if(beat.GetBeatType()!=0){
                animRef = R.anim.blink_anim_offbeat;
            }

            Animation a = AnimationUtils.loadAnimation(this, animRef);
            a.setStartOffset((int)(duration*i*1000));
            a.setDuration(animationTime);

            buttonArray[i].startAnimation(a);



        }
    }

    public void TogglePlay(View v){
        isPlaying = !isPlaying;
        if(isPlaying) {
            Log.d(TAG, "starting click");

            AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)getResources().getDrawable(R.drawable.play_button);
            playButton.setBackground(drawable);
            drawable.start();
            Log.d(TAG,"animating");

            CreateSound();
            track.setLoopPoints (0,
                    (int)(SAMPLE_SIZE*duration)*numBeats,
                    -1);
            track.play();
            SetBlinks();
            return;
        }
        Log.d(TAG, "stoping click");
        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)getResources().getDrawable(R.drawable.stop_button);
        playButton.setBackground(drawable);
        drawable.start();
        Log.d(TAG,"animating");
        track.stop();
        StopBlinks();


    }

    public void StopBlinks(){
        for(int i=0;i<numBeats;i++){

            BeatButton b = buttonArray[i];
            b.clearAnimation();
        }
    }

    public void AddBeat(View v){
        numBeats++;
        if(numBeats > maxBeats){
            numBeats = maxBeats;
            return;
        }
        UpdateBeats();

    }
    public void SubBeat(View v){
        numBeats--;
        if(numBeats < 1){
            numBeats = 1;
            return;
        }
        UpdateBeats();
    }

    public class BeatButton extends Button {
        private int pos;//it position in the beat array
        private int beatType = 0;

        public BeatButton(Context context) {
            super(context);
        }
        public void SetBeatType(int i){
            beatType = i;
        }
        public int GetBeatType(){
            return beatType;
        }

        public void UpdateAppearance(){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition((ViewGroup) con);
            }
            if(beatType == 0) {
                this.setBackgroundResource(R.drawable.on_beat);
            }else if(beatType == 1) {
                this.setBackgroundResource(R.drawable.off_beat);
            }else{
                this.setBackgroundResource(R.drawable.empty_beat);
            }
            if(isPlaying){
                CreateSound();
            }
        }
        public void pressed(View v){
            beatType--;
            if(beatType == -1){
                beatType = 2;
            }
            UpdateAppearance();

        }




    }

    public void OpenDroneSettings(View v){

        Intent intent = new Intent(MainActivity.this,Pop.class);
        intent.putExtra("message", "This message comes from ur mom");
        startActivity(intent);
        //startActivity(new Intent(MainActivity.this,Pop.class));

    }
    public void PlayDrone(View view){
        droneIsPlaying = !droneIsPlaying;
        if(droneIsPlaying) {
            dronePlayer = MediaPlayer.create(this, R.raw.drone_a);
            dronePlayer.setLooping(true);
            dronePlayer.start();
            return;
        }
        dronePlayer.stop();

    }
    public void SetDroneVolume(int vol){
        int maxVolume = 50;
        droneVolume = vol;
        float log1=(float)(Math.log(maxVolume-droneVolume)/Math.log(maxVolume));
        dronePlayer.setVolume(1-log1,1-log1);
    }

}
