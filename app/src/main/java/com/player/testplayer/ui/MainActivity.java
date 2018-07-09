package com.player.testplayer.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.player.testplayer.R;
import com.player.testplayer.utils.WindowService;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zuichu.smartplayer.player.VideoIjkPlayer;
import me.zuichu.smartplayer.view.ControlView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity implements
        VideoIjkPlayer.VideoInfoCallBack, ControlView.ScreenClick {
    @BindView(R.id.fl_parent)
    FrameLayout fl_parent;
    @BindView(R.id.ijk)
    VideoIjkPlayer videoIjkPlayer;
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";
    @BindView(R.id.controlView)
    ControlView controlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        videoIjkPlayer.setControlView(controlView);
        videoIjkPlayer.setAutoPlay(true);
        videoIjkPlayer.setVideoInfoCallBack(this);
        videoIjkPlayer.setPlayerParent(fl_parent, 300);
        videoIjkPlayer.setScreenMode(VideoIjkPlayer.SCREEN_MODE_4_3);
        videoIjkPlayer.prepare(url);
        videoIjkPlayer.setNetAutoPlay(false);
        videoIjkPlayer.setPortraitFullScreen(true);
        controlView.setScreenClick(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 10);
            } else {
                Intent intent = new Intent(this, WindowService.class);
                startService(intent);
            }
        } else {
            Intent intent = new Intent(this, WindowService.class);
            startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(MainActivity.this, "not granted", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(MainActivity.this, "提示", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, WindowService.class);
                    startService(intent);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (videoIjkPlayer.isFullScreen()) {
            videoIjkPlayer.setFullScreen(false);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoIjkPlayer.destroy();
    }

    @Override
    public void onInfoDuration(long currentDuration, long duration) {

    }

    @Override
    public void onPrepared(IMediaPlayer mp) {

    }

    @Override
    public void screenPlay() {
        videoIjkPlayer.playOrPause();
    }

    @Override
    public void screenClick() {
        videoIjkPlayer.setFullScreen(!videoIjkPlayer.isFullScreen());
    }

    @Override
    public void lockClick(boolean lock) {
        videoIjkPlayer.setLock(lock);
    }
}
