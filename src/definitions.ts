export interface JWPlayerMediaTrack{
  url:string;
  label: string;
  default: boolean;
}
export interface JWPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  initialize(options: { webLicenseKey?: string, androidLicenseKey?: string, iosLicenseKey?: string , googleCastId?: string}): Promise<any>;
  create(options: { divId?: string, videoURL: string, posterURL?: string,  forceFullScreenOnLandscape?: boolean, x: number, y:number, width: number, height: number , captions?: Array<JWPlayerMediaTrack>, front? : boolean}): Promise<any>;
  remove(): Promise<any>;
}

