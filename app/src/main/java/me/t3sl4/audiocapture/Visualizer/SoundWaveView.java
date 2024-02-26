package me.t3sl4.audiocapture.Visualizer;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import me.t3sl4.audiocapture.R;
import me.t3sl4.audiocapture.Visualizer.Player.DefaultSoundViewPlayer;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayer;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayerOnCompleteListener;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayerOnDurationListener;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayerOnPauseListener;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayerOnPlayListener;
import me.t3sl4.audiocapture.Visualizer.Player.SoundViewPlayerOnPreparedListener;

import java.io.IOException;

public class SoundWaveView extends FrameLayout implements SoundViewPlayerOnPlayListener,
        SoundViewPlayerOnDurationListener,
        SoundViewPlayerOnPauseListener,
        SoundViewPlayerOnPreparedListener,
        SoundViewPlayerOnCompleteListener {

    protected final Context context;
    protected SoundViewPlayer player = new DefaultSoundViewPlayer();
    protected int layout = R.layout.sounwave_view;

    private SoundVisualizerBarView visualizerBar;

    private final String TAG = SoundWaveView.class.getCanonicalName();

    public SoundWaveView(Context context) {
        super(context);
        this.context = context;

        init(context);
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(context);
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        init(context);
    }

    public void setPlayer(SoundViewPlayer player) {
        this.player = player;
    }

    public void addAudioFileUri(final Uri audioFileUri) throws IOException {
        player.setAudioSource(context, audioFileUri);

        visualizerBar.updateVisualizer(audioFileUri);
    }

    public void addAudioFileUrl(String audioFileUrl) throws IOException {
        player.setAudioSource(audioFileUrl);

        visualizerBar.updateVisualizer(audioFileUrl);
    }

    protected void init(final Context context) {
        View view = LayoutInflater.from(context).inflate(layout, this);

        player.setOnCompleteListener(this)
                .setOnDurationListener(this)
                .setOnPauseListener(this)
                .setOnPlayListener(this)
                .setOnPrepariedListener(this);

        visualizerBar = view.findViewById(R.id.vSoundBar);
    }

    public void addAudioData(byte[] audioData, int length) {
        // Process and visualize the audio data
        visualizerBar.updateVisualizer(audioData, length);
    }

    @Override
    public void onDurationProgress(SoundViewPlayer player, Long duration, Long currentTimestamp) {
        visualizerBar.updatePlayerPercent(currentTimestamp / (float) duration);
    }

    @Override
    public void onPause(SoundViewPlayer player) {
        //durdur başlat
    }

    @Override
    public void onPlay(SoundViewPlayer player) {
        //başlat durdur
    }

    @Override
    public void onPrepared(SoundViewPlayer player) {
        //süre işlemleri
    }

    @Override
    public void onComplete(SoundViewPlayer player) {
        visualizerBar.updatePlayerPercent(0);
    }
}