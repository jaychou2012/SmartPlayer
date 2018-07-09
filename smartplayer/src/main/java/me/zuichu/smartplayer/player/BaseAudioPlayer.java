package me.zuichu.smartplayer.player;

import java.util.Locale;

/**
 * Created by office on 2018/5/15.
 */

public class BaseAudioPlayer {
    public static final int AUDIO_STATUS_PLAYING = 0;
    public static final int AUDIO_STATUS_PAUSE = 1;
    public static final int AUDIO_STATUS_STOP = 2;

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
}
