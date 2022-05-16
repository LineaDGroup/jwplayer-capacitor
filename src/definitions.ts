import type { PluginListenerHandle } from '@capacitor/core';

export interface JWPlayerMediaTrack{
  file:string;
  label: string;
  default: boolean;
}
export interface JWPlayerMediaCaption {
  file: string;
  label: string;
}
export interface JWPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;

  initialize(options: { webLicenseKey?: string, androidLicenseKey?: string, iosLicenseKey?: string, googleCastId?: string, debug?: boolean }): Promise<any>;

  create(options: {
    webConfiguration?: {
      container: string,
      properties?: any,
    }
    nativeConfiguration?: any,
    captions?: JWPlayerMediaCaption[],
    tracks?: JWPlayerMediaTrack[]
  }): Promise<any>;

  remove(): Promise<any>;
  getPosition(): Promise<{ value: number }>;
  seek(options: { position: number}): Promise<any>;

  /**
   * Listen for events in player
   *
   * @since 1.0.0
   */
  addListener(
      eventName: 'playerEvent',
      listenerFunc: EventChangeListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  addListener(
      eventName: 'fullScreenPlayerEvent',
      listenerFunc: EventChangeListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  addListener(
      eventName: 'readyPlayerEvent',
      listenerFunc: EventChangeListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * Remove all listeners (including the network status changes) for this plugin.
   *
   * @since 1.0.0
   */
  removeAllListeners(): Promise<void>;
}

export interface JWPlayerEvent {
  /**
   * Whether there is an active connection or not.
   *
   * @since 1.0.0
   */
  name: string;

  /**
   * The type of network connection currently in use.
   *
   * If there is no active network connection, `connectionType` will be `'none'`.
   *
   * @since 1.0.0
   */
  data: any;
}
export type EventChangeListener = (event: JWPlayerEvent) => void;

