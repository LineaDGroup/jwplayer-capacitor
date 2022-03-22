export interface JWPlayerMediaTrack {
    file: string;
    label: string;
    default: boolean;
}

export interface JWPlayerPlugin {
    echo(options: { value: string }): Promise<{ value: string }>;

    initialize(options: { webLicenseKey?: string, androidLicenseKey?: string, iosLicenseKey?: string, googleCastId?: string }): Promise<any>;

    create(options: {
        webConfiguration?: {
            container: string,
            properties?: {}
        },
        nativeConfiguration?: { videoURL: string, posterURL?: string, forceFullScreenOnLandscape?: boolean, x: number, y: number, width: number, height: number, front?: boolean },
        captions?: Array<JWPlayerMediaTrack>
    }): Promise<any>;

    remove(): Promise<any>;
}

