package io.github.thejyu.moodist.media;

/**
 * Shared playback state between the Capacitor plugin (driven by the web app)
 * and the foreground service that renders the notification.
 */
final class MediaState {
    /** Whether the notification should exist at all (false after "stop"). */
    static volatile boolean active = false;
    static volatile boolean playing = false;
    static volatile String title = "Moodist";
    static volatile String text = "";

    /** Receives user actions (play / pause / stop) from the notification or media buttons. */
    interface ActionListener {
        void onAction(String action);
    }

    static volatile ActionListener listener;

    static void dispatch(String action) {
        ActionListener l = listener;
        if (l != null) l.onAction(action);
    }

    private MediaState() {}
}
