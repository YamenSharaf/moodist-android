import { useEffect, useMemo, useRef } from 'react';

import { MediaNotification } from '@/lib/media-notification';
import { useSoundStore } from '@/stores/sound';
import { sounds as soundData } from '@/data/sounds';

const labels: Record<string, string> = Object.fromEntries(
  soundData.categories.flatMap(category =>
    category.sounds.map(sound => [sound.id, sound.label]),
  ),
);

function describe(ids: Array<string>) {
  const names = ids.map(id => labels[id]).filter(Boolean);

  if (names.length <= 3) return names.join(', ');

  return `${names.slice(0, 3).join(', ')} +${names.length - 3}`;
}

/**
 * Android (Capacitor) media controls: a foreground-service notification with
 * play / pause / stop that mirrors the sound store.
 */
export function NativeMediaControls() {
  const isPlaying = useSoundStore(state => state.isPlaying);
  const play = useSoundStore(state => state.play);
  const pause = useSoundStore(state => state.pause);
  const soundsState = useSoundStore(state => state.sounds);
  const askedPermission = useRef(false);

  const selectedText = useMemo(
    () =>
      describe(
        Object.keys(soundsState).filter(id => soundsState[id].isSelected),
      ),
    [soundsState],
  );

  // Notification buttons, lock screen and headset buttons → sound store
  useEffect(() => {
    const handle = MediaNotification.addListener('action', ({ action }) => {
      if (action === 'play') {
        play();
      } else if (action === 'pause') {
        pause();
      } else if (action === 'stop') {
        pause();
        MediaNotification.stop();
      }
    });

    return () => {
      handle.then(h => h.remove());
    };
  }, [play, pause]);

  // Sound store → notification
  useEffect(() => {
    const sync = async () => {
      if (isPlaying && !askedPermission.current) {
        askedPermission.current = true;

        try {
          const { notifications } = await MediaNotification.checkPermissions();

          if (notifications === 'prompt') {
            await MediaNotification.requestPermissions();
          }
        } catch {
          // Older Android versions don't need a runtime permission
        }
      }

      await MediaNotification.update({
        playing: isPlaying,
        text: selectedText,
        title: 'Moodist',
      });
    };

    sync().catch(() => {});
  }, [isPlaying, selectedText]);

  return null;
}
