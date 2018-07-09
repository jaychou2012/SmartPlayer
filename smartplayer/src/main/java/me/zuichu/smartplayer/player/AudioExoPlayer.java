package me.zuichu.smartplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by office on 2018/5/11.
 */

public class AudioExoPlayer extends BaseAudioPlayer implements Player.EventListener, SeekBar.OnSeekBarChangeListener, TextOutput, AudioManager.OnAudioFocusChangeListener {
    private static AudioExoPlayer audioExoPlayer;
    private static SimpleExoPlayer exoPlayer;
    private static Context context;
    private AudioCallBack audioCallBack;
    private AudioInfoCallBack audioInfoCallBack;
    private AudioBufferCallBack audioBufferCallBack;
    private AudioStatusCallBack audioStatusCallBack;
    private boolean autoPlay = true;
    private boolean looping = false;
    private SeekBar seekbar;
    private Timer timer;
    private TimerTask timerTask;
    public static final int MEDIA_PROGRESS = 0;
    private String url = "";
    private DefaultBandwidthMeter BANDWIDTH_METER;
    private DataSource.Factory dataSourceFactory;
    private long progress = 0;
    private List<MediaSource> mediaSourceList;
    private AudioManager audioManager;
    private boolean isPause = false;
    private long duration = 0;
    private boolean audioFocus = true;

    private AudioExoPlayer() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public static AudioExoPlayer getInstance(Context c) {
        context = c;
        if (audioExoPlayer == null) {
            audioExoPlayer = new AudioExoPlayer();
        }
        return audioExoPlayer;
    }

