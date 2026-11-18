package io.github.thejyu.moodist.media;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import io.github.thejyu.moodist.MainActivity;
import io.github.thejyu.moodist.R;

/**
 * Foreground service that keeps the app alive while sounds play (audio itself is
 * produced by the WebView) and shows a media notification with play/pause/stop.
 */
public class MediaPlaybackService extends Service {

    private static final String CHANNEL_ID = "playback";
    private static final int NOTIFICATION_ID = 1001;

    @Nullable
    private static MediaPlaybackService instance;

    private MediaSessionCompat session;
    private boolean inForeground = false;

    /** Starts the service, or refreshes it if already running. Call on the main thread. */
    static void sync(Context context) {
        if (instance != null) {
            instance.refresh();
        } else if (MediaState.active && MediaState.playing) {
            Intent intent = new Intent(context, MediaPlaybackService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    /** Removes the notification and stops the service. Call on the main thread. */
    static void shutdown() {
        if (instance != null) instance.stopAll();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createChannel();

        session = new MediaSessionCompat(this, "Moodist");
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                MediaState.dispatch("play");
            }

            @Override
            public void onPause() {
                MediaState.dispatch("pause");
            }

            @Override
            public void onStop() {
                MediaState.dispatch("stop");
            }
        });
        session.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refresh();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        if (session != null) {
            session.setActive(false);
            session.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void refresh() {
        if (!MediaState.active) {
            stopAll();
            return;
        }

        boolean playing = MediaState.playing;

        session.setMetadata(
            new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, MediaState.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MediaState.text)
                .build()
        );
        session.setPlaybackState(
            new PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                )
                .setState(
                    playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    playing ? 1f : 0f
                )
                .build()
        );

        Notification notification = buildNotification(playing);

        if (playing) {
            startForeground(NOTIFICATION_ID, notification);
            inForeground = true;
        } else {
            // Keep the notification (so the user can resume) but let the system
            // treat it as dismissible and allow the process to be reclaimed.
            if (inForeground) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH);
                } else {
                    stopForeground(false);
                }
                inForeground = false;
            }
            notify(notification);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void notify(Notification notification) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (manager.areNotificationsEnabled()) manager.notify(NOTIFICATION_ID, notification);
    }

    private void stopAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        inForeground = false;
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        stopSelf();
    }

    private Notification buildNotification(boolean playing) {
        NotificationCompat.Action playPause = playing
            ? new NotificationCompat.Action(R.drawable.ic_media_pause, "Pause", broadcast(MediaActionReceiver.ACTION_PAUSE, 1))
            : new NotificationCompat.Action(R.drawable.ic_media_play, "Play", broadcast(MediaActionReceiver.ACTION_PLAY, 2));
        NotificationCompat.Action stop =
            new NotificationCompat.Action(R.drawable.ic_media_stop, "Stop", broadcast(MediaActionReceiver.ACTION_STOP, 3));

        Intent open = new Intent(this, MainActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, open, pendingFlags());

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moodist)
            .setContentTitle(MediaState.title)
            .setContentText(MediaState.text)
            .setContentIntent(contentIntent)
            .setDeleteIntent(broadcast(MediaActionReceiver.ACTION_DISMISS, 4))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(playing)
            .addAction(playPause)
            .addAction(stop)
            .setStyle(
                new MediaStyle()
                    .setMediaSession(session.getSessionToken())
                    .setShowActionsInCompactView(0, 1)
            )
            .build();
    }

    private PendingIntent broadcast(String action, int requestCode) {
        Intent intent = new Intent(this, MediaActionReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(this, requestCode, intent, pendingFlags());
    }

    private static int pendingFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        return flags;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel =
            new NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Controls for playing ambient sounds");
        channel.setShowBadge(false);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}
