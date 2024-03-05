package me.t3sl4.audiocapture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.t3sl4.audiohandler.Recording.RecordingService;
import me.t3sl4.audiocapture.Visualizer.SoundWaveView;
import me.t3sl4.audiohandler.AudioHandler;

public class MainActivity extends AppCompatActivity implements AudioHandler.ActivityResultCallback {
    private AudioHandler audioHandler;

    private Button stopRecording;
    private SoundWaveView audioVisualizerView;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioVisualizerView = findViewById(R.id.visualizer);
        stopRecording = findViewById(R.id.stopRecording);

        audioHandler = new AudioHandler(this);
        audioHandler.setActivityResultCallback(this);
        audioHandler.createHelloWorld();

        audioHandler.startService();

        stopRecording.setOnClickListener(v -> {
            if(isRecording) {
                audioHandler.stopService();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 2000) {
            if (data != null) {
                Intent intent = new Intent(this, RecordingService.class);
                intent.putExtra(RecordingService.EXTRA_CODE, resultCode);
                intent.putExtra(RecordingService.EXTRA_DATA, data);

                ContextCompat.startForegroundService(this, intent);

                isRecording = true;
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(audioHandler.broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRecording) {
            audioHandler.startService();
        }
    }
}