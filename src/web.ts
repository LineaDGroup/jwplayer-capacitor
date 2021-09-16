import { WebPlugin } from '@capacitor/core';

import type { JWPlayerPlugin } from './definitions';

export class JWPlayerWeb extends WebPlugin implements JWPlayerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initialize(options: {androidLicenseKey : string, iosLicenseKey: string}): Promise<any> {
    console.log('initializing player for web with options',options);
  }

  async create(options: {videoURL : string}): Promise<any> {
    console.log('creating a web player with options',options);
  }

  async remove(): Promise<any> {
    console.log('removing a web player');
  }
}
