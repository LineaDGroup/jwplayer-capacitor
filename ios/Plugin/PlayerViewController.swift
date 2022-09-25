//
//  PlayerViewController.swift
//  Plugin
//
//  Created by Andres Diaz on 19/09/22.
//  Copyright © 2022 Max Lynch. All rights reserved.
//

import Foundation
import JWPlayerKit
import GoogleCast
import UIKit


class PlayerViewController: JWPlayerViewController, JWPlayerViewControllerDelegate{

    var fullScreenVC: JWFullScreenViewController?
    var capacitor : JWPlayerPlugin?

    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        self.forceFullScreenOnLandscape = true
        self.forceLandscapeOnFullScreen = true
        self.player.volume = 1
        do {
            let skinStylingBuilder = JWPlayerSkinBuilder()

            let builder = JWCaptionStyleBuilder()
                .fontColor(.white)
                .highlightColor(.black)
                .backgroundColor(UIColor(white: 0.0, alpha: 0.5))
                .edgeStyle(.raised)
            let style = try builder.build()
            self.playerView.captionStyle = style
            self.offlineMessage = ""
        }catch let error as NSError {
            print("Fail: \(error.localizedDescription)")
        }

    }
    func playerViewControllerWillGoFullScreen(_ controller: JWPlayerViewController) -> JWFullScreenViewController? {
        self.fullScreenVC = JWFullScreenViewController()
        return self.fullScreenVC
    }

    func playerViewControllerDidGoFullScreen(_ controller: JWPlayerViewController) {
       
    }

    func playerViewControllerWillDismissFullScreen(_ controller: JWPlayerViewController) {
        print("Eliminando full screen")
        
        let dictionary: [String : Any] = [
            "name" : "fullScreenPlayerEvent",
            "data" : false,
        ]
        capacitor?.notifyEvent("fullScreenPlayerEvent", data: dictionary as! [String : Any] )
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
        capacitor?.notifyEvent("playerEvent", data: dictionary as! [String : Any] )
    }




}
