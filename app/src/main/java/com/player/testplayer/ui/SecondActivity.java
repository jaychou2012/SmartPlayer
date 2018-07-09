package com.player.testplayer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.player.testplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zuichu.smartplayer.player.IVideoPlayer;
import me.zuichu.smartplayer.player.VideoPlayer;
import me.zuichu.smartplayer.view.ControlView;

public class SecondActivity extends AppCompatActivity implements ControlView.ScreenClick {
    @BindView(R.id.fl_parent)
    FrameLayout fl_parent;
    @BindView(R.id.videoPlayerView)
    VideoPlayer videoPlayerView;
    @BindView(R.id.controlView)
    ControlView controlView;
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        videoPlayerView.setControlView(controlView);
        videoPlayerView.setPlayerParent(fl_parent, 300);
        videoPlayerView.setScreenMode(IVideoPlayer.MODE_DEFAULT_SIZE);
        videoPlayerView.setDataSoure(url);
        videoPlayerView.setPortraitFullScreen(true);
        videoPlayerView.setAutoPlay(true);
        videoPlayerView.setPrepareAsync();
        controlView.setScreenClick(this);
    }

    @Override
    public void onBackPressed() {
        if (videoPlayerView.isFullScreen()) {
            videoPlayerView.setFullScreen(false);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayerView.destroy();
    }

    @Override
    public void screenPlay() {
        videoPlayerView.playOrPause();
    }

    @Override
    public void screenClick() {
        videoPlayerView.setFullScreen(!videoPlayerView.isFullScreen());
    }

    @Override
    public void lockClick(boolean lock) {
        videoPlayerView.setLock(lock);
    }
}
