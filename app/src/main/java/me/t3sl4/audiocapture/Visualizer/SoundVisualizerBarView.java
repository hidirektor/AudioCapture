package me.t3sl4.audiocapture.Visualizer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import me.t3sl4.audiocapture.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SoundVisualizerBarView extends View {

    /**
     * constant value for Height of the bar
     */
    public static final int VISUALIZER_HEIGHT = 28;

    /**
     * bytes array converted from file.
     */
    private byte[] bytes;

    /**
     * Percentage of audio sample scale
     * Should updated dynamically while audioPlayer is played
     */
    private float denseness;

    /**
     * Canvas painting for sample scale, filling played part of audio sample
     */
    private Paint playedStatePainting = new Paint();
    /**
     * Canvas painting for sample scale, filling not played part of audio sample
     */
    private Paint notPlayedStatePainting = new Paint();

    private int width;
    private int height;

    private int playedStateColor;
    private int nonPlayedStateColor;

    private final Context context;

    public SoundVisualizerBarView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public SoundVisualizerBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SoundVisualizerBarView, 0, 0);
        playedStateColor = a.getColor(R.styleable.SoundVisualizerBarView_statePlayingColor,
                ContextCompat.getColor(context, R.color.gray));
        nonPlayedStateColor = a.getColor(R.styleable.SoundVisualizerBarView_stateNonPlayingColor,
                ContextCompat.getColor(context, R.color.colorRecording));

        a.recycle();

        init();
    }

    public void updateVisualizer(byte[] audioData, int length) {
        // Process and visualize the real-time audio data
        // Update your visualizer logic here based on the audio data
        // You may need to decode the audio data and update the visualization accordingly
        this.bytes = audioData;
        this.invalidate(); // Request a redraw of the view
    }

    public void setPlayedStateColor(@ColorRes int playedStateColor) {
        this.playedStateColor = playedStateColor;
    }

    public void setNonPlayedStateColor(@ColorRes int nonPlayedStateColor) {
        this.nonPlayedStateColor = nonPlayedStateColor;
    }

    private void init() {
        bytes = null;

        playedStatePainting.setStrokeWidth(1f);
        playedStatePainting.setAntiAlias(true);
        playedStatePainting.setColor(playedStateColor);
        notPlayedStatePainting.setStrokeWidth(1f);
        notPlayedStatePainting.setAntiAlias(true);
        notPlayedStatePainting.setColor(nonPlayedStateColor);
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(Uri uri) throws FileNotFoundException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        updateVisualizer(inputStream);
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(String url) throws IOException {
        URLConnection connection = new URL("url of your .mp3 file").openConnection();
        connection.connect();

        updateVisualizer(connection.getInputStream());
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(InputStream inputStream) {
        this.bytes = readInputStream(inputStream);
        invalidate();
    }

    /**
     * update and redraw Visualizer view
     */
    public void updateVisualizer(byte[] bytes) {
        this.bytes = bytes;
        invalidate();
    }

    /**
     * Update player percent. 0 - file not played, 1 - full played
     *
     * @param percent
     */
    public void updatePlayerPercent(float percent) {
        denseness = (int) Math.ceil(width * percent);
        if (denseness < 0) {
            denseness = 0;
        } else if (denseness > width) {
            denseness = width;
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null || width == 0) {
            return;
        }
        float totalBarsCount = width / dp(3);
        if (totalBarsCount <= 0.1f) {
            return;
        }
        byte value;
        int samplesCount = (bytes.length * 8 / 5);
        float samplesPerBar = samplesCount / totalBarsCount;
        float barCounter = 0;
        int nextBarNum = 0;

        int y = (height - dp(VISUALIZER_HEIGHT)) / 2;
        int barNum = 0;
        int lastBarNum;
        int drawBarCount;

        for (int a = 0; a < samplesCount; a++) {
            if (a != nextBarNum) {
                continue;
            }
            drawBarCount = 0;
            lastBarNum = nextBarNum;
            while (lastBarNum == nextBarNum) {
                barCounter += samplesPerBar;
                nextBarNum = (int) barCounter;
                drawBarCount++;
            }

            int bitPointer = a * 5;
            int byteNum = bitPointer / Byte.SIZE;
            int byteBitOffset = bitPointer - byteNum * Byte.SIZE;
            int currentByteCount = Byte.SIZE - byteBitOffset;
            int nextByteRest = 5 - currentByteCount;
            value = (byte) ((bytes[byteNum] >> byteBitOffset) & ((2 << (Math.min(5, currentByteCount) - 1)) - 1));
            if (nextByteRest > 0) {
                value <<= nextByteRest;
                value |= bytes[byteNum + 1] & ((2 << (nextByteRest - 1)) - 1);
            }

            for (int b = 0; b < drawBarCount; b++) {
                float left = barNum * dp(4);
                float top = y + dp(VISUALIZER_HEIGHT - Math.max(1, VISUALIZER_HEIGHT * value / 31.0f));
                float right = left + dp(3);
                float bottom = y + dp(VISUALIZER_HEIGHT);
                if (left < denseness && left + dp(2) < denseness) {
                    canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                } else {
                    canvas.drawRect(left, top, right, bottom, playedStatePainting);
                    if (left < denseness) {
                        canvas.drawRect(left, top, right, bottom, notPlayedStatePainting);
                    }
                }
                barNum++;
            }
        }
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(getContext().getResources().getDisplayMetrics().density * value);
    }

    private byte[] readInputStream(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toByteArray();
    }

}