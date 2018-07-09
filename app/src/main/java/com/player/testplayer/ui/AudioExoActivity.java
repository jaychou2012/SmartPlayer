package com.player.testplayer.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.player.testplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zuichu.smartplayer.player.AudioExoPlayer;
import me.zuichu.smartplayer.player.AudioPlayer;
import me.zuichu.smartplayer.player.BaseAudioPlayer;

public class AudioExoActivity extends AppCompatActivity implements AudioExoPlayer.AudioBufferCallBack, AudioExoPlayer.AudioInfoCallBack, View.OnClickListener, AudioExoPlayer.AudioStatusCallBack {
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
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";
    private AudioExoPlayer audioExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_exo);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        audioExoPlayer = AudioExoPlayer.getInstance(this);
        audioExoPlayer.setSeekBar(seekBar);
        audioExoPlayer.setAutoPlay(true);
        audioExoPlayer.setAudioFocus(true);
        audioExoPlayer.setAudioInfoCallBack(this);
        audioExoPlayer.setAudioBufferCallBack(this);
        audioExoPlayer.setAudioStatusCallBack(this);
        audioExoPlayer.startPlay(url);
        iv_play.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioExoPlayer.getInstance(this).destroy();
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
    public void onExoInfoDuration(long currentDuration, long duration) {
        tv_current.setText(AudioPlayer.generateTime(currentDuration));
        tv_duration.setText(AudioPlayer.generateTime(duration));
    }

    @Override
    public void onMediaFormat(Format audioFormat, Format videoFormat) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                audioExoPlayer.playOrPause();
                iv_play.setImageResource(audioExoPlayer.isPause() ?
                        R.mipmap.ic_video_player_btn_play : R.mipmap.ic_video_player_btn_pause);
                break;
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
