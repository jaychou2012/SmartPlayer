package me.zuichu.smartplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import me.zuichu.smartplayer.R;
import me.zuichu.smartplayer.utils.Utils;
import me.zuichu.smartplayer.view.BaseControl;
import me.zuichu.smartplayer.view.GestureView;
import me.zuichu.smartplayer.view.LoadingView;
import me.zuichu.smartplayer.view.NetView;
import me.zuichu.smartplayer.view.OrientationUtils;

/**
 * Created by office on 2018/5/16.
 */

public class VideoPlayer extends BaseVideoPlayer implements IVideoPlayer, MediaPlayer.OnInfoListener
        , MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnTimedTextListener, SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback, OrientationUtils.OrientationListener, GestureView.GestureSeek, View.OnTouchListener, NetView.NetClick {
    private View rootView;
    private SurfaceView surfaceView;
    private LoadingView loadingView;
    private GestureView gestureView;
    private NetView netView;
    private Context context;
    private MediaPlayer mediaPlayer;
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
    private SurfaceHolder surfaceHolder;

    public VideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_mediaplayer, this);

        surfaceView = rootView.findViewById(R.id.sv);
        loadingView = rootView.findViewById(R.id.loadingView);
        gestureView = rootView.findViewById(R.id.gestureView);
        netView = rootView.findViewById(R.id.netView);

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(false);
        gestureView.setGestureSeek(this);
        setOnTouchListener(this);
        orientationUtils = new OrientationUtils(getContext());
        orientationUtils.setOrientationListener(this);
        orientationUtils.setOrientationEnable();
        netView.setNetClick(this);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setVideoCallBack(VideoCallBack callBack) {
        this.videoCallBack = callBack;
    }

    public void setVideoInfoCallBack(VideoInfoCallBack infoCallBack) {
        this.videoInfoCallBack = infoCallBack;
    }

    public int getVideoWidth() {
        return currentVideoWidth;
    }

    public int getVideoHeight() {
        return currentVideoHeight;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            if (duration != 0) {
                return duration;
            }
            duration = mediaPlayer.getDuration();
            return duration;
        }
        return 0;
    }

    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            current = mediaPlayer.getCurrentPosition();
            return current;
        }
        return 0;
    }

    @Override
    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } else {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                mediaPlayer.pause();
            }
        }
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
    public boolean isLooping() {
        if (mediaPlayer != null) {
            return mediaPlayer.isLooping();
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public void seekTo(long msec) {
        if (mediaPlayer != null) {
            loadingView.showLoading();
            mediaPlayer.seekTo((int) msec);
        }
    }

    public void setWakeMode(int mode) {
        if (mediaPlayer != null) {
            mediaPlayer.setWakeMode(context, mode);
        }
    }

    public void setDataSoure(String path) {
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(FileDescriptor fileDescriptor) {
        try {
            mediaPlayer.setDataSource(fileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(Uri uri) {
        try {
            mediaPlayer.setDataSource(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initListener();
    }

    public void setDataSoure(FileDescriptor fd, long offset, long length) {
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
        mediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void setVolume(float audioVolume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(audioVolume, audioVolume);
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
                if (mediaPlayer == null || isPause) {
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

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mediaPlayer != null) {
            mediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    @Override
    public void setLooping(boolean looping) {
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(looping);
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

    public void setPlayerParent(FrameLayout frameLayout, int playerHeightDp) {
        this.frameLayout = frameLayout;
        this.playerHeight = playerHeightDp;
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        if (mediaPlayer != null) {
            mediaPlayer.setAudioStreamType(streamtype);
        }
    }

    @Override
    public void setScreenMode(int mode) {
        this.screenMode = mode;
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
        setMode(screenMode, surfaceView, currentVideoWidth, currentVideoHeight);
    }

    public void setPortraitFullScreen(boolean portrait) {
        this.portrait = portrait;
    }

    public void setPrepare() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPrepareAsync() {
        if (mediaPlayer != null) {
            mediaPlayer.prepareAsync();
        }
    }

    public void setControlView(BaseControl controlView) {
        this.control = controlView;
        controlView.setOrientation(true);
        setSeekBar(controlView.getSeekBar());
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setLock(boolean lock) {
        if (lock) {
            orientationUtils.setOrientationEnable();
        } else {
            orientationUtils.setOrientationDisable();
        }
        gestureView.setEnAble(lock);
    }

    @Override
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPause = false;
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Override
    public void destroy() {
        if (mediaPlayer != null) {
            cancelTimer();
            stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (videoCallBack != null) {
            videoCallBack.onInfo(mp, what, extra);
        }
        switch (what) {
            case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:

                break;
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                showToast("错误交叉");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                loadingView.hideLoading();
                if (isPause) {
                    pause();
                } else {
                    isPause = false;
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                loadingView.showLoading();
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
        if (videoCallBack != null) {
            videoCallBack.onCompletion(mp);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (videoInfoCallBack != null) {
            videoInfoCallBack.onPrepared(mp);
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
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (videoCallBack != null) {
            videoCallBack.onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (videoCallBack != null) {
            videoCallBack.onSeekComplete(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (videoCallBack != null) {
            videoCallBack.onError(mp, what, extra);
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

    @Override
    public void onTimedText(MediaPlayer mp, TimedText text) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        seekTo(seekBar.getProgress());
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (videoCallBack != null) {
            videoCallBack.onVideoSizeChanged(mp, width, height);
        }
        currentVideoWidth = width;
        currentVideoHeight = height;
        setMode(screenMode, surfaceView, width, height);
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
        mediaPlayer.seekTo((int) seek);
        loadingView.hideLoading();
        if (duration != 0) {
            gestureView.showSeekView(add, seek, getDuration());
            seekbar.setProgress((int) seek);
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

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        if (!isPlaying()) {
            start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    public void orientation(int direction) {
        if (direction == OrientationUtils.ORIENTATION_PORTRAIT) {
            setFullScreen(false);
        } else {
            setFullScreen(true);
        }
    }

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
