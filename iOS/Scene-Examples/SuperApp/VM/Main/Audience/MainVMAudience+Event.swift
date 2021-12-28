//
//  MainVMAudience+Event.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit
import AgoraSyncManager

extension MainVMAudience: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinedOfUid", tag: "MainVM")
        guard let view = delegate?.mainVMShouldGetRemoteRender(self) else {
            return
        }
        invokeMainVMShouldStartRenderRemoteView(self)
        subscribeVideoRemote(view: view,
                             uid: uid)
    }
}

extension MainVMAudience: AgoraRtcMediaPlayerDelegate {
    
    
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol,
                             didChangedTo state: AgoraMediaPlayerState,
                             error: AgoraMediaPlayerError) {
        Log.info(text: "agoraRtcMediaPlayer didChangedTo \(state.rawValue) \(error.rawValue)", tag: "MainVMAudience")
        if state == .openCompleted {
            Log.info(text: "openCompleted", tag: "MainVMAudience")
            playerKit.play()
        }
    }
}

extension MainVMAudience {
    func onUpdated(object: IObject) {
        Log.info(text: "onUpdated",
                 tag: "MainVM")
        
        if let userIdPK = object.getPropertyWith(key: "userIdPK", type: String.self) as? String {
            handleByPKInfo(userIdPK: userIdPK)
        }
        
        if let expiredTime = object.getPropertyWith(key: "expiredTime", type: String.self) as? String, expiredTime == "-1" {
            Log.info(text: "update expiredTime \(expiredTime)", tag: "MainVM")

        }
    }
    
    func onDeleted(object: IObject) {
        Log.info(text: "onDeleted",
                 tag: "MainVM")
        if object.getId() == roomInfo.roomId {
            invokeMainVMShouldCloseRoom(self)
        }
    }
}
