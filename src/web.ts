import {WebPlugin} from '@capacitor/core';
import {JWPlayerMediaTrack} from '.';
import type {JWPlayerPlugin} from './definitions';

declare var jwplayer: any;

export class JWPlayerWeb extends WebPlugin implements JWPlayerPlugin {
    private isInit: boolean = false;
    private playerInstance: any | undefined = undefined;

    async echo(options: { value: string }): Promise<{ value: string }> {
        console.log('ECHO', options);
        return options;
    }

    async initialize(options: { webLicenseKey?: string, androidLicenseKey?: string, iosLicenseKey?: string, googleCastId?: string }): Promise<any> {
        if (options.webLicenseKey) {
            jwplayer.key = options.webLicenseKey;
            jwplayer.debug = true;
            this.isInit = true;
            console.log('Init', options);
        } else {
            console.error('Jwplayer does not have a key', options);
        }
    }

    async create(options: { divId?: string, videoURL: string, posterURL?: string, forceFullScreenOnLandscape?: boolean, x: number, y: number, width: number, height: number, captions?: Array<JWPlayerMediaTrack>, front?: boolean }): Promise<any> {
        if (this.isInit) {
            setTimeout(() => {
                if (this.playerInstance === undefined) {
                    this.playerInstance = jwplayer(options.divId);
                    this.playerInstance.setup({
                        "autostart": true,
                        "file": options.videoURL,
                        "image": options.posterURL,
                        "height": options.height,
                        "width": options.width,
                        "tracks": options.captions?.map(item => {
                            return {
                                'kind': "captions",
                                'file': item.url,
                                'label': item.label
                            }
                        })
                    })
                } else {
                    this.playerInstance.load({
                            "autostart": true,
                            "file": options.videoURL,
                            "image": options.posterURL,
                            "height": options.height,
                            "width": options.width,
                            "tracks": options.captions?.map(item => {
                                return {
                                    'kind': "captions",
                                    'file': item.url,
                                    'label': item.label
                                }
                            })
                        }
                    )
                }
            }, 1000);

        } else {
            console.error("Jwplayer has not initialized")
        }

    }

    async remove(): Promise<any> {
        if (this.playerInstance) {
            await this.playerInstance!.remove();
            this.playerInstance = undefined;
            this.isInit = false;
        }
        return true;
    }
}
