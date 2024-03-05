package me.t3sl4.audiohandler;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;

public class AudioHandler {
    private Context context;
    private ActivityResultCallback activityResultCallback;

    private boolean isRecording = false;

    public final static String ACTION_STOP = "me.t3sl4.audiohandler.stop";
    public static String BROADCAST_EXTRA_DATA = "me.t3sl4.audiohandler.waveform_data";
    public static String BROADCAST_WAVEFORM = "me.t3sl4.audiohandler.waveform";

    public AudioHandler(Context context) {
        this.context = context;

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, makeIntentFilter());
    }

    public interface ActivityResultCallback {
        void startActivityForResult(Intent intent, int requestCode);
    }

    public void setActivityResultCallback(ActivityResultCallback callback) {
        this.activityResultCallback = callback;
    }

    public void createHelloWorld() {
        Toast.makeText(context, "Hello World", Toast.LENGTH_SHORT).show();
    }

    public void stopService() {
        final Intent broadcast = new Intent(ACTION_STOP);
        context.sendBroadcast(broadcast);
        isRecording = false;
    }

    public void startService() {
        if (!isRecording) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Burada kullanıcıdan izin isteme işlemini tetikleyin.
                // Bu, Activity üzerinden yapılmalıdır.
            } else {
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mediaProjectionManager != null && activityResultCallback != null) {
                    Intent intent = mediaProjectionManager.createScreenCaptureIntent();
                    activityResultCallback.startActivityForResult(intent, 2000);
                }
            }
        }
    }

    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            final byte[] audioData = intent.getByteArrayExtra(BROADCAST_EXTRA_DATA);

            Log.d("audioData", Arrays.toString(audioData));
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_WAVEFORM);

        return intentFilter;
    }
}