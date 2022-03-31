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
}