    private void initExoPlayer() {
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        exoPlayer.addListener(this);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void startPlay(String url) {
        this.url = url;
        initExoPlayer();
    }

    public void startPlayList(List<MediaSource> list) {
        mediaSourceList = list;
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(mediaSourceList);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        exoPlayer.addListener(this);
        exoPlayer.prepare(concatenatingMediaSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void startPlayWithText(String url, String textUrl) {
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        Format format = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, C.SELECTION_FLAG_DEFAULT, "en");
        MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(textUrl), format, C.TIME_UNSET);
        MergingMediaSource mergedSource =
                new MergingMediaSource(mediaSource, subtitleSource);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        exoPlayer.addListener(this);
        exoPlayer.prepare(mergedSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void startPlayLoop(String url) {
        this.url = url;
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        exoPlayer.addListener(this);
        exoPlayer.prepare(loopingSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void startPlayLoop(String url, int count) {
        this.url = url;
        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource, count);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        exoPlayer.addListener(this);
        exoPlayer.prepare(loopingSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
        exoPlayer.setAudioAttributes(audioAttributes);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void resetUrl(String url) {
        this.url = url;
        if (exoPlayer != null) {
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
            exoPlayer.prepare(mediaSource, true, true);
            exoPlayer.setPlayWhenReady(autoPlay);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
            exoPlayer.setAudioAttributes(audioAttributes);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } else {
            initExoPlayer();
        }
    }

    public void startPlayRaw(int resId) {
        try {
            DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(resId));
            RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(context);
            rawResourceDataSource.open(dataSpec);
            BANDWIDTH_METER = new DefaultBandwidthMeter();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
            LoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(rawResourceDataSource.getUri());
            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            exoPlayer.addListener(this);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(autoPlay);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
            exoPlayer.setAudioAttributes(audioAttributes);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }
    }

    public void startPlayAssets(int resId) {
        try {
            DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(resId));
            AssetDataSource assetDataSource = new AssetDataSource(context);
            assetDataSource.open(dataSpec);
            BANDWIDTH_METER = new DefaultBandwidthMeter();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
            LoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(assetDataSource.getUri());
            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            exoPlayer.addListener(this);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(autoPlay);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).build();
            exoPlayer.setAudioAttributes(audioAttributes);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } catch (AssetDataSource.AssetDataSourceException e) {
            e.printStackTrace();
        }
    }

    public void addMediaSource(MediaSource mediaSource) {
        mediaSourceList.add(mediaSource);
    }

    public void addMediaSource(List<MediaSource> list) {
        mediaSourceList.clear();
        mediaSourceList.addAll(list);
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public void setAudioCallBack(AudioCallBack callBack) {
        this.audioCallBack = callBack;
    }

    public void setAudioInfoCallBack(AudioInfoCallBack infoCallBack) {
        this.audioInfoCallBack = infoCallBack;
    }

    public void seekTo(long positionMs) {
        exoPlayer.seekTo(positionMs);
    }

    public void setSpeed(float speed) {
        if (exoPlayer != null) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed);
            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public void setSpeed(float speed, float pitch) {
        if (exoPlayer != null) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch);
            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public void setSpeed(float speed, float pitch, boolean skipSilence) {
        if (exoPlayer != null) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch, skipSilence);
            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public void setAudioBufferCallBack(AudioBufferCallBack bufferCallBack) {
        this.audioBufferCallBack = bufferCallBack;
    }

    public void setAudioStatusCallBack(AudioStatusCallBack audioStatusCallBack) {
        this.audioStatusCallBack = audioStatusCallBack;
    }

    public SimpleExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    public long getDuration() {
        if (exoPlayer != null) {
            if (duration != 0) {
                return duration;
            }
            duration = exoPlayer.getDuration();
            return duration;
        }
        return 0;
    }

    public long getCurrentPosition() {
        if (exoPlayer != null) {
            return exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setAudioFocus(boolean audioFocus) {
        this.audioFocus = audioFocus;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
        if (exoPlayer != null) {
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        }
    }

    public void pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PAUSE);
            }
        }
    }

    public void playOrPause() {
        if (isPause) {
            resumePlay();
        } else {
            pause();
        }
    }

    public void setTextOutPut() {
        if (exoPlayer != null) {
            exoPlayer.addTextOutput(this);
        }
    }

    public void resumePlay() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            isPause = false;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_PLAYING);
            }
        }
    }

    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            isPause = true;
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
        }
    }

    public void stopReset() {
        if (exoPlayer != null) {
            exoPlayer.stop(true);
            isPause = true;
        }
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    public void destroy() {
        if (exoPlayer != null) {
            cancelTimer();
            stop();
            release();
            exoPlayer.removeListener(this);
            exoPlayer = null;
            audioExoPlayer = null;
            audioManager.abandonAudioFocus(this);
            if (audioStatusCallBack != null) {
                audioStatusCallBack.onAudioStatus(AUDIO_STATUS_STOP);
            }
        }
    }

    public boolean isLooping() {
        if (exoPlayer != null) {
            return exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL ? true : false;
        }
        return false;
    }

    public boolean isPlaying() {
        if (exoPlayer != null) {
            return exoPlayer.getPlaybackState() == PlaybackState.STATE_PLAYING ? true : false;
        }
        return false;
    }

    public boolean isPause() {
        return isPause;
    }

    /**
     * 0-1
     *
     * @param audioVolume
     */
    public void setVolume(float audioVolume) {
        if (exoPlayer != null) {
            exoPlayer.setVolume(audioVolume);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        if (audioCallBack != null) {
            audioCallBack.onTimelineChanged(timeline, manifest, reason);
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (audioCallBack != null) {
            audioCallBack.onTracksChanged(trackGroups, trackSelections);
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (audioCallBack != null) {
            audioCallBack.onLoadingChanged(isLoading);
        }
        if (seekbar != null) {
            seekbar.setSecondaryProgress((int) (exoPlayer.getBufferedPosition() * seekbar.getMax() / getDuration()));
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (audioCallBack != null) {
            audioCallBack.onPlayerStateChanged(playWhenReady, playbackState);
        }
        switch (playbackState) {
            case PlaybackState.STATE_PLAYING:
                System.out.println("播放状态: STATE_PLAYING");
                duration = exoPlayer.getDuration();
                if (audioInfoCallBack != null) {
                    audioInfoCallBack.onMediaFormat(exoPlayer.getAudioFormat(), exoPlayer.getVideoFormat());
                }
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(false);
                }
                initTimer();
                break;
            case PlaybackState.STATE_BUFFERING:
                System.out.println("播放状态: 缓存完成 playing");
                break;
            case PlaybackState.STATE_CONNECTING:
                System.out.println("播放状态: 连接 CONNECTING");
                break;
            case PlaybackState.STATE_ERROR://错误
                System.out.println("播放状态: 错误 STATE_ERROR");
                break;
            case PlaybackState.STATE_FAST_FORWARDING:
                System.out.println("播放状态: 快速传递");
//                pausePlay();//暂停播放
                break;
            case PlaybackState.STATE_NONE:
                System.out.println("播放状态: 无 STATE_NONE");
                break;
            case PlaybackState.STATE_PAUSED:
                System.out.println("播放状态: 暂停 PAUSED");
                if (audioBufferCallBack != null) {
                    audioBufferCallBack.onInfoBuffer(true);
                }
                break;
            case PlaybackState.STATE_REWINDING:
                System.out.println("播放状态: 倒回 REWINDING");
                break;
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
                System.out.println("播放状态: 跳到下一个");
                break;
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                System.out.println("播放状态: 跳到上一个");
                break;
            case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
                System.out.println("播放状态: 跳到指定的Item");
                break;
            case PlaybackState.STATE_STOPPED:
                System.out.println("播放状态: 停止的 STATE_STOPPED");
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        if (audioCallBack != null) {
            audioCallBack.onRepeatModeChanged(repeatMode);
        }
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        if (audioCallBack != null) {
            audioCallBack.onShuffleModeEnabledChanged(shuffleModeEnabled);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        switch (error.type) {
            case ExoPlaybackException.TYPE_RENDERER:
                showToast("渲染出错");
                break;
            case ExoPlaybackException.TYPE_SOURCE:
                showToast("播放源出错");
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                showToast("未知错误");
                break;
            default:
                showToast("播放错误 " + error.getLocalizedMessage());
                break;
        }
        if (audioCallBack != null) {
            audioCallBack.onPlayerError(error);
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        if (audioCallBack != null) {
            audioCallBack.onPositionDiscontinuity(reason);
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        if (audioCallBack != null) {
            audioCallBack.onPlaybackParametersChanged(playbackParameters);
        }
    }

    @Override
    public void onSeekProcessed() {
        if (audioCallBack != null) {
            audioCallBack.onSeekProcessed();
        }
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekbar = seekBar;
        seekbar.setOnSeekBarChangeListener(this);
    }

    public Format getAudioFormat() {
        if (exoPlayer != null) {
            return exoPlayer.getAudioFormat();
        }
        return null;
    }

    public Format getVideoFormat() {
        if (exoPlayer != null) {
            return exoPlayer.getVideoFormat();
        }
        return null;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MEDIA_PROGRESS:
                    if (seekbar == null) {
                        return;
                    }
                    progress = (seekbar.getMax() * getCurrentPosition()) / getDuration();
                    seekbar.setProgress((int) progress);
                    seekbar.setSecondaryProgress((int) (seekbar.getMax() * exoPlayer.getBufferedPosition() / getDuration()));
                    if (audioInfoCallBack != null) {
                        audioInfoCallBack.onExoInfoDuration(getCurrentPosition(), getDuration());
                    }
                    break;
            }
        }
    };

    private void initTimer() {
        if (seekbar != null) {
            seekbar.setMax(100);
        }
        timer = null;
        timerTask = null;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isPause()) {
                    return;
                }
                handler.obtainMessage(MEDIA_PROGRESS).sendToTarget();
            }
        };
        timer.schedule(timerTask, 0, 1000);
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

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
                    seconds).toString();
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    .toString();
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

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (!audioFocus) {
            return;
        }
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                resumePlay();
                setVolume(1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (isPlaying()) {
                    pause();
                    audioManager.abandonAudioFocus(this);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPlaying()) {
                    setVolume(0.1f);
                }
                break;
        }
    }

    public interface AudioCallBack {

        void onTimelineChanged(Timeline timeline, Object manifest, int reason);

        void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections);

        void onLoadingChanged(boolean isLoading);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onRepeatModeChanged(int repeatMode);

        void onShuffleModeEnabledChanged(boolean shuffleModeEnabled);

        void onPlayerError(ExoPlaybackException error);

        void onPositionDiscontinuity(int reason);

        void onPlaybackParametersChanged(PlaybackParameters playbackParameters);

        void onSeekProcessed();
    }

    public interface AudioInfoCallBack {
        void onExoInfoDuration(long currentDuration, long duration);

        void onMediaFormat(Format audioFormat, Format videoFormat);
    }

    public interface AudioBufferCallBack {
        void onInfoBuffer(boolean buffer);
    }

    public interface AudioStatusCallBack {
        void onAudioStatus(int audioStatus);
    }
}
