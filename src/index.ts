import { registerPlugin } from '@capacitor/core';
import './jwplayer-8.24.3/jwplayer.js'

import type { JWPlayerPlugin } from './definitions';

const JWPlayer = registerPlugin<JWPlayerPlugin>('JWPlayer', {
  web: () => import('./web').then(m => new m.JWPlayerWeb()),
});

export * from './definitions';
export { JWPlayer };
