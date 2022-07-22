import {WebPlugin} from '@capacitor/core';

import type {JWPlayerCuePoint} from '.';
import type {JWPlayerPlugin, JWPlayerEvent} from './definitions';

declare let jwplayer: any;

export class JWPlayerWeb extends WebPlugin implements JWPlayerPlugin {
    private isInit = false;
    private playerInstance: any | undefined = undefined;

    async echo(options: { value: string }): Promise<{ value: string }> {
        return options;
    }

    async initialize(options: { webLicenseKey?: string, androidLicenseKey?: string, iosLicenseKey?: string, googleCastId?: string, debug?: boolean }): Promise<any> {
        if (options.webLicenseKey) {
            jwplayer.key = options.webLicenseKey;
            jwplayer.debug = options.debug;
            this.isInit = true;
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

    async create(options: { webConfiguration?: { container: string; properties?: any }}): Promise<any> {
        if (this.isInit && options.webConfiguration) {
            if (this.playerInstance === undefined) {
                this.playerInstance = jwplayer(options.webConfiguration!.container);
                this.playerInstance.setup({
                    ...options.webConfiguration!.properties ?? {},
                });
                this.loadEvents();
            } else {
                this.playerInstance.load({
                        ...options.webConfiguration!.properties ?? {},
                    },
                );
            }
            return true;
        }
    }

    private loadEvents() {
        this.playerInstance.on('ready', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'readyPlayerEvent',
                data: eventData
            };
            this.notifyListeners('readyPlayerEvent', event);
        });
        this.playerInstance.on('fullscreen', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'fullScreenPlayerEvent',
                data: eventData
            };
            this.notifyListeners('fullScreenPlayerEvent', event);
        });
        this.playerInstance.on('play', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'play',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('pause', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'pause',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('playAttemptFailed', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'playAttemptFailed',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('buffer', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'buffer',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('idle', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'idle',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('complete', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'complete',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('error', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'error',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('warning', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'warning',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('seek', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'seek',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('time', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'time',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('playlistItem', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'playlistItem',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
        this.playerInstance.on('playlistComplete', (eventData: any) => {
            const event: JWPlayerEvent = {
                name: 'playlistComplete',
                data: eventData
            };
            this.notifyListeners('playerEvent', event);
        });
    }

    getPosition(): Promise<{ value: number }> {
        if (this.playerInstance !== undefined) {
            return Promise.resolve({value: this.playerInstance.getPosition()});
        }
        return Promise.resolve({value: 0});
    }

    seek(options: { position: number }): Promise<any> {
        this.playerInstance.seek(options.position);
        return Promise.resolve(true);
    }

    addCuePoints(options: { cuePoints: JWPlayerCuePoint[] }) {
        this.playerInstance.addCues(options.cuePoints);
        return Promise.resolve(true);
    }

    addButton(img: string, tooltip: string, callback: () => void, id: string, btnClass: string): void {
        this.playerInstance.addButton(img, tooltip, callback, id, btnClass);
    }

    playlistItem(options: { index: number }): Promise<any> {
        this.playerInstance.playlistItem(options.index);
        return Promise.resolve(true);
    }



}
