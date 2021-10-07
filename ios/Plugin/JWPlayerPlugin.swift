import Foundation
import Capacitor
import JWPlayerKit
import GoogleCast

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(JWPlayerPlugin)
public class JWPlayerPlugin: CAPPlugin {
    
    
    private let implementation = JWPlayer()
    var JWPLAYER_KEY: String = "";
    var GOOGLE_CAST_ID : String?
    private var playerViewController : JWPlayerViewController?
    private var castController : JWCastController?
    private var isFront = false

    
    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func initialize(_ call: CAPPluginCall) {
        
        self.JWPLAYER_KEY = call.getString("iosLicenseKey", "")
        self.GOOGLE_CAST_ID = call.getString("googleCastId", "")
        if self.JWPLAYER_KEY.isEmpty {
            call.reject("License key missing! You need to set up your JWPlayer license key")
            return
        }
        JWPlayerKitLicense.setLicenseKey(self.JWPLAYER_KEY)
        if self.GOOGLE_CAST_ID != nil && !(self.GOOGLE_CAST_ID?.isEmpty ?? false){
            let discoveryCriteria = GCKDiscoveryCriteria(applicationID:  self.GOOGLE_CAST_ID!)
            let options = GCKCastOptions(discoveryCriteria: discoveryCriteria)
            GCKCastContext.setSharedInstanceWith(options)
        }
        call.resolve([
            "initialized": true
        ])
    }
    
    
    @objc func remove(_ call: CAPPluginCall){
        DispatchQueue.main.async {
            if self.playerViewController != nil {
                //self.webView?.superview?.willRemoveSubview(self.playerViewController!.view)
                self.playerViewController!.view.removeFromSuperview()
                self.playerViewController!.removeFromParent()
                self.playerViewController!.view = nil
                self.playerViewController = nil
                
                if !self.isFront {
                    self.webView?.isOpaque = true
                    self.webView?.backgroundColor = UIColor.white
                    self.webView?.scrollView.backgroundColor = UIColor.white
                }
                
            }
        }
        call.resolve([
            "removed": true
        ])
    }
    
    @objc func create(_ call: CAPPluginCall) {
        
        DispatchQueue.main.async {
            do {
                if self.playerViewController == nil {
                    self.playerViewController = JWPlayerViewController()
                    self.playerViewController?.loadViewIfNeeded()
                    
                    
                    let videoURL = call.getString("videoURL", "")
                    let posterURL = call.getString("posterURL", "")
                    let width = call.getDouble("width") ?? 100
                    let height = call.getDouble("height") ?? 100
                    let x = call.getDouble("x") ?? 0
                    let y = call.getDouble("y") ?? 0
                    let autostart = call.getBool("autostart") ?? false
                    let forceFullScreenOnLandscape = call.getBool("forceFullScreenOnLandscape") ?? false
                    self.isFront = call.getBool("front") ?? false
                    if videoURL.isEmpty {
                        call.reject("You have to provide a fileURL to playback")
                        return
                    }
                    
                    var playerItem = try JWPlayerItemBuilder()
                        .file(URL(string: videoURL)!)
                        .build()
                    
                    if let captions = call.getArray("captions", [String: Any].self){
                        var captionTracks = [JWMediaTrack]()
                        for caption in captions {
                            let urlString = caption["url"] as! String?
                            let url = URL(string: urlString!)!
                            let builder = JWCaptionTrackBuilder()
                            .file(url)
                                .label(caption["label"] as! String)
                                .defaultTrack((caption["default"] as! Bool?)!)
                            do {
                                let englishTrack = try builder.build()
                                captionTracks.append(englishTrack)
                            } catch {
                                // Handle error
                            }
                        }
                        
                        playerItem = try JWPlayerItemBuilder()
                            .file(URL(string: videoURL)!)
                            .mediaTracks(captionTracks)
                            .build()
                    }
                    
                    
                    
                    if !posterURL.isEmpty {
                        let posterImage = URL(string:posterURL)!
                        playerItem = try JWPlayerItemBuilder()
                            .file(URL(string: videoURL)!)
                            .posterImage(posterImage)
                            .build()
                    }
                    
                    // Second, create a player config with the created JWPlayerItem. Add the related config.
                    let config = try JWPlayerConfigurationBuilder()
                        .playlist([playerItem])
                        .autostart(autostart)
                        .build()
                    self.playerViewController!.player.configurePlayer(with: config)
                    self.playerViewController?.view.frame = CGRect(x: x, y: y, width: width, height: height)
                    if forceFullScreenOnLandscape {
                        self.playerViewController!.forceFullScreenOnLandscape = true
                        self.playerViewController!.forceLandscapeOnFullScreen = true
                    }
                    self.playerViewController?.playerView.videoGravity = .resize
                    self.playerViewController!.view?.autoresizingMask = [.flexibleHeight, .flexibleWidth]
                    //self.playerViewController!.view?.translatesAutoresizingMaskIntoConstraints = false
                    
                    /*
                    
                     */
                
                    if !self.isFront {
                        self.webView?.isOpaque = false
                        self.webView?.backgroundColor = UIColor.clear
                        self.webView?.scrollView.backgroundColor = UIColor.clear
                        self.webView?.superview?.addSubview(self.playerViewController!.view)
                        
                        //self.bridge?.viewController?.view.superview?.addSubview(self.playerViewController!.view)

                        self.bridge?.viewController?.addChild(self.playerViewController!)
                        //self.bridge?.viewController?.view.bringSubviewToFront(self.webView!)

                        self.webView?.superview?.bringSubviewToFront(self.webView!)
                       // self.bridge?.viewController?.view.superview?.bringSubviewToFront((self.bridge?.viewController?.view)!)
                    } else{
                        self.bridge?.viewController?.view.addSubview(self.playerViewController!.view)
                        self.bridge?.viewController?.addChild(self.playerViewController!)
                        self.playerViewController!.didMove(toParent: self.bridge?.viewController?.parent)
                    }
    
                    if self.GOOGLE_CAST_ID != nil && !(self.GOOGLE_CAST_ID?.isEmpty ?? false) {
                        self.setUpCastController()
                    }
                    self.notifyListeners("onJWPlayerReady", data: nil)
                }
            }catch let error as NSError {
                print("Fail: \(error.localizedDescription)")
            }
            
        }
        call.resolve([
            "created": true
        ])
    }
    
    func setUpCastController() {
        self.castController = JWCastController(player: self.playerViewController!.player)
    }
    

    
}
