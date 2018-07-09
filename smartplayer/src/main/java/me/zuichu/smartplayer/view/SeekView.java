package me.zuichu.smartplayer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import me.zuichu.smartplayer.R;

public class SeekView extends FrameLayout implements SeekBar.OnSeekBarChangeListener, Animation.AnimationListener, View.OnClickListener {
    private View rootView;
    private SeekBar seekBar;
    private TextView tv_min;
    private TextView tv_max;
    private TextView tv_title;
    private TextView tv_transparent;
    private String title;
    private boolean percentSign = true;
    private boolean animation = true;
    public static long animDuration = 500;
    private static TranslateAnimation translateAnimation;
    private static AlphaAnimation alphaAnimation;
    private onProgressChange onProgressChange;

    public SeekView(@NonNull Context context) {
        super(context);
        init();
    }

    public SeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_seek, this);

        seekBar = rootView.findViewById(R.id.seekbar);
        tv_min = rootView.findViewById(R.id.tv_min);
        tv_max = rootView.findViewById(R.id.tv_max);
        tv_title = rootView.findViewById(R.id.tv_title);
        tv_transparent = rootView.findViewById(R.id.tv_transparent);

        seekBar.setOnSeekBarChangeListener(this);
        tv_transparent.setOnClickListener(this);
        tv_title.setText(title + "（" + seekBar.getMax() + (percentSign ? "%）" : "）"));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tv_min.setText(progress + (percentSign ? "%" : ""));
        tv_title.setText(title + "（" + progress + (percentSign ? "%）" : "）"));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void dismissView() {
        if (animation) {
            setTranslateAnimationToBottom();
        } else {
            setVisibility(View.GONE);
        }
    }

    public void showView() {
        if (animation) {
            setVisibility(View.VISIBLE);
            setTranslateAnimationToTop();
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress);
        tv_title.setText(title + "（" + progress + (percentSign ? "%）" : "）"));
        if (onProgressChange != null) {
            onProgressChange.onProgressChange(progress);
        }
    }

    public void setOnProgressChange(onProgressChange onProgressChange) {
        this.onProgressChange = onProgressChange;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMaxProgress(int maxProgress) {
        seekBar.setMax(maxProgress);
        tv_max.setText(maxProgress + (percentSign ? "%" : ""));
    }

    public void setPercentSign(boolean percentSign) {
        this.percentSign = percentSign;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    private void setTranslateAnimationToTop() {
        translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
                1.0f, Animation.RELATIVE_TO_PARENT, 0);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(animDuration);
        translateAnimation.setInterpolator(new LinearInterpolator());
        setAnimation(translateAnimation);
        startAnimation(translateAnimation);
    }

    private void setTranslateAnimationToBottom() {
        translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
                0, Animation.RELATIVE_TO_PARENT, 1.0f);
        translateAnimation.setFillAfter(true);
        translateAnimation.setDuration(animDuration);
        translateAnimation.setInterpolator(new LinearInterpolator());
        setAnimation(translateAnimation);
        startAnimation(translateAnimation);
        setLayoutAnimationListener(this);
    }

    private void setAlphaAnimationIn() {
        alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(animDuration);
        setAnimation(alphaAnimation);
        startAnimation(alphaAnimation);
    }

    private void setAlphaAnimationOut() {
        alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(animDuration);
        setAnimation(alphaAnimation);
        startAnimation(alphaAnimation);
        setLayoutAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        dismissView();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_transparent) {
            dismissView();
        }
    }

    public interface onProgressChange {
        void onProgressChange(int progress);
    }

}
