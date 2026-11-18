package io.github.thejyu.moodist;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

import io.github.thejyu.moodist.media.MediaNotificationPlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MediaNotificationPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
