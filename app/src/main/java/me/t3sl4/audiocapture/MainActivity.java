package me.t3sl4.audiocapture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.t3sl4.audiocapture.Recording.RecordingService;
import me.t3sl4.audiocapture.Visualizer.SoundWaveView;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static me.t3sl4.audiocapture.Recording.RecordingService.ACTION_STOP;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {
    private final static boolean DEBUG = true;
    public static String BROADCAST_WAVEFORM = "me.t3sl4.audiocapture.waveform";
    public static String BROADCAST_EXTRA_DATA = "me.t3sl4.audiocapture.waveform_data";
    private SoundWaveView audioVisualizerView;
    private boolean isRecording = false;

    private Button stopRecording;

    private static final String CLIENT_ID = "73137b123d294597878fe43b1a3d9646";
    private static final String REDIRECT_URI = "https://hidirektor.com.tr";
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-playback-state,streaming";

    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioVisualizerView = findViewById(R.id.visualizer);
        stopRecording = findViewById(R.id.stopRecording);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, makeIntentFilter());

        authenticateSpotifyUser();

        fetchSpotifyTokenAndCurrentPlayingTrack();

        //startService();

        stopRecording.setOnClickListener(v -> {
            if(isRecording) {
                stopService();
            }
        });
    }

    private void authenticateSpotifyUser() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-playback-state"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRecording) {
            startService();
        }
    }

    private void fetchCurrentPlayingTrack(String accessToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spotify.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotifyService service = retrofit.create(SpotifyService.class);

        service.getCurrentPlayingTrack("Bearer " + accessToken).enqueue(new retrofit2.Callback<CurrentlyPlaying>() {
            @Override
            public void onResponse(retrofit2.Call<CurrentlyPlaying> call, retrofit2.Response<CurrentlyPlaying> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("SpotifyTrackInfo", "Şu anda çalan şarkı: " + response.body().item.name);
                    showCustomToast("Şu anda çalan şarkı: " + response.body().item.name);
                    //Toast.makeText(MainActivity.this, "Şu anda çalan şarkı: " + response.body().item.name, Toast.LENGTH_LONG).show();
                } else {
                    Log.d("SpotifyTrackInfo", "Şu anda çalan şarkı bilgisi alınamadı.");
                    showCustomToast("Şu anda çalan şarkı bilgisi alınamadı.");
                    //Toast.makeText(MainActivity.this, "Şu anda çalan şarkı bilgisi alınamadı.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<CurrentlyPlaying> call, Throwable t) {
                Log.e("SpotifyTrackInfo", "API isteği başarısız.", t);
                showCustomToast("API isteği başarısız.");
                //Toast.makeText(MainActivity.this, "API isteği başarısız.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showCustomToast(String message) {
        // Layout'u inflate etme
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container));

        // Layout içindeki TextView ve ImageView'i bulup, değerleri ayarlama
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message); // Gösterilecek mesajı ayarla
        ImageView image = layout.findViewById(R.id.toast_icon);
        image.setImageResource(R.drawable.spotify_logo); // Özelleştirilmiş ikonu ayarla

        // Toast oluşturma ve gösterme
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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
        } else if(requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    saveToken(response.getAccessToken());
                    break;
            }
        }
    }

    private void saveToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SpotifyToken", token);
        editor.apply();

        fetchCurrentPlayingTrack(token);
    }

    private void startService() {
        if (!isRecording) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // Kullanıcıya neden bu izne ihtiyaç duyulduğunu açıklayın, gerekirse
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    // Burada kullanıcıya bir diyalog gösterin ve izni açıklayın, ardından izni tekrar isteyin
                } else {
                    // İzinleri ilk kez isteyin veya kullanıcı "bir daha sorma" seçeneğini işaretlediyse
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
                }
            } else {
                // İzinler zaten verilmiş, servisi başlat
                MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mediaProjectionManager != null) {
                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 2000);
                }
            }
        }
    }

    private void stopService() {
        final Intent broadcast = new Intent(ACTION_STOP);
        sendBroadcast(broadcast);
        isRecording = false;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BROADCAST_WAVEFORM.equals(action) && intent.getExtras() != null) {
                final byte[] audioData = intent.getByteArrayExtra(BROADCAST_EXTRA_DATA);
                final int length = intent.getIntExtra("length", 0);

                if (audioData != null && length > 0) {
                    audioVisualizerView.addAudioData(audioData, length);
                }
            }
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_WAVEFORM);

        return intentFilter;
    }

    private void fetchSpotifyTokenAndCurrentPlayingTrack() {
        SharedPreferences sharedPreferences = getSharedPreferences("SpotifyPreferences", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("SpotifyToken", "");
        if (!token.isEmpty()) {
            fetchCurrentPlayingTrack(token);
        } else {
            Log.d("SpotifyAuth", "Token bulunamadı.");
        }
    }
}