//
//  MainVMAudience.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation
import SyncManager
import AgoraRtcKit

class MainVMAudience: NSObject, MainVMProtocol {
    weak var delegate: MainVMDelegate?
    var mode: Mode
    let config: Config
    var syncManager: SyncManager!
    var pushUrlString: String!
    var pullUrlString: String!
    
    var sceneRef: SceneReference!
    var roomInfo: RoomInfo!
    var agoraKit: AgoraRtcEngineKit!
    var mediaPlayer: AgoraRtcMediaPlayerProtocol!
    let queue = DispatchQueue(label: "queue.MainVMAudience")
    var lastUserIdPKValue = ""
    let videoSize = CGSize(width: 640, height: 360)
    var audioIsMute = false
    
    init(config: Config,
         syncManager: SyncManager) {
        self.mode = .pull
        self.config = config
        self.syncManager = syncManager
        self.pushUrlString = "rtmp://push.test1.agoramde.agoraio.cn/live/" + config.roomId
        let string = "http://play2.test1.agoramde.agoraio.cn/live/\(config.roomId).flv"
        self.pullUrlString = string
        super.init()
    }
    
    deinit {
        Log.info(text: "deinit", tag: "MainVMAudience")
    }
    
    func start() {
        queue.async { [weak self] in
            guard let `self` = self else { return }
            do {
                try self.startSignal()
                self.invokeMainVM(self, didJoinRoom: self.roomInfo)
                self.initMediaPlayer()
                self.subscribePKInfo()
            } catch let error {
                self.invokeMainVM(self, shouldShow: error.localizedDescription)
            }
        }
    }
    
    func invite(userIdPK: String) {
        
    }
    
    func getSceneRef() -> SceneReference {
        return sceneRef
    }
    
    func cancleConnect() {
        if mode == .rtc {
            resetPKInfo()
        }
    }
    
    func close() {
        sceneRef.unsubscribe()
        closeInternal()
    }
    
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    func revertMuteLocalAudio() {
        if mode == .rtc {
            audioIsMute = !audioIsMute
            agoraKit.muteLocalAudioStream(audioIsMute)
        }
    }
    
    /// `true` is mute
    func getLocalAudioMuteState() -> Bool {
        return audioIsMute
    }
}
