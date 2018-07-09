package me.zuichu.smartplayer.player;

import android.media.AudioManager;
import android.widget.SeekBar;

/**
 * Created by office on 2018/5/15.
 */

public interface IAudioPlayer {

    int MEDIA_PROGRESS = 0;

    int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    long getDuration();

    long getCurrentPosition();

    void setSpeed(float speed);

    boolean isLooping();

    boolean isPlaying();

    void seekTo(long msec);

    void setVolume(float leftVolume, float rightVolume);

    void initTimer();

    void cancelTimer();

    void setScreenOnWhilePlaying(boolean screenOn);

    void setLooping(boolean looping);

    void setAutoPlay(boolean autoPlay);

    void setSeekBar(SeekBar seekBar);

    void setAudioStreamType(int streamtype);

    void start();

    void pause();

    void stop();

    void reset();

    void release();

    void destroy();

}
