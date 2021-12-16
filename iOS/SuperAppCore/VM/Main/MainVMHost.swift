//
//  MainVMHost.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import SyncManager
import AgoraRtcKit

class MainVMHost: NSObject, MainVMProtocol {
    weak var delegate: MainVMDelegate?
    let config: Config
    var syncManager: SyncManager!
    let pushUrlString: String
    
    var mode: Mode!
    var sceneRef: SceneReference!
    var roomInfo: RoomInfo!
    var agoraKit: AgoraRtcEngineKit!
    let queue = DispatchQueue(label: "queue.MainVMHost")
    var lastUserIdPKValue = ""
    let liveTranscoding = AgoraLiveTranscoding.default()
    let videoSize = CGSize(width: 640, height: 360)
    var audioIsMute = false
    
    init(config: Config,
         syncManager: SyncManager) {
        self.mode = config.mode
        self.config = config
        self.syncManager = syncManager
        self.pushUrlString = "rtmp://push.test1.agoramde.agoraio.cn/live/" + config.roomId
        super.init()
    }
    
    deinit {
        Log.info(text: "deinit", tag: "MainVMHost")
    }
    
    func start() {
        Log.info(text: pushUrlString, tag: "url")
        queue.async { [weak self] in
            guard let `self` = self else { return }
            do {
                try self.startSignal()
                self.invokeMainVM(self, didJoinRoom: self.roomInfo)
                self.mode == .push ? self.joinRtcByPush() : self.joinRtcByPassPush()
                self.subscribePKInfo()
            } catch let error {
                self.invokeMainVM(self, shouldShow: error.localizedDescription)
            }
        }
    }
    
    func invite(userIdPK: String) { /** 邀请连麦 **/
        updatePKInfo(userIdPK: userIdPK)
    }
    
    func getSceneRef() -> SceneReference {
        return sceneRef
    }
    
    func cancleConnect() { /** 取消连麦 **/
        if mode == .byPassPush {
            updatePKInfo(userIdPK: "")
        }
    }
    
    func close() {
        closeInternal()
    }
    
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    func revertMuteLocalAudio() {
        audioIsMute = !audioIsMute
        agoraKit.muteLocalAudioStream(audioIsMute)
    }
    
    /// `true` is mute
    func getLocalAudioMuteState() -> Bool {
        return audioIsMute
    }
}
