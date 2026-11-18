package io.github.thejyu.moodist.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Forwards notification button presses to the web app. */
public class MediaActionReceiver extends BroadcastReceiver {

    static final String ACTION_PLAY = "io.github.thejyu.moodist.media.PLAY";
    static final String ACTION_PAUSE = "io.github.thejyu.moodist.media.PAUSE";
    static final String ACTION_STOP = "io.github.thejyu.moodist.media.STOP";
    static final String ACTION_DISMISS = "io.github.thejyu.moodist.media.DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case ACTION_PLAY:
                MediaState.dispatch("play");
                break;
            case ACTION_PAUSE:
                MediaState.dispatch("pause");
                break;
            case ACTION_STOP:
                MediaState.dispatch("stop");
                // If the web app is gone, make sure the notification goes away anyway.
                if (MediaState.listener == null) MediaPlaybackService.shutdown();
                break;
            case ACTION_DISMISS:
                // Notification swiped away while paused.
                MediaState.active = false;
                MediaPlaybackService.shutdown();
                break;
        }
    }
}
