import Foundation
import Capacitor
import JWPlayerKit
import GoogleCast
import UIKit

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(JWPlayerPlugin)
public class JWPlayerPlugin: CAPPlugin {


    private let implementation = JWPlayer()
    var JWPLAYER_KEY: String = "";
    var GOOGLE_CAST_ID : String?
    private var playerViewController : PluginViewController?
    private var castController : JWCastController?
    private var isFront = false
    private var progressView: UIView = UIView()


    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }


    func showProgress(){
        DispatchQueue.main.async {
            var spinner = UIActivityIndicatorView(style: .whiteLarge)
            self.progressView.backgroundColor = UIColor(white: 0, alpha: 1)
            spinner.translatesAutoresizingMaskIntoConstraints = false
            spinner.startAnimating()
            self.progressView.addSubview(spinner)
            self.progressView.center = (self.webView?.superview!.center)!
            self.progressView.frame = CGRect(
                x: 0,
                y: 0,
                width: (self.bridge?.viewController?.view.frame.width)!,
                height: (self.bridge?.viewController?.view.frame.height)!
            )

            spinner.centerXAnchor.constraint(equalTo: self.progressView.centerXAnchor).isActive = true
            spinner.centerYAnchor.constraint(equalTo: self.progressView.centerYAnchor).isActive = true

            self.bridge?.webView?.superview!.addSubview(self.progressView)
            self.bridge?.webView?.superview!.superview?.bringSubviewToFront(self.progressView)

            //let value = UIInterfaceOrientation.landscapeLeft.rawValue
            //UIDevice.current.setValue(value, forKey: "orientation")

        }

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
        showProgress()
        call.resolve([
            "initialized": true
        ])
    }


    @objc func remove(_ call: CAPPluginCall){
        DispatchQueue.main.async {
            if self.playerViewController != nil {
                self.playerViewController?.player.stop()
                self.bridge?.viewController?.view.sendSubviewToBack(self.playerViewController!.view)
                self.webView?.superview?.willRemoveSubview(self.playerViewController!.view)
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
                    self.playerViewController = PluginViewController()
                    self.playerViewController?.capacitor = self
                    self.playerViewController?.loadViewIfNeeded()

                    let nativeConfiguration : [String: Any]? = call.getObject("nativeConfiguration")
                    let playlist  =  nativeConfiguration?["playlist"] as! [[String:Any]]


                    let posterURL = call.getString("posterURL", "")
                    let width = nativeConfiguration?["width"] != nil ? nativeConfiguration?["width"] as! Double :  200
                    let height = nativeConfiguration?["height"] != nil ? nativeConfiguration?["height"] as! Double :  200
                    let x = nativeConfiguration?["x"] != nil ? nativeConfiguration?["x"] as! Double :  0
                    let y = nativeConfiguration?["y"] != nil ? nativeConfiguration?["y"] as! Double :  0
                    let autostart = nativeConfiguration?["autostart"] ?? false
                    let forceFullScreenOnLandscape = nativeConfiguration?["forceFullScreenOnLandscape"] != nil ? nativeConfiguration?["forceFullScreenOnLandscape"] as! Bool : true
                    let forceFullScreen = nativeConfiguration?["forceFullScreen"] != nil ? nativeConfiguration?["forceFullScreen"] as! Bool : true
                    self.isFront = nativeConfiguration?["front"] != nil ? nativeConfiguration?["front"] as! Bool : true


                    print("PLUGIN .............")
                    var playList : [JWPlayerItem] = []

                    for video in playlist {
                        if video["file"] == nil {
                            call.reject("You have to provide a fileURL to playback")
                            return
                        }
                        var captionTracks = [JWMediaTrack]()
                        if let captions = video["captions"] as? [[String:Any]]{
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
                        }

                        let playerItem = try JWPlayerItemBuilder()
                            .file(URL(string: video["file"] as! String)!)
                            .mediaTracks(captionTracks)
                            .title(video["title"] as! String)
                            .description(video["description"] as! String)
                            .startTime((video["starttime"] != nil) ? video["starttime"] as! Double : 0)
                            .build()
                        playList.append(playerItem)
                    }



                    /*
                     if !posterURL.isEmpty {
                     let posterImage = URL(string:posterURL)!
                     playerItem = try JWPlayerItemBuilder()
                     .file(URL(string: videoURL)!)
                     .posterImage(posterImage)
                     .build()
                     }*/

                    // Second, create a player config with the created JWPlayerItem. Add the related config.
                    let config = try JWPlayerConfigurationBuilder()
                        .playlist(playList)
                        .autostart(autostart as! Bool)
                        .build()
                    self.playerViewController!.player.configurePlayer(with: config)
                    // self.playerViewController!.delegate?.playerViewControllerDidGoFullScreen()
                    self.playerViewController?.view.frame = CGRect(
                        x: 0,
                        y: 0,
                        width: (self.bridge?.viewController?.view.frame.width)!,
                        height: (self.bridge?.viewController?.view.frame.height)!
                    )
                    if forceFullScreenOnLandscape {
                        self.playerViewController!.forceFullScreenOnLandscape = true
                        self.playerViewController!.forceLandscapeOnFullScreen = true
                    }
                    if forceFullScreen {
                        self.playerViewController!.modalPresentationStyle = .fullScreen
                    }
                    self.playerViewController?.playerView.videoGravity = .resizeAspect
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
                        self.bridge?.webView!.addSubview(self.playerViewController!.view)
                        self.bridge?.viewController?.addChild(self.playerViewController!)
                        //self.playerViewController!.didMove(toParent: self.bridge?.viewController?.parent)
                    }

                    if self.GOOGLE_CAST_ID != nil && !(self.GOOGLE_CAST_ID?.isEmpty ?? false) {
                        self.setUpCastController()
                    }
                    self.progressView.removeFromSuperview()
                    self.progressView.superview?.removeFromSuperview()
                    self.notifyListeners("onJWPlayerReady", data: nil)
                    self.playerViewController?.transitionToFullScreen(animated: true)
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
        // self.castController = JWCastController(player: self.playerViewController!.player)
    }


    @objc func addButton(_ call: CAPPluginCall) {
    }

    @objc func addCuePoints(_ call: CAPPluginCall) {
    }


    @objc func getPosition(_ call: CAPPluginCall) {
        var time = 0.0
        if ((self.playerViewController?.player) != nil) {
            time = self.playerViewController?.player.time.position.binade ?? 0.0
        }
        call.resolve([
            "value": time
        ])
    }

}

