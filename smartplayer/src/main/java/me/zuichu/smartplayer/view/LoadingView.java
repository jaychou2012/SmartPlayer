package me.zuichu.smartplayer.view;

import android.content.Context;
import android.net.TrafficStats;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.zuichu.smartplayer.R;

public class LoadingView extends FrameLayout {
    private TextView tv_loading;
    private View rootView;

    public LoadingView(@NonNull Context context) {
        super(context);
        init();
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_loading, this);
        tv_loading = rootView.findViewById(R.id.tv_loading);
    }

    public void showLoading() {
        this.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        this.setVisibility(View.GONE);
    }

    public void setLoading() {
        tv_loading.setText("缓冲中..." + getNetSpeed());
    }

    public boolean isLoading() {
        return this.getVisibility() == View.VISIBLE ? true : false;
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
