import { registerPlugin } from '@capacitor/core';
import type { PermissionState, PluginListenerHandle } from '@capacitor/core';

export type MediaNotificationAction = 'play' | 'pause' | 'stop';

export interface MediaNotificationPlugin {
  addListener(
    eventName: 'action',
    listener: (event: { action: MediaNotificationAction }) => void,
  ): Promise<PluginListenerHandle>;
  checkPermissions(): Promise<{ notifications: PermissionState }>;
  requestPermissions(): Promise<{ notifications: PermissionState }>;
  /** Removes the notification and stops the foreground service. */
  stop(): Promise<void>;
  /** Shows or updates the notification; starts the foreground service while playing. */
  update(options: {
    playing: boolean;
    text?: string;
    title?: string;
  }): Promise<void>;
}

/** Native-only plugin, implemented in android/.../media/MediaNotificationPlugin.java */
export const MediaNotification =
  registerPlugin<MediaNotificationPlugin>('MediaNotification');
