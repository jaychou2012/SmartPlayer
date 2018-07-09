package me.zuichu.smartplayer.player;

import android.media.AudioManager;
import android.widget.SeekBar;

/**
 * Created by office on 2018/5/16.
 */

public interface IVideoPlayer {
    int MEDIA_PROGRESS = 0;

    int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    int MODE_ORI_SIZE = 0;
    int MODE_FIT_SIZE = 5;

    int MODE_DEFAULT_SIZE = MODE_FIT_SIZE;

    int MODE_FULL_SIZE = 1;

    int MODE_16_9_SIZE = 2;

    int MODE_4_3_SIZE = 3;

    long getDuration();

    long getCurrentPosition();

    void setSpeed(float speed);

    boolean isLooping();

    boolean isPlaying();

    boolean isPause();

    void seekTo(long msec);

    void setVolume(float audioVolume);

    void initTimer();

    void cancelTimer();

    void setScreenOnWhilePlaying(boolean screenOn);

    void setLooping(boolean looping);

    void setAutoPlay(boolean autoPlay);

    void setSeekBar(SeekBar seekBar);

    void setAudioStreamType(int streamtype);

    void setScreenMode(int mode);

    void setFullScreen(boolean fullScreen);

    void start();

    void pause();

    void stop();

    void reset();

    void release();

    void destroy();
}
