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
import me.zuichu.smartplayer.player.AudioPlayer;
import me.zuichu.smartplayer.player.BaseAudioPlayer;

public class AudioActivity extends AppCompatActivity implements AudioPlayer.AudioDurationCallBack, AudioPlayer.AudioBufferCallBack, View.OnClickListener, AudioPlayer.AudioStatusCallBack {
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
    private AudioPlayer audioPlayer;
    private String url = "http://221.228.226.5/14/z/w/y/y/zwyyobhyqvmwslabxyoaixvyubmekc" +
            "/sh.yinyuetai.com/4599015ED06F94848EBF877EAAE13886.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        audioPlayer = AudioPlayer.getInstance(this);
        audioPlayer.setDataSoure(url);
        audioPlayer.setAudioDurationCallBack(this);
        audioPlayer.setAudioBufferCallBack(this);
        audioPlayer.setAudioStatusCallBack(this);
        audioPlayer.setAutoPlay(true);
        audioPlayer.setSeekBar(seekBar);
        audioPlayer.startAsync();
        iv_play.setOnClickListener(this);
    }

    @Override
    public void onInfoDuration(int currentDuration, int duration) {
        tv_current.setText(AudioPlayer.generateTime(currentDuration));
        tv_duration.setText(AudioPlayer.generateTime(duration));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayer.getInstance(this).destroy();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                audioPlayer.playOrPause();
                iv_play.setImageResource(audioPlayer.isPause() ?
                        R.mipmap.ic_video_player_btn_play : R.mipmap.ic_video_player_btn_pause);
                break;
        }
    }

    /**
     * 音频焦点处理
     *
     * @param audioStatus
     */
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
