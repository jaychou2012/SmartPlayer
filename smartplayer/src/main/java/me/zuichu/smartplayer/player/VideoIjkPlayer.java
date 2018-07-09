package me.zuichu.smartplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.utils.Utils;
import me.zuichu.smartplayer.view.BaseControl;
import me.zuichu.smartplayer.view.GestureView;
import me.zuichu.smartplayer.view.LoadingView;
import me.zuichu.smartplayer.view.NetView;
import me.zuichu.smartplayer.view.OrientationUtils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.widget.media.IRenderView;
import tv.danmaku.ijk.media.player.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.widget.media.Options;

public class VideoIjkPlayer extends BaseVideoPlayer implements IVideoPlayer, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnInfoListener, IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener, GestureView.GestureSeek,
        OrientationUtils.OrientationListener, NetView.NetClick {
    private View rootView;
    private IjkVideoView ijkVideoView;
    private LoadingView loadingView;
    private GestureView gestureView;
    private NetView netView;
    private Context context;
    private IjkMediaPlayer ijkMediaPlayer;
    private long duration = 0;
    private long current = 0;
    private VideoCallBack videoCallBack;
    private VideoInfoCallBack videoInfoCallBack;
    private int currentVideoWidth = 0;
    private int currentVideoHeight = 0;
    private SurfaceView surfaceView;
    private FrameLayout frameLayout;
    private int playerHeight = 0;
    private int screenMode = MODE_DEFAULT_SIZE;
    public static int SCREEN_MODE_FILL_PARENT = IRenderView.AR_ASPECT_FILL_PARENT;
    public static int SCREEN_MODE_MATCH_PARENT = IRenderView.AR_MATCH_PARENT;
    public static int SCREEN_MODE_WRAP_CONTENT = IRenderView.AR_ASPECT_WRAP_CONTENT;
    public static int SCREEN_MODE_16_9 = IRenderView.AR_16_9_FIT_PARENT;
    public static int SCREEN_MODE_4_3 = IRenderView.AR_4_3_FIT_PARENT;
    private SeekBar seekbar;
    private Timer timer;
    private TimerTask timerTask;
    public static final int MEDIA_PROGRESS = 0;
    private boolean autoPlay = true;
    private boolean looping = false;
    private boolean fullScreen = false;
    private GestureDetector gestureDetector;
    private OrientationUtils orientationUtils;
    private BaseControl control;
    private int seekCount = 3000;
    private long seek = 0;
    private boolean netAutoPlay = false;
    private boolean isPause = false;
    private boolean portrait = false;

    public VideoIjkPlayer(Context context) {
        super(context);
        init(context);
    }

    public VideoIjkPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoIjkPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_ijk, this);

        ijkVideoView = rootView.findViewById(R.id.videoview);
        loadingView = rootView.findViewById(R.id.loadingView);
        gestureView = rootView.findViewById(R.id.gestureView);
        netView = rootView.findViewById(R.id.netView);

        ijkMediaPlayer = new IjkMediaPlayer();
        ijkVideoView.setIjkMediaPlayer(ijkMediaPlayer);
        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(false);
        gestureView.setGestureSeek(this);
        setOnTouchListener(this);
        orientationUtils = new OrientationUtils(getContext());
        orientationUtils.setOrientationListener(this);
        orientationUtils.setOrientationEnable();
        netView.setNetClick(this);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    public void setControlView(BaseControl controlView) {
        this.control = controlView;
        controlView.setOrientation(true);
        setSeekBar(controlView.getSeekBar());
    }

    public void setVideoCallBack(VideoCallBack callBack) {
        this.videoCallBack = callBack;
    }

    public void setVideoInfoCallBack(VideoInfoCallBack infoCallBack) {
        this.videoInfoCallBack = infoCallBack;
    }

    public IjkMediaPlayer getMediaPlayer() {
        return ijkMediaPlayer;
    }

    public IjkVideoView getIjkVideoView() {
        return ijkVideoView;
    }

    public void prepare(String url) {
        ijkVideoView.setVideoURI(Uri.parse(url));
        ijkVideoView.setOnCompletionListener(this);
        ijkVideoView.setOnInfoListener(this);
        ijkVideoView.setOnPreparedListener(this);
        ijkVideoView.setOnErrorListener(this);
    }

    public void prepare(String url, String list) {

    }

    public int getVideoWidth() {
        return currentVideoWidth;
    }

    public int getVideoHeight() {
        return currentVideoHeight;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setNetAutoPlay(boolean autoPlay) {
        this.netAutoPlay = autoPlay;
        netView.setNetAutoPlay(autoPlay);
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
            current = ijkMediaPlayer.getCurrentPosition();
            return current;
        }
        return 0;
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
        return ijkVideoView.isPlaying();
    }

    public void playOrPause() {
        if (isPause) {
            start();
        } else {
            pause();
        }
        control.setPlayStatus(isPause());
    }

    @Override
    public void seekTo(long msec) {
        if (ijkMediaPlayer != null) {
            loadingView.showLoading();
            ijkMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public void setVolume(float audioVolume) {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setVolume(audioVolume, audioVolume);
        }
    }

    @Override
    public void initTimer() {
        timer = null;
        timerTask = null;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (ijkMediaPlayer == null || isPause) {
                    return;
                }
                handler.obtainMessage(MEDIA_PROGRESS).sendToTarget();
            }
        };
        timer.schedule(timerTask, 0, 1000);
        seekbar.setMax((int) getDuration());
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

    public void setHardCodec(boolean hardCodec) {
        Options options = new Options();
        options.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", hardCodec ? 1 : 0);
        ijkVideoView.setOptions(options);
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = looping;
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setLooping(looping);
        }
    }

    @Override
    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
        Options options = new Options();
        options.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", autoPlay ? 1 : 0);// 是否自动启动播放，0不是，1是
        ijkVideoView.setOptions(options);
    }

    @Override
    public void setSeekBar(SeekBar seekBar) {
        this.seekbar = seekBar;
        seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        ijkMediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setScreenMode(int mode) {
        ijkVideoView.setCurrentAspectRatio(mode);
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        if (fullScreen) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            frameLayout.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, Utils.dp2px(context, playerHeight));
            frameLayout.setLayoutParams(layoutParams);
        }
        setWindowFullScreen(fullScreen);
    }

    public void setWindowFullScreen(boolean fullScreen) {
        if (!fullScreen) {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            ((Activity) context).getWindow().setAttributes(lp);
            ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            if (portrait) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            ((Activity) context).getWindow().setAttributes(lp);
            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        if (control != null) {
            control.setOrientation(!fullScreen);
        }
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

    public void setPlayerParent(FrameLayout frameLayout, int playerHeightDp) {
        this.frameLayout = frameLayout;
        this.playerHeight = playerHeightDp;
    }

    @Override
    public void start() {
        ijkVideoView.start();
        isPause = false;
    }

    @Override
    public void pause() {
        ijkVideoView.pause();
        isPause = true;
    }

    public void resume() {
        ijkVideoView.resume();
        isPause = false;
    }

    @Override
    public void stop() {
        ijkVideoView.stopPlayback();
    }

    @Override
    public void reset() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
        }
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void release() {
        ijkVideoView.release(true);
    }

    @Override
    public void destroy() {
        cancelTimer();
        orientationUtils.setOrientationDisable();
        ijkVideoView.stopPlayback();
        ijkVideoView.release(true);
        ijkMediaPlayer = null;
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (videoCallBack != null) {
            videoCallBack.onCompletion(iMediaPlayer);
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (videoCallBack != null) {
            videoCallBack.onInfo(iMediaPlayer, what, extra);
        }
        switch (what) {
            case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                loadingView.showLoading();
                break;
            case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
            case IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                loadingView.hideLoading();
                if (isPause) {
                    pause();
                } else {
                    isPause = false;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (videoCallBack != null) {
            videoCallBack.onError(iMediaPlayer, what, extra);
        }
        switch (what) {
            case IjkMediaPlayer.MEDIA_ERROR_MALFORMED:
                showToast("MEDIA_ERROR_MALFORMED !");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                showToast("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK!");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                showToast("MEDIA_ERROR_UNSUPPORTED!");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_SERVER_DIED:
                showToast("MEDIA_ERROR_SERVER_DIED !");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_IO:
                showToast("MEDIA_ERROR_IO !");
                break;
            case IjkMediaPlayer.MEDIA_INFO_TIMED_TEXT_ERROR:
                showToast("MEDIA_INFO_TIMED_TEXT_ERROR !");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_TIMED_OUT:
                showToast("链接超时");
                break;
            case IjkMediaPlayer.MEDIA_ERROR_UNKNOWN:
                showToast("获取不到视频源");
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (videoInfoCallBack != null) {
            videoInfoCallBack.onPrepared(iMediaPlayer);
        }
        ijkMediaPlayer = (IjkMediaPlayer) iMediaPlayer;
        ijkMediaPlayer.setOnVideoSizeChangedListener(this);
        if (autoPlay) {
            iMediaPlayer.start();
            isPause = false;
        } else {
            iMediaPlayer.pause();
            isPause = true;
        }
        initTimer();
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height,
                                   int sar_num, int sar_den) {
        if (videoCallBack != null) {
            videoCallBack.onVideoSizeChanged(iMediaPlayer, width, height);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MEDIA_PROGRESS:
                    seekbar.setProgress((int) getCurrentPosition());
                    if (videoInfoCallBack != null) {
                        videoInfoCallBack.onInfoDuration(current, getDuration());
                    }
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

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seek = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(seekBar.getProgress());
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
        ijkVideoView.seekTo((int) seek);
        loadingView.hideLoading();
        if (duration != 0) {
            gestureView.showSeekView(add, seek, getDuration());
            seekbar.setProgress((int) seek);
        }
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

    public interface VideoCallBack {

        boolean onInfo(IMediaPlayer mp, int what, int extra);

        void onCompletion(IMediaPlayer mp);

        void onSeekComplete(IMediaPlayer mp);

        void onBufferingUpdate(IMediaPlayer mp, int percent);

        void onVideoSizeChanged(IMediaPlayer mp, int width, int height);

        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    public interface VideoInfoCallBack {

        void onInfoDuration(long currentDuration, long duration);

        void onPrepared(IMediaPlayer mp);
    }
}
