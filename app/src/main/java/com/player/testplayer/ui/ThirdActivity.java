package com.player.testplayer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.player.testplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zuichu.smartplayer.player.IVideoPlayer;
import me.zuichu.smartplayer.player.VideoExoPlayer;
import me.zuichu.smartplayer.view.ControlView;
import me.zuichu.smartplayer.view.SeekView;

public class ThirdActivity extends AppCompatActivity implements ControlView.ScreenClick {
    @BindView(R.id.fl_parent)
    FrameLayout fl_parent;
    @BindView(R.id.videoExoPlayer)
    VideoExoPlayer videoExoPlayer;
    @BindView(R.id.controlView)
    ControlView controlView;
    @BindView(R.id.seekView)
    SeekView seekView;
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        videoExoPlayer.setControlView(controlView);
        videoExoPlayer.setPlayerParent(fl_parent, 100);
        videoExoPlayer.setScreenMode(IVideoPlayer.MODE_DEFAULT_SIZE);
        videoExoPlayer.setPortraitFullScreen(false);
        videoExoPlayer.setScreenOnWhilePlaying(true);
        videoExoPlayer.setAutoPlay(true);
        videoExoPlayer.startPlay(url);
        controlView.setScreenClick(this);
        seekView.setTitle("标题");
        seekView.setProgress(60);
        seekView.showView();
    }

    @Override
    public void onBackPressed() {
        if (videoExoPlayer.isFullScreen()) {
            videoExoPlayer.setFullScreen(false);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoExoPlayer.destroy();
    }

    @Override
    public void screenPlay() {
        videoExoPlayer.playOrPause();
    }

    @Override
    public void screenClick() {
        videoExoPlayer.setFullScreen(!videoExoPlayer.isFullScreen());
    }

    @Override
    public void lockClick(boolean lock) {
        videoExoPlayer.setLock(lock);
    }
}
