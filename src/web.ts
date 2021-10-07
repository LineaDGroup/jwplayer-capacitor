import { WebPlugin } from '@capacitor/core';
import { JWPlayerMediaTrack } from '.';

import type { JWPlayerPlugin } from './definitions';

export class JWPlayerWeb extends WebPlugin implements JWPlayerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  async initialize(options: { androidLicenseKey?: string, iosLicenseKey?: string , googleCastId?: string}): Promise<any> {
    console.log('initializing player for web with options',options);
  }

  async create(options: { videoURL: string, posterURL?: string,  forceFullScreenOnLandscape?: boolean, x: number, y:number, width: number, height: number, captions?: Array<JWPlayerMediaTrack> , front?: boolean}): Promise<any> {
    console.log('creating a web player with options',options);
  }

  async remove(): Promise<any> {
    console.log('removing a web player');
  }
}
