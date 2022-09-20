import Foundation
import JWPlayerKit
import GoogleCast
import UIKit

@objc public class JWPlayer: NSObject {
    
    static var supportedInterfaceOrientations = UIInterfaceOrientationMask.portrait
    
    private var JWPLAYER_KEY: String = "";
    private var GOOGLE_CAST_ID : String?
    private let plugin: JWPlayerPlugin
    private var playerViewController : PlayerViewController?
    private var castController : JWCastController?
    private var isFront = false
    private var progressView: UIView = UIView()
    
    init(plugin: JWPlayerPlugin) {
        self.plugin = plugin
        super.init()
    }
    
    @objc public static func getSupportedInterfaceOrientations() -> UIInterfaceOrientationMask {
        return JWPlayer.supportedInterfaceOrientations
    }
    
    @objc public func load(_ key: String, googleCastId: String, completion: @escaping () -> Void) {
        self.JWPLAYER_KEY = key
        self.GOOGLE_CAST_ID = googleCastId
        if self.JWPLAYER_KEY.isEmpty {
            print("License key missing! You need to set up your JWPlayer license key")
            return
        }
        JWPlayerKitLicense.setLicenseKey(self.JWPLAYER_KEY)
        if self.GOOGLE_CAST_ID != nil && !(self.GOOGLE_CAST_ID?.isEmpty ?? false){
            let discoveryCriteria = GCKDiscoveryCriteria(applicationID:  self.GOOGLE_CAST_ID!)
            let options = GCKCastOptions(discoveryCriteria: discoveryCriteria)
            GCKCastContext.setSharedInstanceWith(options)
        }
        self._showProgress()
        completion()
    }
    
    @objc public func getPostion(completion: @escaping (Double) -> Void) {
        var time = 0.0
        if ((self.playerViewController?.player) != nil) {
            time = self.playerViewController?.player.time.position.binade ?? 0.0
        }
        completion(time)
    }
    
    @objc public func create(nativeConfiguration : [String: Any], _advertisingConfig : [String:Any]?, completion: @escaping () -> Void ){
        DispatchQueue.main.async {
            do {
                if self.playerViewController == nil {
                    var playList : [JWPlayerItem] = []
                    self.playerViewController = PlayerViewController()
                    self.playerViewController?.capacitor = self.plugin
                    self.playerViewController?.loadViewIfNeeded()
                    if let list = nativeConfiguration["playlist"] as? [[String:Any]]{
                        playList = try self._generatePlayList(list)
                    }
                    let params = JWPlayerConfigurationBuilder()
                        .playlist(playList)
                        .autostart(true)
                    if let advertisingConfig = _advertisingConfig {
                        let addConfig = try self._generateAdConfig(advertisingConfig)
                        params.advertising(addConfig)
                    }
                    let config = try params.build()
                    self.playerViewController!.player.configurePlayer(with: config)
                    self.playerViewController?.view.frame = CGRect(
                        x: 0,
                        y: 0,
                        width: (self.plugin.bridge?.viewController?.view.frame.width)!,
                        height: (self.plugin.bridge?.viewController?.view.frame.height)!
                    )
                    self.playerViewController!.forceFullScreenOnLandscape = true
                    self.playerViewController!.forceLandscapeOnFullScreen = true
                    self.playerViewController!.modalPresentationStyle = .fullScreen
                    
                    self.playerViewController?.playerView.videoGravity = .resizeAspect
                    self.playerViewController!.view?.autoresizingMask = [.flexibleHeight, .flexibleWidth]
                    self.plugin.bridge?.webView!.addSubview(self.playerViewController!.view)
                    self.plugin.bridge?.viewController?.addChild(self.playerViewController!)
                    self.progressView.removeFromSuperview()
                    self.progressView.superview?.removeFromSuperview()
                    self.playerViewController?.transitionToFullScreen(animated: true)
                }
            }
            catch JWplayerCapacitorPluginError.errorBuildingAdConfig {
                print("Error building advertising")
            }
            catch let error as NSError {
                print("Fail: \(error.localizedDescription)")
            }
            completion()
        }
        
    }
    
