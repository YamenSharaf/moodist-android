import { useEffect, useState } from 'react';
import { Capacitor } from '@capacitor/core';

import { MediaSessionTrack } from './media-session-track';
import { NativeMediaControls } from './native-media-controls';
import { useSSR } from '@/hooks/use-ssr';

export function MediaControls() {
  const [mode, setMode] = useState<'none' | 'native' | 'web'>('none');
  const { isBrowser } = useSSR();

  useEffect(() => {
    if (!isBrowser) return;

    if (Capacitor.isNativePlatform()) {
      setMode('native');
    } else if ('mediaSession' in navigator) {
      setMode('web');
    }
  }, [isBrowser]);

  if (mode === 'native') return <NativeMediaControls />;
  if (mode === 'web') return <MediaSessionTrack />;

  return null;
}
