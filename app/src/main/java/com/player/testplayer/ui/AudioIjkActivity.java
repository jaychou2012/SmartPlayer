package com.player.testplayer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.player.testplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zuichu.smartplayer.player.AudioIjkPlayer;
import me.zuichu.smartplayer.player.AudioPlayer;
import me.zuichu.smartplayer.player.BaseAudioPlayer;

public class AudioIjkActivity extends AppCompatActivity implements AudioIjkPlayer.AudioDurationCallBack, View.OnClickListener, AudioIjkPlayer.AudioBufferCallBack, AudioIjkPlayer.AudioStatusCallBack {
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_duration)
    TextView tv_duration;
    @BindView(R.id.seekbar)
    SeekBar seekBar;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    @BindView(R.id.tv_status)
    TextView tv_status;
    private AudioIjkPlayer audioIjkPlayer;
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_ijk);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        audioIjkPlayer = AudioIjkPlayer.getInstance(this);
        audioIjkPlayer.setSeekBar(seekBar);
        audioIjkPlayer.setLogLevel(true);
        audioIjkPlayer.setAutoPlay(true);
        audioIjkPlayer.setMediacodec(true);
        audioIjkPlayer.setAudioDurationCallBack(this);
        audioIjkPlayer.setAudioBufferCallBack(this);
        audioIjkPlayer.setAudioStatusCallBack(this);
        audioIjkPlayer.startPlay(url);
        iv_play.setOnClickListener(this);
    }

    @Override
    public void onInfoDuration(long currentDuration, long duration) {
        tv_current.setText(AudioPlayer.generateTime(currentDuration));
        tv_duration.setText(AudioPlayer.generateTime(duration));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioIjkPlayer.getInstance(this).destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                audioIjkPlayer.playOrPause();
                iv_play.setImageResource(audioIjkPlayer.isPause() ?
                        R.mipmap.ic_video_player_btn_play : R.mipmap.ic_video_player_btn_pause);
                break;
        }
    }

    @Override
    public void onInfoBuffer(boolean buffer) {
        if (buffer) {
            tv_status.setText("缓冲中...");
        } else {
            tv_status.setText("播放中...");
        }
    }

    @Override
    public void onAudioStatus(int audioStatus) {
        switch (audioStatus) {
            case BaseAudioPlayer.AUDIO_STATUS_PLAYING:
                iv_play.setImageResource(R.mipmap.ic_video_player_btn_pause);
                break;
            case BaseAudioPlayer.AUDIO_STATUS_PAUSE:
                iv_play.setImageResource(R.mipmap.ic_video_player_btn_play);
                break;
            case BaseAudioPlayer.AUDIO_STATUS_STOP:
                iv_play.setImageResource(R.mipmap.ic_video_player_btn_play);
                break;
        }
    }
}
