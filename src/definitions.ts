export interface JWPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  initialize(options: { androidLicenseKey: string, iosLicenseKey: string }): Promise<any>;
  create(options: { videoURL: string, x: number, y:number, width: number, height: number }): Promise<any>;
  remove(): Promise<any>;
}
