export interface JWPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  initialize(options: { androidLicenseKey?: string, iosLicenseKey?: string , googleCastId?: string}): Promise<any>;
  create(options: { videoURL: string, posterURL?: string,  forceFullScreenOnLandscape?: boolean, x: number, y:number, width: number, height: number }): Promise<any>;
  remove(): Promise<any>;
}
