//
//  MainVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/18.
//

import Foundation
import SyncManager
import AgoraRtcKit

protocol MainVMDelegate: NSObjectProtocol {
    func mainVM(_ vm: MainVM, didJoinRoom info: RoomInfo)
    func mainVM(_ vm: MainVM, shouldShow tips: String)
    func mainVMShouldGetLocalRender(_ vm: MainVM) -> UIView
    func mainVMShouldGetRemoteRender(_ vm: MainVM) -> UIView
}

class MainVM: NSObject {
    private let config: Config
    private var syncManager: SyncManager!
    var sceneRef: SceneReference!
    private var roomInfo: RoomInfo!
    weak var delegate: MainVMDelegate?
    var agoraKit: AgoraRtcEngineKit!
    var mediaPlayer: AgoraRtcMediaPlayerProtocol!
    let videoConfig = AgoraVideoEncoderConfiguration(size: CGSize(width: 640, height: 360),
                                                     frameRate: .fps15,
                                                     bitrate: 700,
                                                     orientationMode: .fixedPortrait,
                                                     mirrorMode: .auto)
    var pushUrlString: String!
    var pullUrlString: String!
    
    init(config: Config,
         syncManager: SyncManager) {
        self.config = config
        self.syncManager = syncManager
        self.pushUrlString = "rtmp://examplepush.agoramde.agoraio.cn/live/" + config.roomId
        self.pullUrlString = "http://examplepull.agoramde.agoraio.cn/live/\(config.roomId).flv"
        super.init()
    }
    
    func start() {
        /// 1. 往currentChannel写入roomInfo
        ///    角色需要区分？只是主播要更新\主播各自写\观众只读
        /// 2. 往defaultChannel写入房间信息（）
        ///    可能不需要，因为join的时候把这些信息写在property里面了
        /// 3. 往members写入本用户信息
        
        join()
    }
    
    func join() { /** 加入房间并设置房间参数 **/
        let roomId = config.roomId
        let userId = StorageManager.uuid
        let createTime = config.createdTime
        let info = RoomInfo(createTime: createTime,
                            expiredTime: 0,
                            roomId: roomId,
                            roomName: config.roomName,
                            userIdPK: "",
                            userCount: 0,
                            liveMode: 1)
        roomInfo = info
        let scene = Scene(id: roomId,
                          userId: userId,
                          property: info.dict)
        sceneRef = syncManager.joinScene(scene: scene,
                                         success: { [weak self](_) in
                                            Log.info(text: "joinScene success",
                                                     tag: "MainVM")
                                            self?.addMember()
                                         },
                                         fail: { [weak self](error) in
                                            Log.info(text: "joinScene fail: \(error.errorDescription ?? "")",
                                                     tag: "MainVM")
                                            guard let self = self else {
                                                return
                                            }
                                            self.invokeMainVM(self, shouldShow: error.errorDescription ?? "")
                                         })
    }
    
    func addMember() { /** 把本地用户添加到人员列表 **/
        let userId = StorageManager.uuid
        let userName = StorageManager.userName
        let roomId = roomInfo.roomId
        let userInfo = UserInfo(expiredTime: 0,
                                userId: userId,
                                userName: userName,
                                roomId: roomId)
        sceneRef.collection(className: "member")
            .add(data: userInfo.dict) { [weak self](_) in
                Log.info(text: "addMember success",
                         tag: "MainVM")
                guard let self = self else {
                    return
                }
                self.invokeMainVM(self,
                                  didJoinRoom: self.roomInfo)
                self.config.entryType == .asCreator ? self.joinRtcDirect() : self.initMediaPlayer(config: self.config)
                
            } fail: { [weak self](error) in
                Log.info(text: "addMember fail: \(error.errorDescription ?? "")",
                         tag: "MainVM")
                guard let self = self else {
                    return
                }
                self.invokeMainVM(self, shouldShow: error.errorDescription ?? "")
            }
        
        
        
    }
    
    func fetchMembers(success: (IObject) -> ()) {
        
    }
}
