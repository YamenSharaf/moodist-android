import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.github.thejyu.moodist',
  appName: 'Moodist',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  }
};

export default config;
