package com.player.testplayer.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.player.testplayer.view.WindowView;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

public class WindowService extends Service {
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private float mStartX, mStartY;
    int statusBarHeight = -1;
    private WindowView windowView;
    private boolean click = true;
    private int startX, startY, stopX, stopY;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
    }

    private void createToucher() {
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = FLAG_NOT_FOCUSABLE | FLAG_WATCH_OUTSIDE_TOUCH;

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        params.width = Utils.dp2px(getBaseContext(), 60);
        params.height = Utils.dp2px(getBaseContext(), 60);
        windowView = new WindowView(getBaseContext());
        windowManager.addView(windowView, params);
        windowView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        windowView.getImageView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = event.getRawX();
                        mStartY = event.getRawY();
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        params.x += event.getRawX() - mStartX;
                        params.y += event.getRawY() - mStartY;
                        windowManager.updateViewLayout(windowView, params);
                        mStartX = event.getRawX();
                        mStartY = event.getRawY();
                        stopX = (int) event.getX();
                        stopY = (int) event.getY();
                        if (Math.abs(startX - stopX) >= 10 || Math.abs(startY - stopY) >= 10) {
                            click = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (click) {
                            Toast.makeText(getBaseContext(), "点击", Toast.LENGTH_SHORT).show();
                        }
                        click = true;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowView != null) {
            windowManager.removeView(windowView);
        }
    }
}
