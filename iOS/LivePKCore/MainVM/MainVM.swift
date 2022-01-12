//
//  MainVM.swift
//  LivePK
//
//  Created by ZYP on 2021/9/23.
//

import Foundation
import AgoraRtcKit

protocol MainVMDelegate: NSObjectProtocol {
    func mainVMDidUpdateRenderInfos(renders: [RenderInfo])
    func mainVMShouldShowTips(tips: String)
}

class MainVM: NSObject {
    var loginInfo: LoginInfo!
    var agoraKit: AgoraRtcEngineKit!
    var channelLocal: AgoraRtcConnection?
    var channelRemote: AgoraRtcConnection?
    weak var delegate: MainVMDelegate?
    var renderInfos = [RenderInfo]()
    var pkRoomName: String?
    var players = [String : AgoraRtcMediaPlayerProtocol]()
    var manager: PKSyncManager!
    let queue = DispatchQueue(label: "MainVM.queue")
    var appId: String!
    let kPKKey = "PK"
    
    deinit {
        Log.info(text: "deinit", tag: "MainVM")
    }
    
    init(loginInfo: LoginInfo,
         appId: String) {
        super.init()
        self.appId = appId
        self.loginInfo = loginInfo
        self.manager = PKSyncManager(appId: appId)
    }
    
    func start() {
        manager.delegate = self
        switch loginInfo.role {
        case .audience:
            makeConnect(roomName: loginInfo.roomName)
            joinAudienceRtmChannel()
            break
        case .broadcaster:
            joinChannelLocal()
            break
        }
    }
    
    func destory() {
        if channelLocal != nil {
            agoraKit.leaveChannel(nil)
            channelLocal = nil
        }
        
        if let remoteChannel = channelRemote {
            agoraKit.leaveChannelEx(remoteChannel, leaveChannelBlock: nil)
            channelRemote = nil
        }
        
        for mp in players.values {
            agoraKit.destroyMediaPlayer(mp)
        }
        
        players = [:]
        
        agoraKit = nil
        manager = nil
        
        AgoraRtcEngineKit.destroy()
    }
}
