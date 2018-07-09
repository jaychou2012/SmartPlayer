package me.zuichu.smartplayer.player;

import android.app.Activity;
import android.content.Context;
import android.net.TrafficStats;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.Locale;

/**
 * Created by office on 2018/5/16.
 */

public class BaseVideoPlayer extends FrameLayout {

    public BaseVideoPlayer(@NonNull Context context) {
        super(context);
    }

    public BaseVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    public void setMode(int mode, final SurfaceView surfaceView, final int width, final int height) {
        switch (mode) {
            case IVideoPlayer.MODE_ORI_SIZE:
                surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int lw = ((FrameLayout) surfaceView.getParent()).getWidth();
                        int lh = ((FrameLayout) surfaceView.getParent()).getHeight();
                        int w = width;
                        int h = height;
                        if (w > lw || h > lh) {
                            float wRatio = (float) w / (float) lw;
                            float hRatio = (float) h / (float) lh;
                            // 选择大的一个进行缩放
                            float ratio = Math.max(wRatio, hRatio);
                            w = (int) Math.ceil((float) w / ratio);
                            h = (int) Math.ceil((float) h / ratio);
                            // 设置surfaceView的布局参数
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
                            lp.gravity = Gravity.CENTER;
                            surfaceView.setLayoutParams(lp);
                        } else {
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
                            lp.gravity = Gravity.CENTER;
                            surfaceView.setLayoutParams(lp);
                        }
                        surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                break;
            case IVideoPlayer.MODE_FULL_SIZE:
                surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int lw = ((FrameLayout) surfaceView.getParent()).getWidth();
                        int lh = ((FrameLayout) surfaceView.getParent()).getHeight();
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(lw, lh);
                        lp.gravity = Gravity.CENTER;
                        surfaceView.setLayoutParams(lp);
                        surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                break;
            case IVideoPlayer.MODE_16_9_SIZE:
                surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int lw = ((FrameLayout) surfaceView.getParent()).getWidth();
                        int lh = ((FrameLayout) surfaceView.getParent()).getHeight();
                        int w = width;
                        int h = (int) ((width * 9.0) / 16.0);
                        if (w > lw || h > lh) {
                            float wRatio = (float) w / (float) lw;
                            float hRatio = (float) h / (float) lh;
                            // 选择大的一个进行缩放
                            float ratio = Math.max(wRatio, hRatio);
                            w = (int) Math.ceil((float) w / ratio);
                            h = (int) Math.ceil((float) h / ratio);
                            // 设置surfaceView的布局参数
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
                            lp.gravity = Gravity.CENTER;
                            surfaceView.setLayoutParams(lp);
                        }
                        surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                break;
            case IVideoPlayer.MODE_4_3_SIZE:
                surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int lw = ((FrameLayout) surfaceView.getParent()).getWidth();
                        int lh = ((FrameLayout) surfaceView.getParent()).getHeight();
                        int w = width;
                        int h = (int) ((width * 3.0) / 4.0);
                        if (w > lw || h > lh) {
                            float wRatio = (float) w / (float) lw;
                            float hRatio = (float) h / (float) lh;
                            // 选择大的一个进行缩放
                            float ratio = Math.max(wRatio, hRatio);
                            w = (int) Math.ceil((float) w / ratio);
                            h = (int) Math.ceil((float) h / ratio);
                            // 设置surfaceView的布局参数
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
                            lp.gravity = Gravity.CENTER;
                            surfaceView.setLayoutParams(lp);
                        }
                        surfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                break;
        }
    }

    public void setWindowFullScreen(boolean fullScreen) {
        if (!fullScreen) {
            WindowManager.LayoutParams lp = ((Activity) getContext()).getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) getContext()).getWindow().setAttributes(lp);
            ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams lp = ((Activity) getContext()).getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            ((Activity) getContext()).getWindow().setAttributes(lp);
            ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public void setViewFullScreen(boolean fullScreen) {
        if (!fullScreen) {
            ((Activity) getContext()).getWindow().getDecorView().
                    setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            ((Activity) getContext()).getWindow().getDecorView().
                    setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    private ViewGroup getViewGroup() {
        return (ViewGroup) ((Activity) getContext()).findViewById(Window.ID_ANDROID_CONTENT);
    }

    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;

    public String getNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes(getContext().getApplicationInfo().uid);
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return String.valueOf(speed) + " kb/s";
    }

    public long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }
}
