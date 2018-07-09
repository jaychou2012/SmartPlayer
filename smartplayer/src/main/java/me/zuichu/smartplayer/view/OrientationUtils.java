package me.zuichu.smartplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;

public class OrientationUtils {
    private OrientationEventListener orientationEventListener;
    private OrientationListener orientationListener;
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_REVERSE_LANDSCAPE = 2;
    public static final int ORIENTATION_REVERSE_PROTRAIT = 3;

    public OrientationUtils(final Context context) {
        orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                int screenOrientation = context.getResources().getConfiguration().orientation;
                if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        if (orientationListener != null) {
                            orientationListener.orientation(ORIENTATION_PORTRAIT);
                        }
                    }
                } else if (orientation > 225 && orientation < 315) { //设置横屏
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        if (orientationListener != null) {
                            orientationListener.orientation(ORIENTATION_LANDSCAPE);
                        }
                    }
                } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        if (orientationListener != null) {
                            orientationListener.orientation(ORIENTATION_LANDSCAPE);
                        }
                    }
                } else if (orientation > 135 && orientation < 225) {//设置反向竖屏
                    if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        if (orientationListener != null) {
                            orientationListener.orientation(ORIENTATION_PORTRAIT);
                        }
                    }
                }
            }
        };
    }

    public void setOrientationEnable() {
        if (orientationEventListener != null) {
            orientationEventListener.enable();
        }
    }

    public void setOrientationDisable() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public void setOrientationListener(OrientationListener orientationListener) {
        this.orientationListener = orientationListener;
    }

    public interface OrientationListener {
        void orientation(int direction);
    }

}
