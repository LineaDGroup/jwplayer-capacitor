import Foundation
import Capacitor
import JWPlayerKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(JWPlayerPlugin)
public class JWPlayerPlugin: CAPPlugin, JWPlayerViewDelegate {
    public func playerView(_ view: JWPlayerView, sizeChangedFrom oldSize: CGSize, to newSize: CGSize) {
    
    }
    
    private let implementation = JWPlayer()
    var JWPLAYER_KEY: String = "";
    var jwPlayerViewController: JWPlayerViewController!;

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func initialize(_ call: CAPPluginCall) {

        self.JWPLAYER_KEY = call.getString("iosLicenseKey", "")

        if self.JWPLAYER_KEY.isEmpty {
            call.reject("License key missing! You need to set up your JWPlayer license key")
            return
        }

        JWPlayerKitLicense.setLicenseKey(self.JWPLAYER_KEY)
        call.resolve([
            "initialized": true
        ])
    }

    @objc func create(_ call: CAPPluginCall) {

        DispatchQueue.main.async {
            do {
                self.bridge?.viewController?.view.frame
                self.jwPlayerViewController = JWPlayerViewController();
                let videoURL = call.getString("videoURL", "")

                if videoURL.isEmpty {
                    call.reject("You have to provide a fileURL to playback")
                    return
                }
                // Create a JWPlayerItem
                let item = try JWPlayerItemBuilder()
                    .file(URL(string: videoURL)!)
                    .build()

                // Create a config, and give it the item as a playlist.
                let config = try JWPlayerConfigurationBuilder()
                    .playlist([item])
                    .build()

                

                self.bridge?.viewController?.view.addSubview(self.jwPlayerViewController.view)
                self.jwPlayerViewController.playerView.delegate = self
                
                // Set the config
                self.jwPlayerViewController.player.configurePlayer(with: config)
                
                self.notifyListeners("onJWPlayerReady", data: nil)
            }
            catch let error as NSError {
                    print("Fail: \(error.localizedDescription)")
                }
            
        }
        call.resolve([
            "created": true
        ])
    }
}
