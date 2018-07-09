package me.zuichu.smartplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by office on 2018/5/10.
 */

public class AudioPlayer extends BaseAudioPlayer implements MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnTimedTextListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {
    private static AudioPlayer audioPlayer;
    private static MediaPlayer mediaPlayer;
    private static Context context;
    private AudioCallBack audioCallBack;
    private AudioDurationCallBack audioDurationCallBack;
    private AudioBufferCallBack audioBufferCallBack;
    private AudioStatusCallBack audioStatusCallBack;
    private boolean autoPlay = true;
    private boolean looping = false;
    private boolean audioFocus = true;
    private SeekBar seekbar;
    private Timer timer;
    private TimerTask timerTask;
    public static final int MEDIA_PROGRESS = 0;
    private AudioManager audioManager;
    private boolean isPause = false;
    private long duration = 0;

    private AudioPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public static AudioPlayer getInstance(Context c) {
        context = c;
        if (audioPlayer == null) {
            audioPlayer = new AudioPlayer();
        }
        return audioPlayer;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            if (duration != 0) {
                return (int) duration;
            }
            duration = mediaPlayer.getDuration();
            return (int) duration;
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    private void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } else {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                mediaPlayer.pause();
            }
        }
    }

    public void setAudioFocus(boolean audioFocus) {
        this.audioFocus = audioFocus;
    }

    public boolean isLooping() {
        if (mediaPlayer != null) {
            return mediaPlayer.isLooping();
        }
        return false;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean isPause() {
        return isPause;
    }

    public void seekTo(int msec) {
        mediaPlayer.seekTo(msec);
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(surfaceHolder);
        }
    }

    public void setWakeMode(int mode) {
        if (mediaPlayer != null) {
            mediaPlayer.setWakeMode(context, mode);
        }
    }

    public void setDataSoure(String path) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(FileDescriptor fileDescriptor) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(fileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(Uri uri) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(FileDescriptor fd, long offset, long length) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(fd, offset, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    private void initListener() {
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnTimedTextListener(this);
    }

    private void initTimer() {
        timer = null;
        timerTask = null;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(MEDIA_PROGRESS).sendToTarget();
            }
        };
        timer.schedule(timerTask, 0, 1000);
        seekbar.setMax(getDuration());
    }

    private void cancelTimer() {
        handler.removeMessages(MEDIA_PROGRESS);
        handler.removeCallbacksAndMessages(null);
        if (timer != null) {
            timer.cancel();
            timerTask.cancel();
        }
        timer = null;
        timerTask = null;
    }

    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mediaPlayer != null) {
            mediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    public void setLooping(boolean looping) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekbar = seekBar;
        seekbar.setOnSeekBarChangeListener(this);
    }

    public void setStreamType(int streamType) {
        if (mediaPlayer != null) {
            mediaPlayer.setAudioStreamType(streamType);
        }
    }

    private void setPrepare() {
        if (mediaPlayer != null) {
            try {
                audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPrepareAsync() {
        if (mediaPlayer != null) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mediaPlayer.prepareAsync();
        }
    }

    public void setAudioCallBack(AudioCallBack callBack) {
        this.audioCallBack = callBack;
    }

    public void setAudioDurationCallBack(AudioDurationCallBack durationCallBack) {
        this.audioDurationCallBack = durationCallBack;
    }

    public void setAudioBufferCallBack(AudioBufferCallBack bufferCallBack) {
        this.audioBufferCallBack = bufferCallBack;
    }

    public void setAudioStatusCallBack(AudioStatusCallBack audioStatusCallBack) {
        this.audioStatusCallBack = audioStatusCallBack;
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPause = false;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PLAYING);
            }
        }
    }

    public void startAsync() {
        if (mediaPlayer != null) {
            setPrepareAsync();
        }
    }

    public void playOrPause() {
        if (isPause) {
            start();
        } else {
            pause();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PAUSE);
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            cancelTimer();
            stop();
            mediaPlayer.release();
            mediaPlayer = null;
            audioPlayer = null;
            audioManager.abandonAudioFocus(this);
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (audioCallBack != null) {
            audioCallBack.onInfo(mp, what, extra);
        }
        switch (what) {
            case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:

                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                showToast("错误交叉");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(false);
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(true);
                }
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:

                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:

                break;
            case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:

                break;
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                showToast("未知的信息");
                break;
            case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:

                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (audioCallBack != null) {
            audioCallBack.onCompletion(mp);
        }
    }

    @Override
    public void onTimedText(MediaPlayer mp, TimedText text) {

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (audioCallBack != null) {
            audioCallBack.onSeekComplete(mp);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (seekbar != null) {
            seekbar.setSecondaryProgress((int) ((percent / 100.0) * getDuration()));
        }
        if (audioCallBack != null) {
            audioCallBack.onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (audioCallBack != null) {
            audioCallBack.onPrepared(mp);
        }
        if (autoPlay) {
            mp.start();
            isPause = false;
        } else {
            mp.pause();
            isPause = true;
        }
        initTimer();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (audioCallBack != null) {
            audioCallBack.onError(mp, what, extra);
        }
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_IO:
                showToast("本地文件或网络流错误");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                showToast("比特流不符合相关的编码标准和文件规范");
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                showToast("播放错误");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                showToast("服务器错误");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                showToast("超时");
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                showToast("未知错误");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                showToast("不支持的功能");
                break;
        }
        return false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MEDIA_PROGRESS:
                    seekbar.setProgress(getCurrentPosition());
                    if (audioDurationCallBack != null) {
                        audioDurationCallBack.onInfoDuration(getCurrentPosition(), getDuration());
                    }
                    break;
            }
        }
    };

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeMessages(MEDIA_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(seekBar.getProgress());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (!audioFocus) {
            return;
        }
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //你已经得到了音频焦点。
                System.out.println("音频AUDIOFOCUS_GAIN");
                start();
                setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //你已经失去了音频焦点很长时间了。你必须停止所有的音频播放
                System.out.println("音频AUDIOFOCUS_LOSS");
                if (isPlaying()) {
                    pause();
                    audioManager.abandonAudioFocus(this);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //你暂时失去了音频焦点
                System.out.println("音频AUDIOFOCUS_LOSS_TRANSIENT");
                if (isPlaying()) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //你暂时失去了音频焦点，但你可以小声地继续播放音频（低音量）而不是完全扼杀音频。
                System.out.println("音频AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                if (isPlaying()) {
                    setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    public interface AudioCallBack {

        boolean onInfo(MediaPlayer mp, int what, int extra);

        void onCompletion(MediaPlayer mp);

        void onSeekComplete(MediaPlayer mp);

        void onBufferingUpdate(MediaPlayer mp, int percent);

        void onPrepared(MediaPlayer mp);

        boolean onError(MediaPlayer mp, int what, int extra);
    }

    public interface AudioDurationCallBack {
        void onInfoDuration(int currentDuration, int duration);
    }

    public interface AudioBufferCallBack {
        void onInfoBuffer(boolean buffer);
    }

    public interface AudioStatusCallBack {
        void onAudioStatus(int audioStatus);
    }
}
