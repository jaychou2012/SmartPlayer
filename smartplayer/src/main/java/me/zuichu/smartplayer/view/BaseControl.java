package me.zuichu.smartplayer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class BaseControl extends FrameLayout {

    public BaseControl(@NonNull Context context) {
        super(context);
    }

    public BaseControl(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseControl(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOrientation(boolean portrait) {

    }

    public void setPlayStatus(boolean pause) {

    }

    public void setSeekBarProgress(int progress) {

    }

    public void setDuration(long current, long duration) {

    }

    public void setControl() {

    }

    public SeekBar getSeekBar() {
        return null;
    }
}
