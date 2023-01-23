import Foundation
import Capacitor


/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(JWPlayerPlugin)
public class JWPlayerPlugin: CAPPlugin {
    
    private var implementation: JWPlayer?
    
    override public func load() {

    }

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": true
        ])
    }


    @objc func initialize(_ call: CAPPluginCall) {
        if self.implementation == nil {
            self.implementation = JWPlayer(plugin: self)
        }
        let JWPLAYER_KEY = call.getString("iosLicenseKey", "")
        let GOOGLE_CAST_ID = call.getString("googleCastId", "")
        self.implementation?.load(JWPLAYER_KEY, googleCastId: GOOGLE_CAST_ID, completion: {
            call.resolve([
                "initialized": true
            ])
        })
    }


    @objc func remove(_ call: CAPPluginCall){
        self.implementation?.remove(completion: {
            self.implementation = nil
            call.resolve([
                "removed": true
            ])
        })
    }


    
    @objc func create(_ call: CAPPluginCall) {
        let nativeConfiguration : [String: Any]? = call.getObject("nativeConfiguration")
        let advertisingConfig : [String: Any]? = call.getObject("advertisingConfig")
        
        self.implementation?.create(nativeConfiguration: nativeConfiguration ?? [:], _advertisingConfig: advertisingConfig , completion: {
            call.resolve([
                "created": true
            ])
            self.notifyListeners("readyPlayerEvent", data: nil)
        })

        
    }
    
   
    
    
    @objc func addButton(_ call: CAPPluginCall) {
    }
    
    @objc func addCuePoints(_ call: CAPPluginCall) {
    }
    
    
    
    @objc func getPosition(_ call: CAPPluginCall) {
        self.implementation?.getPostion(completion: { time in
            call.resolve([
                "value": time
            ])
        })
    }
    
    @objc func notifyEvent(_ name: String, data: [String: Any]?) {
        self.notifyListeners(name, data: data)
    }
    
}



