package me.zuichu.smartplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
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
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
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
import java.util.Timer;
import java.util.TimerTask;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.utils.Utils;
import me.zuichu.smartplayer.view.AspectRatioFrameLayout;
import me.zuichu.smartplayer.view.BaseControl;
import me.zuichu.smartplayer.view.GestureView;
import me.zuichu.smartplayer.view.LoadingView;
import me.zuichu.smartplayer.view.NetView;
import me.zuichu.smartplayer.view.OrientationUtils;

public class VideoExoPlayer extends BaseVideoPlayer implements IVideoPlayer, GestureView.GestureSeek,
        View.OnTouchListener, OrientationUtils.OrientationListener,
        NetView.NetClick, Player.EventListener, SeekBar.OnSeekBarChangeListener, TextureView.SurfaceTextureListener, SimpleExoPlayer.VideoListener {
    private View rootView;
    private Context context;
    private AspectRatioFrameLayout aspectRatioFrameLayout;
    private TextureView textureView;
    private LoadingView loadingView;
    private GestureView gestureView;
    private NetView netView;
    private SimpleExoPlayer exoPlayer;
    private DefaultBandwidthMeter BANDWIDTH_METER;
    private DataSource.Factory dataSourceFactory;
    private SeekBar seekbar;
    private Timer timer;
    private TimerTask timerTask;
    public static final int MEDIA_PROGRESS = 0;
    private boolean autoPlay = true;
    private boolean looping = false;
    private VideoCallBack videoCallBack;
    private VideoInfoCallBack videoInfoCallBack;
    private int currentVideoWidth = 0;
    private int currentVideoHeight = 0;
    private FrameLayout frameLayout;
    private int screenMode = MODE_DEFAULT_SIZE;
    private boolean fullScreen = false;
    private boolean isPause = false;
    private BaseControl control;
    private OrientationUtils orientationUtils;
    private GestureDetector gestureDetector;
    private int seekCount = 3000;
    private long seek = 0;
    private boolean netAutoPlay = false;
    private boolean portrait = false;
    private long duration = 0;
    private long current = 0;
    private int playerHeight = 0;
    private List<MediaSource> mediaSourceList;
    private String url = "";
    private long progress = 0;
    private float videoAspectRatio = 1;

    public VideoExoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoExoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoExoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_exoplayer, this);

        aspectRatioFrameLayout = rootView.findViewById(R.id.arfl);
        textureView = rootView.findViewById(R.id.sv);
        loadingView = rootView.findViewById(R.id.loadingView);
        gestureView = rootView.findViewById(R.id.gestureView);
        netView = rootView.findViewById(R.id.netView);

        BANDWIDTH_METER = new DefaultBandwidthMeter();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(BANDWIDTH_METER);
        LoadControl loadControl = new DefaultLoadControl();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getPackageName()), BANDWIDTH_METER);
        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        }
        exoPlayer.addVideoListener(this);
        aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        textureView.setSurfaceTextureListener(this);
        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(false);
        gestureView.setGestureSeek(this);
        setOnTouchListener(this);
        orientationUtils = new OrientationUtils(getContext());
        orientationUtils.setOrientationListener(this);
        orientationUtils.setOrientationEnable();
        netView.setNetClick(this);
    }

    public void startPlay(String url) {
        this.url = url;
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
        exoPlayer.setVideoTextureView(textureView);
        exoPlayer.addListener(this);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(autoPlay);
        initTimer();
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
        initTimer();
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
        initTimer();
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
        initTimer();
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
        initTimer();
    }

    public void resetUrl(String url) {
        this.url = url;
        if (exoPlayer != null) {
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
            exoPlayer.prepare(mediaSource, true, true);
            exoPlayer.setPlayWhenReady(autoPlay);
            initTimer();
        } else {
            initExoPlayer();
        }
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
        initTimer();
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
            initTimer();
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
            initTimer();
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

    @Override
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

    @Override
    public long getCurrentPosition() {
        if (exoPlayer != null) {
            current = exoPlayer.getCurrentPosition();
            return current;
        }
        return 0;
    }

    @Override
    public void setSpeed(float speed) {
        if (exoPlayer != null) {
            PlaybackParameters playbackParameters = new PlaybackParameters(speed);
            exoPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    @Override
    public boolean isLooping() {
        if (exoPlayer != null) {
            return exoPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL ? true : false;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        if (exoPlayer != null) {
            return exoPlayer.getPlaybackState() == PlaybackState.STATE_PLAYING ? true : false;
        }
        return false;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void seekTo(long msec) {
        if (exoPlayer != null) {
            exoPlayer.seekTo(msec);
        }
    }

    /**
     * 0-1
     *
     * @param audioVolume
     */
    @Override
    public void setVolume(float audioVolume) {
        if (exoPlayer != null) {
            exoPlayer.setVolume(audioVolume);
        }
    }

    @Override
    public void initTimer() {
        if (seekbar != null) {
            seekbar.setMax(100);
        }
        timer = null;
        timerTask = null;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (exoPlayer == null || isPause) {
                    return;
                }
                handler.obtainMessage(MEDIA_PROGRESS).sendToTarget();
            }
        };
        timer.schedule(timerTask, 0, 1000);
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
        if (screenOn) {
            ((Activity) getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void setControlView(BaseControl controlView) {
        this.control = controlView;
        controlView.setOrientation(true);
        setSeekBar(controlView.getSeekBar());
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = looping;
        if (exoPlayer != null) {
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        }
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    @Override
    public void setSeekBar(SeekBar seekBar) {
        this.seekbar = seekBar;
        seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setAudioStreamType(int streamtype) {

    }

    @Override
    public void setScreenMode(int mode) {
        this.screenMode = mode;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void playOrPause() {
        if (isPause) {
            start();
        } else {
            pause();
        }
        control.setPlayStatus(isPause());
    }

    public void setPlayerParent(FrameLayout frameLayout, int playerHeightDp) {
        this.frameLayout = frameLayout;
        this.playerHeight = playerHeightDp;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        if (fullScreen) {
            if (portrait) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            frameLayout.setLayoutParams(layoutParams);
        } else {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Utils.dp2px(context, playerHeight));
            frameLayout.setLayoutParams(layoutParams);
        }
        if (control != null) {
            control.setOrientation(!fullScreen);
        }
        setWindowFullScreen(fullScreen);
//        setMode(screenMode, textureView, currentVideoWidth, currentVideoHeight);
    }

    public void setPortraitFullScreen(boolean portrait) {
        this.portrait = portrait;
    }

    public void setLock(boolean lock) {
        if (lock) {
            orientationUtils.setOrientationEnable();
        } else {
            orientationUtils.setOrientationDisable();
        }
        gestureView.setEnAble(lock);
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

    @Override
    public void start() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            isPause = false;
        }
    }

    @Override
    public void pause() {
        if (exoPlayer != null && isPlaying()) {
            exoPlayer.setPlayWhenReady(false);
            isPause = true;
        }
    }

    @Override
    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
    }

    @Override
    public void reset() {
        if (exoPlayer != null) {
            exoPlayer.stop(true);
        }
    }

    @Override
    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }

    @Override
    public void destroy() {
        if (exoPlayer != null) {
            cancelTimer();
            stop();
            release();
            exoPlayer.removeListener(this);
            exoPlayer = null;
        }
    }

    @Override
    public void gestureSeek(boolean add) {
        seekCount = (int) (duration / 400);
        if (add) {
            seek = seek + seekCount;
        } else {
            seek = seek - seekCount;
        }
        if (seek <= 0) {
            seek = 0;
        }
        if (seek > duration) {
            seek = duration;
        }
        exoPlayer.seekTo(seek);
        loadingView.hideLoading();
        if (duration != 0) {
            gestureView.showSeekView(add, seek, getDuration());
            seekbar.setProgress((int) ((seek * 100) / duration));
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureView.onTouch(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                control.setControl();
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void orientation(int direction) {
        if (direction == OrientationUtils.ORIENTATION_PORTRAIT) {
            setFullScreen(false);
        } else {
            setFullScreen(true);
        }
    }

    @Override
    public void netClick(int net) {
        switch (net) {
            case NetView.NET_TYPE_MOBILE:
                start();
                netView.setVisibility(View.GONE);
                break;
            case NetView.NET_TYPE_WIFI:

                break;
            case NetView.NET_TYPE_NONET:

                break;
        }
    }

    @Override
    public void noNetOrMobileNet(int net) {
        switch (net) {
            case NetView.NET_TYPE_MOBILE:
                if (netAutoPlay) {
                    start();
                } else {
                    pause();
                }
                break;
            case NetView.NET_TYPE_WIFI:
                start();
                break;
            case NetView.NET_TYPE_NONET:
                pause();
                break;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (seekbar != null) {
            seekbar.setSecondaryProgress((int) (exoPlayer.getBufferedPosition() * seekbar.getMax() / getDuration()));
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case PlaybackState.STATE_PLAYING:
                System.out.println("播放状态: STATE_PLAYING");
                duration = exoPlayer.getDuration();
                loadingView.hideLoading();
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
                loadingView.showLoading();
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

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

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
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seek = (progress * getDuration()) / 100;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo((seekBar.getProgress() * duration) / 100);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        videoAspectRatio =
                (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;
        if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
            // We will apply a rotation 90/270 degree to the output texture of the TextureView.
            // In this case, the output video's width and height will be swapped.
            videoAspectRatio = 1 / videoAspectRatio;
        }
        System.out.println("视频：" + width + "  " + height + "  "
                + unappliedRotationDegrees + "  " + pixelWidthHeightRatio
                + "  " + videoAspectRatio);
        aspectRatioFrameLayout.setAspectRatio(videoAspectRatio);
        aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//        switch (screenMode) {
//            case IVideoPlayer.MODE_ORI_SIZE:
//                aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                break;
//            case IVideoPlayer.MODE_FIT_SIZE:
//                aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                break;
//            case IVideoPlayer.MODE_FULL_SIZE:
//                aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                break;
//            case IVideoPlayer.MODE_4_3_SIZE:
//                aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                break;
//            case IVideoPlayer.MODE_16_9_SIZE:
//                aspectRatioFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//                break;
//        }

    }

    @Override
    public void onRenderedFirstFrame() {

    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            gestureView.onDown(e);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            gestureView.onScroll(e1, e2, distanceX, distanceY);
            return true;
        }
    }

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
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
                    if (control != null) {
                        control.setDuration(current, getDuration());
                    }
                    if (loadingView.isLoading()) {
                        loadingView.setLoading();
                    }
                    break;
            }
        }
    };

    public interface VideoCallBack {

        boolean onInfo(MediaPlayer mp, int what, int extra);

        void onCompletion(MediaPlayer mp);

        void onSeekComplete(MediaPlayer mp);

        void onBufferingUpdate(MediaPlayer mp, int percent);

        void onVideoSizeChanged(MediaPlayer mp, int width, int height);

        boolean onError(MediaPlayer mp, int what, int extra);
    }

    public interface VideoInfoCallBack {
        void onInfoDuration(long currentDuration, long duration);

        void onPrepared(MediaPlayer mp);
    }
}
