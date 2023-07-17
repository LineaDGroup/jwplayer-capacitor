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
            self.offlineMessage = "..."
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
        //capacitor?.notifyEvent("playerEvent", data: dictionary as! [String : Any] )
    }

    override func jwplayer(_ player: JWPlayerKit.JWPlayer, didLoadPlaylistItem item: JWPlayerItem, at index: UInt) {
        super.jwplayer(player as! JWPlayerKit.JWPlayer, didLoadPlaylistItem: item, at: index)
        let dictionary: [String : Any] = [
            "name" : "playlistItem",
            "data" : [
                "index": index
            ]
        ]
        capacitor?.notifyEvent("playerEvent", data: dictionary )
    }

    override func jwplayerContentDidComplete(_ player: JWPlayerKit.JWPlayer) {
        super.jwplayerContentDidComplete(player as! JWPlayerKit.JWPlayer)
        let dictionary: [String : Any] = [
            "name" : "complete",
            "data" : [
                "complete": true
            ]
        ]
        capacitor?.notifyEvent("playerEvent", data: dictionary )
    }

    override func jwplayerPlaylistHasCompleted(_ player: JWPlayerKit.JWPlayer) {
        super.jwplayerPlaylistHasCompleted(player as! JWPlayerKit.JWPlayer)
        let dictionary: [String : Any] = [
            "name" : "playlistComplete",
            "data" : [
                "complete": true
            ]
        ]
        capacitor?.notifyEvent("playerEvent", data: dictionary )
    }

    override func jwplayer(_ player: JWPlayerKit.JWPlayer, failedWithError code: UInt, message: String) {
        super.jwplayer(player, failedWithError: code, message: message)
        let dictionary: [String : Any] = [
            "name" : "error",
            "data" : [
                "error": message.description
            ]
        ]
        capacitor?.notifyEvent("playerEvent", data: dictionary )
    }


    // MARK: - JWCastDelegate
       // Optionally, override the following methods to receive and respond to events when casting.
       // Always call the superclass's method when overriding these methods.
       
       
       // Called when a new casting device comes online.
       override func castController(_ controller: JWCastController, devicesAvailable devices: [JWCastingDevice]) {
           super.castController(controller, devicesAvailable: devices)
           print("[JWCastDelegate]: \(devices.count) became available: \(devices)")
       }

       // Called when a successful connection to a casting device is made.
       override func castController(_ controller: JWCastController, connectedTo device: JWCastingDevice) {
           super.castController(controller, connectedTo: device)
           print("[JWCastDelegate]: Connected to device: \(device.identifier)")
       }

       
       // Called when the casting device disconnects.
       override func castController(_ controller: JWCastController, disconnectedWithError error: Error?) {
           super.castController(controller, disconnectedWithError: error)
           
           if let error = error {
               print("[JWCastDelegate]: Casting disconnected from device with error: \"\(error.localizedDescription)\"")
           }
           else {
               print("[JWCastDelegate]: Casting disconnected from device successfully.")
           }
       }

       
       // Called when the connected casting device is temporarily disconnected. Video resumes on the mobile device until connection resumes.
       override func castController(_ controller: JWCastController, connectionSuspendedWithDevice device: JWCastingDevice) {
           super.castController(controller, connectionSuspendedWithDevice: device)
           print("[JWCastDelegate]: Connection suspended with device: \(device.identifier)")
       }

       
       // Called after connection is reestablished following a temporary disconnection. Video resumes on the casting device.
       override func castController(_ controller: JWCastController, connectionRecoveredWithDevice device: JWCastingDevice) {
           super.castController(controller, connectionRecoveredWithDevice: device)
           print("[JWCastDelegate]: Connection recovered with device: \(device.identifier)")
       }

       // Called when an attempt to connect to a casting device is unsuccessful.
       override func castController(_ controller: JWCastController, connectionFailedWithError error: Error) {
           super.castController(controller, connectionFailedWithError: error)
           print("[JWCastDelegate]: Connection failed with error: \(error.localizedDescription)")
       }

       // Called when casting session begins.
       override func castController(_ controller: JWCastController, castingBeganWithDevice device: JWCastingDevice) {
           super.castController(controller, castingBeganWithDevice: device)
           print("[JWCastDelegate]: Casting began with device: \(device.identifier)")
       }

       // Called when an attempt to cast to a casting device is unsuccessful.
       override func castController(_ controller: JWCastController, castingFailedWithError error: Error) {
           super.castController(controller, castingFailedWithError: error)
           print("[JWCastDelegate]: Casting failed with error: \(error.localizedDescription)")
       }

       // Called when a casting session ends.
       override func castController(_ controller: JWCastController, castingEndedWithError error: Error?) {
           super.castController(controller, castingEndedWithError: error)
           
           if let error = error {
               print("[JWCastDelegate]: Casting ended with error: \"\(error.localizedDescription)\"")
           }
           else {
               print("[JWCastDelegate]: Casting ended successfully.")
           }
       }

}
