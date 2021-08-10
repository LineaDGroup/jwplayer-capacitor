import { registerPlugin } from '@capacitor/core';

import type { JWPlayerPlugin } from './definitions';

const JWPlayer = registerPlugin<JWPlayerPlugin>('JWPlayer', {
  web: () => import('./web').then(m => new m.JWPlayerWeb()),
});

export * from './definitions';
export { JWPlayer };
