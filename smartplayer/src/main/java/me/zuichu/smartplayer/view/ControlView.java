package me.zuichu.smartplayer.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.player.AudioPlayer;

public class ControlView extends BaseControl implements View.OnClickListener {
    private TextView tv_current;
    private TextView tv_duration;
    private SeekBar seekBar;
    private ImageView iv_screen;
    private ImageView iv_play;
    private ImageView iv_play_min;
    private ImageView iv_lock;
    private LinearLayout ll_control;
    private View rootView;
    private ScreenClick screenClick;
    private CountDownTimer countDownTimer;
    private int count = 5 * 1000;
    private boolean lock = false;

    public ControlView(@NonNull Context context) {
        super(context);
        init();
    }

    public ControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_controlview, this);

        tv_current = rootView.findViewById(R.id.tv_current);
        tv_duration = rootView.findViewById(R.id.tv_duration);
        seekBar = rootView.findViewById(R.id.seekbar);
        iv_screen = rootView.findViewById(R.id.iv_screen);
        iv_play = rootView.findViewById(R.id.iv_play);
        iv_play_min = rootView.findViewById(R.id.iv_play_min);
        iv_lock = rootView.findViewById(R.id.iv_lock);
        ll_control = rootView.findViewById(R.id.ll_control);

        iv_screen.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        iv_play_min.setOnClickListener(this);
        iv_lock.setOnClickListener(this);
        setVisibility(View.GONE);
        setControl();
    }

    @Override
    public void setOrientation(boolean portrait) {
        super.setOrientation(portrait);
        if (portrait) {
            iv_play.setVisibility(View.GONE);
            iv_play_min.setVisibility(View.VISIBLE);
        } else {
            iv_play.setVisibility(View.VISIBLE);
            iv_play_min.setVisibility(View.GONE);
        }
    }

    @Override
    public SeekBar getSeekBar() {
        return seekBar;
    }

    @Override
    public void setDuration(long current, long duration) {
        super.setDuration(current, duration);
        tv_current.setText(AudioPlayer.generateTime(current));
        tv_duration.setText(AudioPlayer.generateTime(duration));
    }

    @Override
    public void setPlayStatus(boolean pause) {
        super.setPlayStatus(pause);
        iv_play_min.setBackgroundResource(pause ? R.drawable.btn_play_selector : R.drawable.btn_pause_selector);
        iv_play.setImageResource(pause ? R.mipmap.ic_video_player_btn_play : R.mipmap.ic_video_player_btn_pause);
    }

    @Override
    public void setSeekBarProgress(int progress) {
        super.setSeekBarProgress(progress);
        seekBar.setProgress(progress);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_play) {
            if (screenClick != null) {
                screenClick.screenPlay();
            }
        } else if (id == R.id.iv_screen) {
            if (screenClick != null) {
                screenClick.screenClick();
            }
        } else if (id == R.id.iv_play_min) {
            if (screenClick != null) {
                screenClick.screenPlay();
            }
        } else if (id == R.id.iv_lock) {
            if (screenClick != null) {
                screenClick.lockClick(lock);
            }
            iv_lock.setImageResource(lock ? R.mipmap.live_player_icon_rotate_unlock : R.mipmap.live_player_icon_rotate_lock);
            lock = !lock;
            setLockStatus();
        }
    }

    @Override
    public void setControl() {
        super.setControl();
        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
            setLockStatus();
            countDownTimer = new CountDownTimer(count, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setVisibility(View.GONE);
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }.start();
        } else {
            setVisibility(View.GONE);
            setLockStatus();
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    private void setLockStatus() {
        if (lock) {
            ll_control.setVisibility(View.GONE);
            iv_play_min.setVisibility(View.GONE);
        } else {
            ll_control.setVisibility(View.VISIBLE);
            iv_play_min.setVisibility(View.VISIBLE);
        }
    }

    public void setScreenClick(ScreenClick screenClick) {
        this.screenClick = screenClick;
    }

    public interface ScreenClick {

        void screenPlay();

        void screenClick();

        void lockClick(boolean lock);
    }
}
