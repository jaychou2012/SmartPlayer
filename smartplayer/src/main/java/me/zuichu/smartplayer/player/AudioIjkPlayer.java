package me.zuichu.smartplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;

/**
 * Created by office on 2018/5/14.
 */

public class AudioIjkPlayer extends BaseAudioPlayer implements IAudioPlayer, SeekBar.OnSeekBarChangeListener, IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
    private static AudioIjkPlayer audioIjkPlayer;
    private static IjkMediaPlayer ijkMediaPlayer;
    private static Context context;
    private SeekBar seekBar;
    private Timer timer;
    private TimerTask timerTask;
    private AudioCallBack audioCallBack;
    private AudioDurationCallBack audioDurationCallBack;
    private AudioBufferCallBack audioBufferCallBack;
    private AudioStatusCallBack audioStatusCallBack;
    private boolean autoPlay = true;
    private AudioManager audioManager;
    private boolean isPause = false;
    private boolean audioFocus = true;
    private long duration = 0;

    private AudioIjkPlayer() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        if (ijkMediaPlayer == null) {
            ijkMediaPlayer = new IjkMediaPlayer();
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        initListener();
    }

    public static AudioIjkPlayer getInstance(Context c) {
        context = c;
        if (audioIjkPlayer == null) {
            audioIjkPlayer = new AudioIjkPlayer();
        }
        return audioIjkPlayer;
    }

    public IjkMediaPlayer getIjkPlayer() {
        return ijkMediaPlayer;
    }

    public void startPlay(String url) {
        if (ijkMediaPlayer != null) {
            try {
                ijkMediaPlayer.setDataSource(url);
                ijkMediaPlayer.setAudioStreamType(AUDIO_STREAM_TYPE);
                ijkMediaPlayer.prepareAsync();
                ijkMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
                showToast(e.getLocalizedMessage());
            }
        }
    }

    public MediaInfo getMediaInfo() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getMediaInfo();
        }
        return null;
    }

    @Override
    public long getDuration() {
        if (ijkMediaPlayer != null) {
            if (duration != 0) {
                return duration;
            }
            duration = ijkMediaPlayer.getDuration();
            return duration;
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setAudioFocus(boolean audioFocus) {
        this.audioFocus = audioFocus;
    }

    @Override
    public void setSpeed(float speed) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public boolean isLooping() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.isLooping();
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        if (ijkMediaPlayer != null) {
            return ijkMediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean isPause() {
        return isPause;
    }

    public void playOrPause() {
        if (isPause) {
            start();
        } else {
            pause();
        }
    }

    @Override
    public void seekTo(long msec) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.seekTo(msec);
        }
    }

    private void initListener() {
        ijkMediaPlayer.setOnInfoListener(this);
        ijkMediaPlayer.setOnCompletionListener(this);
        ijkMediaPlayer.setOnErrorListener(this);
        ijkMediaPlayer.setOnPreparedListener(this);
        ijkMediaPlayer.setOnBufferingUpdateListener(this);
        ijkMediaPlayer.setOnSeekCompleteListener(this);
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

    @Override
    public void initTimer() {
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
        seekBar.setMax((int) getDuration());
    }

    @Override
    public void cancelTimer() {
        handler.removeMessages(MEDIA_PROGRESS);
        handler.removeCallbacksAndMessages(null);
        if (timer != null) {
            timer.cancel();
            timerTask.cancel();
        }
        timer = null;
        timerTask = null;
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    @Override
    public void setLooping(boolean looping) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setLooping(looping);
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    public void setMediacodec(boolean hardCode) {//是否硬解码，0软解码，1硬解码
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", hardCode ? 1 : 0);
        }
    }

    public void setMediacodecAutoRotate(boolean mediacodecAutoRotate) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", mediacodecAutoRotate ? 1 : 0);
        }
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {// 是否自动启动播放，0不是，1是
        this.autoPlay = autoPlay;
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", autoPlay ? 1 : 0);
        }
    }

    public void setLogLevel(boolean log) {
        ijkMediaPlayer.native_setLogLevel(log ? IjkMediaPlayer.IJK_LOG_DEBUG : IjkMediaPlayer.IJK_LOG_SILENT);
    }

    @Override
    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setAudioStreamType(streamtype);
        }
    }

    @Override
    public void start() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.start();
            isPause = false;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PLAYING);
            }
        }
    }

    @Override
    public void pause() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.pause();
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PAUSE);
            }
        }
    }

    @Override
    public void stop() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
        }
    }

    @Override
    public void reset() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
        }
    }

    @Override
    public void release() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.release();
        }
    }

    @Override
    public void destroy() {
        if (ijkMediaPlayer != null) {
            cancelTimer();
            release();
            ijkMediaPlayer = null;
            audioIjkPlayer = null;
            audioManager.abandonAudioFocus(this);
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
            IjkMediaPlayer.native_profileEnd();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(seekBar.getProgress() * getDuration() / seekBar.getMax());
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MEDIA_PROGRESS:
                    seekBar.setProgress((int) getCurrentPosition());
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
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (audioCallBack != null) {
            audioCallBack.onInfo(iMediaPlayer, what, extra);
        }
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                showToast("错误交叉");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(false);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(true);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:

                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:

                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:

                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:

                break;
            case IMediaPlayer.MEDIA_INFO_UNKNOWN:
                showToast("未知的信息");
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:

                break;
        }
        return false;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (audioCallBack != null) {
            audioCallBack.onCompletion(iMediaPlayer);
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (audioCallBack != null) {
            audioCallBack.onError(iMediaPlayer, what, extra);
        }
        switch (what) {
            case IMediaPlayer.MEDIA_ERROR_IO:
                showToast("本地文件或网络流错误");
                break;
            case IMediaPlayer.MEDIA_ERROR_MALFORMED:
                showToast("比特流不符合相关的编码标准和文件规范");
                break;
            case IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                showToast("播放错误");
                break;
            case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                showToast("服务器错误");
                break;
            case IMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                showToast("超时");
                break;
            case IMediaPlayer.MEDIA_ERROR_UNKNOWN:
                showToast("未知错误");
                break;
            case IMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                showToast("不支持的功能");
                break;
        }
        return false;
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        if (audioCallBack != null) {
            audioCallBack.onSeekComplete(iMediaPlayer);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (audioCallBack != null) {
            audioCallBack.onPrepared(iMediaPlayer);
        }
        initTimer();
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        if (seekBar != null) {
            seekBar.setSecondaryProgress((int) ((i / 100.0) * getDuration()));
        }
        if (audioCallBack != null) {
            audioCallBack.onBufferingUpdate(iMediaPlayer, i);
        }
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

        boolean onInfo(IMediaPlayer mp, int what, int extra);

        void onCompletion(IMediaPlayer mp);

        void onSeekComplete(IMediaPlayer mp);

        void onBufferingUpdate(IMediaPlayer mp, int percent);

        void onPrepared(IMediaPlayer mp);

        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    public interface AudioDurationCallBack {
        void onInfoDuration(long currentDuration, long duration);
    }

    public interface AudioBufferCallBack {
        void onInfoBuffer(boolean buffer);
    }

    public interface AudioStatusCallBack {
        void onAudioStatus(int audioStatus);
    }
}