class PluginViewController: JWPlayerViewController, JWPlayerViewControllerDelegate{

    var fullScreenVC: JWFullScreenViewController?
    var capacitor : CAPPlugin?

    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        self.forceFullScreenOnLandscape = true
        self.forceLandscapeOnFullScreen = true

    }
    func playerViewControllerWillGoFullScreen(_ controller: JWPlayerViewController) -> JWFullScreenViewController? {
        self.fullScreenVC = JWFullScreenViewController()
        return self.fullScreenVC
    }

    func playerViewControllerDidGoFullScreen(_ controller: JWPlayerViewController) {
        let dictionary: [String : Any] = [
            "name" : "fullScreenPlayerEvent",
            "data" : controller.isFullScreen,
        ]
        capacitor?.notifyListeners("fullScreenPlayerEvent", data: dictionary as! [String : Any] )
    }

    func playerViewControllerWillDismissFullScreen(_ controller: JWPlayerViewController) {

    }

    func playerViewControllerDidDismissFullScreen(_ controller: JWPlayerViewController) {

    }

    func playerViewController(_ controller: JWPlayerViewController, controlBarVisibilityChanged isVisible: Bool, frame: CGRect) {

    }

    func playerViewController(_ controller: JWPlayerViewController, sizeChangedFrom oldSize: CGSize, to newSize: CGSize) {

    }

    func playerViewController(_ controller: JWPlayerViewController, screenTappedAt position: CGPoint) {

    }

    func playerViewController(_ controller: JWPlayerViewController, relatedMenuOpenedWithItems items: [JWPlayerItem], withMethod method: JWRelatedInteraction) {

    }

    func playerViewController(_ controller: JWPlayerViewController, relatedMenuClosedWithMethod method: JWRelatedInteraction) {

    }

    func playerViewController(_ controller: JWPlayerViewController, relatedItemBeganPlaying item: JWPlayerItem, atIndex index: Int, withMethod method: JWRelatedInteraction) {

    }


    //pragma MARK: - Related advertising methods
    // Reports when an event is emitted by the player.
    override func jwplayer(_ player: AnyObject, adEvent event: JWAdEvent) {
        super.jwplayer(player, adEvent: event)

        switch event.type {
        case .adBreakStart:
            print("Ad break has begun")
        case .schedule:
            print("The ad(s) has been scheduled")
        case .request:
            print("The ad has been requested")
        case .started:
            print("The ad playback has started")
        case .impression:
            print("The ad impression has been fulfilled")
        case .meta:
            print("The ad metadata is ready")
        case .clicked:
            print("The ad has been tapped")
        case .pause:
            print("The ad playback has been paused")
        case .play:
            print("The ad playback has been resumed")
        case .skipped:
            print("The ad has been skipped")
        case .complete:
            print("The ad playback has finished")
        case .adBreakEnd:
            print("The ad break has finished")
        default:
            break
        }
    }

    // This method is triggered when a time event fires for a currently playing ad.
    override func onAdTimeEvent(_ time: JWTimeData) {
        super.onAdTimeEvent(time)


        // If you are not interested in the ad time data, avoid overriding this method due to performance reasons.
    }

    override func onMediaTimeEvent(_ time: JWTimeData) {
        super.onMediaTimeEvent(time)
        let dictionary: [String : Any] = [
            "name" : "time",
            "data" : [
                "position": time.position.binade
            ]
        ]
        capacitor?.notifyListeners("playerEvent", data: dictionary as! [String : Any] )
    }




}

