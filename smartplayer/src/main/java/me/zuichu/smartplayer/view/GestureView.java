package me.zuichu.smartplayer.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.player.AudioPlayer;
import me.zuichu.smartplayer.utils.Utils;

public class GestureView extends FrameLayout {
    private ProgressBar progressBar;
    private ImageView iv_status;
    private TextView tv_time;
    private View rootView;
    private FrameLayout.LayoutParams layoutParams;
    private float dx = 0, dy = 0;
    private float directX = 0, directY = 0;
    private int limitX = 50, limitY = 100;
    private float brightness = 0;
    private float curretnVolume = 0.5f;
    private int maxVolume = 15;
    private Window window;
    private int left = 0, right = 0;
    private AudioManager audioManager;
    private GestureSeek gestureSeek;
    private boolean enable = true;

    public GestureView(Context context) {
        super(context);
        init();
    }

    public GestureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_gestureview, this);

        progressBar = rootView.findViewById(R.id.progressbar);
        iv_status = rootView.findViewById(R.id.iv_status);
        tv_time = rootView.findViewById(R.id.tv_time);

        layoutParams = new LayoutParams(Utils.getScreenWidth(getContext()) / 3, FrameLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        setVisibility(View.GONE);
        brightness = ((Activity) getContext()).getWindow().getAttributes().screenBrightness;
        window = ((Activity) getContext()).getWindow();
        left = Utils.getScreenWidth(getContext()) / 3;
        right = (Utils.getScreenWidth(getContext()) / 3) * 2;
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curretnVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / Float.valueOf(maxVolume);
    }

    public void showBrightnessView() {
        if (brightness < 0.00f) {
            brightness = 0.50f;
        }
        if (brightness < 0.01f) {
            brightness = 0.01f;
        }
        if (directY > 0) {
            brightness = brightness + 0.01f;
        } else {
            brightness = brightness - 0.01f;
        }
        if (brightness > 1.0f) {
            brightness = 1.0f;
        } else if (brightness < 0.01f) {
            brightness = 0.01f;
        }
        setVisibility(View.VISIBLE);
        iv_status.setVisibility(View.VISIBLE);
        tv_time.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        iv_status.setImageResource(R.mipmap.ic_brightness_white);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
        progressBar.setMax(100);
        progressBar.setProgress((int) (brightness * 100));
    }

    public void showVolumeView() {
        setVisibility(View.VISIBLE);
        iv_status.setVisibility(View.VISIBLE);
        tv_time.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        iv_status.setImageResource(R.mipmap.ic_volume_up_white);
        if (curretnVolume < 0.01f) {
            curretnVolume = 0.00f;
        }
        if (directY > 0) {
            curretnVolume = curretnVolume + 0.01f;
        } else {
            curretnVolume = curretnVolume - 0.01f;
        }
        if (curretnVolume > 1.0f) {
            curretnVolume = 1.0f;
        } else if (curretnVolume < 0.01f) {
            curretnVolume = 0.0f;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (curretnVolume * maxVolume),
                AudioManager.FLAG_PLAY_SOUND);
        progressBar.setMax(100);
        progressBar.setProgress((int) (curretnVolume * 100));
    }

    public void showSeekView(boolean forward, long current, long duration) {
        setVisibility(View.VISIBLE);
        iv_status.setVisibility(View.GONE);
        tv_time.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        tv_time.setText(AudioPlayer.generateTime(current) + " / " + AudioPlayer.generateTime(duration));
    }

    public void hideGestureView() {
        setVisibility(View.GONE);
    }

    public void onTouch(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                hideGestureView();
                break;
        }
    }

    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!enable) {
            return;
        }
        dx = e2.getX() - e1.getX();
        dy = e2.getY() - e1.getY();
        directX = distanceX;
        directY = distanceY;
        if (Math.abs(dy) > Math.abs(dx) && e1.getX() < left) {
            showBrightnessView();
        } else if (Math.abs(dy) > Math.abs(dx) && e1.getX() > right) {
            showVolumeView();
        } else if (Math.abs(dx) > Math.abs(dy)) {
            if (gestureSeek == null) {
                return;
            }
            gestureSeek.gestureSeek(distanceX < 0 ? true : false);
        }
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public void setEnAble(boolean enable) {
        this.enable = enable;
    }

    public void setGestureSeek(GestureSeek gestureSeek) {
        this.gestureSeek = gestureSeek;
    }

    public interface GestureSeek {
        void gestureSeek(boolean add);
    }
}
