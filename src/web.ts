import { WebPlugin } from '@capacitor/core';

import type { JWPlayerPlugin } from './definitions';

export class JWPlayerWeb extends WebPlugin implements JWPlayerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
