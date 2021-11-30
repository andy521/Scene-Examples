//
//  MainVMHost.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import SyncManager
import AgoraRtcKit

class MainVMHost: NSObject, MainVMProtocol {
    var delegate: MainVMDelegate?
    let config: Config
    let syncManager: SyncManager
    let pushUrlString: String
    
    var mode: Mode!
    var sceneRef: SceneReference!
    var roomInfo: RoomInfo!
    var agoraKit: AgoraRtcEngineKit!
    let queue = DispatchQueue(label: "queue.MainVMHost")
    var lastUserIdPKValue = ""
    
    init(config: Config,
         syncManager: SyncManager) {
        self.mode = config.mode
        self.config = config
        self.syncManager = syncManager
        self.pushUrlString = "rtmp://examplepush.agoramde.agoraio.cn/live/" + config.roomId
        super.init()
    }
    
    func start() {
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
    
    func invite(userIdPK: String) {
        updatePKInfo(userIdPK: userIdPK)
    }
    
    func getSceneRef() -> SceneReference {
        return sceneRef
    }
}
