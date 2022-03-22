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


    async remove(): Promise<any> {
        if (this.playerInstance) {
            await this.playerInstance!.remove();
            this.playerInstance = undefined;
            this.isInit = false;
        }
        return true;
    }

    async create(options: { webConfiguration?: { container: string; properties?: {} }; nativeConfiguration?: { videoURL: string; posterURL?: string; forceFullScreenOnLandscape?: boolean; x: number; y: number; width: number; height: number; front?: boolean }; captions?: Array<JWPlayerMediaTrack> }): Promise<any> {
        if (this.isInit && options.webConfiguration) {
            if (this.playerInstance === undefined) {
                this.playerInstance = jwplayer( options.webConfiguration!.container);
                this.playerInstance.setup({
                    ... options.webConfiguration!.properties ?? {},
                    "tracks":  options.captions?.map(item => {
                        return {
                            'kind': "captions",
                            'file': item.file,
                            'label': item.label
                        }
                    })
                })
            } else {
                this.playerInstance.load({
                        ... options.webConfiguration!.properties ?? {},
                        "tracks":  options.captions?.map(item => {
                            return {
                                'kind': "captions",
                                'file': item.file,
                                'label': item.label
                            }
                        })
                    }
                )
            }
            return true;
        } else {
            console.error("Jwplayer has not initialized")
            return false;
        }
    }
}
