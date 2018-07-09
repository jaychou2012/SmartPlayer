package me.zuichu.smartplayer.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.utils.Utils;

public class NetView extends FrameLayout implements View.OnClickListener {
    private TextView tv_net;
    private FrameLayout fl_net;
    private View rootView;
    private NetStateReceiver netStateReceiver;
    private NetClick netClick;
    public static final int NET_TYPE_NONET = 0;
    public static final int NET_TYPE_WIFI = 1;
    public static final int NET_TYPE_MOBILE = 2;
    private boolean netAutoPlay = false;
    private int currentNetType = NET_TYPE_WIFI;

    public NetView(@NonNull Context context) {
        super(context);
        init();
    }

    public NetView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NetView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_netview, this);

        tv_net = rootView.findViewById(R.id.tv_net);
        fl_net = rootView.findViewById(R.id.fl_net);

        tv_net.setOnClickListener(this);
        netStateReceiver = new NetStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getContext().registerReceiver(netStateReceiver, intentFilter);
        setVisibility(View.GONE);
    }

    public void setNetClick(NetClick netClick) {
        this.netClick = netClick;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_net) {
            if (netClick != null) {
                netClick.netClick(currentNetType);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(netStateReceiver);
    }

    class NetStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                int netType = 0;
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo == null) {
                    setNetStatus("没有网络", NET_TYPE_NONET);
                } else {
                    netType = networkInfo.getType();
                    if (netType == ConnectivityManager.TYPE_WIFI) {
                        if (Utils.isNetAvailable() != 0) {
                            setNetStatus("没有网络", NET_TYPE_NONET);
                        } else {
                            setVisibility(View.GONE);
                            if (netClick != null) {
                                netClick.noNetOrMobileNet(NET_TYPE_WIFI);
                            }
                        }
                    } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                        if (netAutoPlay) {
                            setVisibility(View.GONE);
                            if (netClick != null) {
                                netClick.noNetOrMobileNet(NET_TYPE_MOBILE);
                            }
                        } else {
                            setNetStatus("当前是移动网络", NET_TYPE_MOBILE);
                        }
                    }
                }
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                if (Utils.isNetAvailable() != 0) {
                    setNetStatus("没有网络", NET_TYPE_NONET);
                }
            }
        }
    }

    public void setNetAutoPlay(boolean netAutoPlay) {
        this.netAutoPlay = netAutoPlay;
    }

    private void setNetStatus(String text, int netType) {
        tv_net.setText(text);
        setVisibility(View.VISIBLE);
        if (netClick != null) {
            netClick.noNetOrMobileNet(netType);
        }
        currentNetType = netType;
        if (netType == NET_TYPE_NONET) {
            fl_net.setBackgroundResource(R.drawable.bg_nonet);
        } else {
            fl_net.setBackground(null);
        }
    }

    public interface NetClick {
        void netClick(int net);

        void noNetOrMobileNet(int net);
    }
}
