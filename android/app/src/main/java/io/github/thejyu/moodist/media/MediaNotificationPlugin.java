package io.github.thejyu.moodist.media;

import android.Manifest;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

/**
 * Bridge between the web app and the native media notification.
 *
 * JS → native: update({ playing, title, text }), stop()
 * native → JS: "action" event with { action: "play" | "pause" | "stop" }
 */
@CapacitorPlugin(
    name = "MediaNotification",
    permissions = { @Permission(alias = "notifications", strings = { Manifest.permission.POST_NOTIFICATIONS }) }
)
public class MediaNotificationPlugin extends Plugin {

    @Override
    public void load() {
        MediaState.listener = action -> {
            JSObject data = new JSObject();
            data.put("action", action);
            getActivity().runOnUiThread(() -> notifyListeners("action", data, true));
        };
    }

    @Override
    protected void handleOnDestroy() {
        MediaState.listener = null;
        MediaState.active = false;
        MediaPlaybackService.shutdown();
    }

    @PluginMethod
    public void update(PluginCall call) {
        boolean playing = Boolean.TRUE.equals(call.getBoolean("playing", false));

        MediaState.playing = playing;
        MediaState.title = call.getString("title", "Moodist");
        MediaState.text = call.getString("text", "");
        if (playing) MediaState.active = true;

        getActivity().runOnUiThread(() -> {
            MediaPlaybackService.sync(getContext());
            call.resolve();
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        MediaState.active = false;
        MediaState.playing = false;

        getActivity().runOnUiThread(() -> {
            MediaPlaybackService.shutdown();
            call.resolve();
        });
    }
}
