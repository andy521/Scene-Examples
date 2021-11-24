//
//  MainVM+Handle+Audience.swift
//  LivePKCore
//
//  Created by ZYP on 2021/9/27.
//

import UIKit
import AgoraRtcKit

extension MainVM {
    func subscribeMedia(view: UIView, roomName: String) {
        if players[roomName] == nil {
            let config = AgoraRtcEngineConfig()
            let logConfig = AgoraLogConfig()
            config.appId = appId
            logConfig.level = .info
            config.logConfig = logConfig
            let engine = AgoraRtcEngineKit.sharedEngine(with: config,
                                                        delegate: nil)
            let mp = engine.createMediaPlayer(with: self)
        
            players[roomName] = mp
        }
        
        let player = players[roomName]!
        player.setView(view)
        player.open("https://webdemo-pull-hdl.agora.io/lbhd/\(roomName).flv", startPos: 0)
        player.play()
    }
    
    func makeConnect(roomName: String) {
        let info = RenderInfo(isLocal: false, uid: 0, roomName: roomName, type: .cdn)
        renderInfos.append(info)
        delegate?.mainVMDidUpdateRenderInfos(renders: renderInfos)
    }
    
    func joinAudienceRtmChannel() {
        let channelName = loginInfo.roomName
        let pkKey = kPKKey
        queue.async { [weak self] in
            do {
                try self?.manager.login()
                try self?.manager.join(channelName: channelName)
                let attrs = self?.manager.getAttributes(channelName: channelName)
                if let pkName = attrs?.filter({ $0.key == pkKey }).map({ $0.value }).first {
                    DispatchQueue.main.async { [weak self] in
                        self?.makeConnect(roomName: pkName)
                    }
                }
            } catch let error {
                self?.invokeShouldShowTips(tips: (error as! SyncError).description)
            }
        }
    }
}

extension MainVM {
    func handleAttributiesForAudience(channelName: String, attributes: [PKSyncManager.Attribute]) {
        if let formChannelName = attributes.filter({ $0.key == kPKKey }).map({ $0.value }).first {
            makeConnect(roomName: formChannelName)
        }
        else {
            if let localPlayer = players[loginInfo.roomName] {
                players = [loginInfo.roomName : localPlayer]
            }
            removeAllRemoteRenders()
            invokeDidUpdateRenderInfos(renders: renderInfos)
        }
    }
}