    @objc func remove(completion: @escaping () -> Void){
        print("Removing player")
        DispatchQueue.main.async {
            if self.playerViewController != nil {
                self.playerViewController?.player.stop()
                self.plugin.bridge?.viewController?.view.sendSubviewToBack(self.playerViewController!.view)
                self.plugin.webView?.superview?.willRemoveSubview(self.playerViewController!.view)
                self.playerViewController!.view.removeFromSuperview()
                self.playerViewController!.removeFromParent()
                self.playerViewController!.view = nil
                self.playerViewController = nil
            }
            completion()
        }
    }
    
    
    @objc private func _generatePlayList(_ list: [[String:Any]]) throws -> [JWPlayerItem] {
        var playList : [JWPlayerItem] = []
        var captionTracks : [JWMediaTrack] = []
        for video in list {
            if let tracks = video["tracks"] as? [[String:Any]] {
                captionTracks = try self._generateCaptions(tracks)
            }
            
            if let file = video["file"] as? String {
                let playerItem = try JWPlayerItemBuilder()
                    .file(URL(string: file)!)
                    .mediaTracks(captionTracks)
                    .title(video["title"] as! String)
                    .description(video["description"] as! String)
                    .startTime((video["starttime"] != nil) ? video["starttime"] as! Double : 0)
                    .build()
                playList.append(playerItem)
                
            }
        }
        return playList
    }
    
    
    @objc private func _generateCaptions(_ tracks: [[String:Any]]) throws -> [JWMediaTrack] {
        var captionTracks : [JWMediaTrack] = []
        for caption in tracks {
            if let kind = caption["kind"] as? String  {
                if kind == "captions" {
                    let urlString = caption["file"] as! String?
                    let url = URL(string: urlString!)!
                    let builder = JWCaptionTrackBuilder()
                        .file(url)
                        .label(caption["label"] as! String)
                    let englishTrack = try builder.build()
                    captionTracks.append(englishTrack)
                }
            }
        }
        return captionTracks
    }
    
    
    @objc private func _generateAdConfig(_ advertismentConfig: [String:Any]) throws -> JWPlayerKit.JWAdvertisingConfig{
        var adList : [JWAdBreak] = []
        if let advertisingConfigArray = advertismentConfig["schedule"] as? [[String:Any]]{
            for ad in advertisingConfigArray {
                let url =  URL(string: ad["url"] as? String ?? "")
                if url != nil && ad["url"] != nil && ad["begin"] != nil {
                    let adBreakBuilder = JWAdBreakBuilder()
                        .offset(.midroll(seconds: ad["begin"] as! Double))
                        .tags([url!])
                    guard let adBreak = try? adBreakBuilder.build() else {
                        // Handle build error
                        print("Error parsing ad")
                        throw JWplayerCapacitorPluginError.errorBuildingAdConfig
                    }
                    adList.append(adBreak)
                }
            }
        }
        let adConfigBuilder = JWImaAdvertisingConfigBuilder()
            .schedule(adList)
        guard let adConfig = try? adConfigBuilder.build() else {
            print("Error building config")
            throw JWplayerCapacitorPluginError.errorBuildingAdConfig
        }
        return adConfig
        
    }
    
    
    @objc private func _showProgress() {
        DispatchQueue.main.async { [weak self] in
            guard let strongSelf = self else {
                return
            }
            var spinner = UIActivityIndicatorView(style: .whiteLarge)
            spinner.translatesAutoresizingMaskIntoConstraints = false
            spinner.startAnimating()
            strongSelf.progressView.backgroundColor = UIColor(white: 0, alpha: 1)
            strongSelf.progressView.addSubview(spinner)
            strongSelf.progressView.center = (strongSelf.plugin.webView?.superview!.center)!
            strongSelf.progressView.frame = CGRect(
                x: 0,
                y: 0,
                width: (strongSelf.plugin.bridge?.viewController?.view.frame.width)!,
                height: (strongSelf.plugin.bridge?.viewController?.view.frame.height)!
            )
            
            spinner.centerXAnchor.constraint(equalTo: strongSelf.progressView.centerXAnchor).isActive = true
            spinner.centerYAnchor.constraint(equalTo: strongSelf.progressView.centerYAnchor).isActive = true            
            strongSelf.plugin.bridge?.webView?.superview!.addSubview(strongSelf.progressView)
            strongSelf.plugin.bridge?.webView?.superview!.superview?.bringSubviewToFront(strongSelf.progressView)
            JWPlayer.supportedInterfaceOrientations = .landscape
        }
    }
}
enum JWplayerCapacitorPluginError: Error {
    case errorBuildingAdConfig
    case unexpected(code: Int)
}
