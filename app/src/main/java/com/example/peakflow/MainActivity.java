package com.example.peakflow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Boolean isblowing;
    List<Integer> peakValues = new ArrayList<Integer>();
    int maxPeak;
    boolean muteAudio = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }


    @SuppressLint("SetTextI18n")
    public void recordBlow(View view) {

        if (!muteAudio) {
            Log.d("Debug", "Audio has been muted");
            AssetFileDescriptor afd = null;
            try {
                afd = getAssets().openFd("blowInstruction.mp3");
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaPlayer player = new MediaPlayer();
            try {
                player.setDataSource(Objects.requireNonNull(afd).getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.start();
        }

        boolean recorder = true;
        int minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        short[] buffer = new short[minSize];
        ar.startRecording();
        while (recorder) {
            ar.read(buffer, 0, minSize);
            for (short s : buffer) {
                if (Math.abs(s) > 27000)   //DETECT VOLUME (IF I BLOW IN THE MIC)
                {
                    int blow_value = Math.abs(s);
                    Log.d("debug", "blown Value: " + blow_value);
                    peakValues.add(blow_value);
                    setContentView(R.layout.activity_main);
                    ar.stop();
                    recorder = false;
                    isblowing = true;
                }
            }
        }
        isblowing = false;
        Log.d("DEBUG", "Blowing not detected!");
        Log.d("Debug", String.valueOf(peakValues));
        maxPeak = Collections.max(peakValues);


        String displayValue = String.valueOf(maxPeak);
        TextView blowValue = findViewById(R.id.bValue);
        blowValue.setText(displayValue.substring(0, displayValue.length() - 2));

        int peekScaleNumber = Integer.parseInt(Integer.toString(maxPeak).substring(0, 1));


        Log.d("Debug", "Peek Scale Value: " + peekScaleNumber);
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(peekScaleNumber + 2);
        peakValues.clear();
    }
}
