//
//  MainVMAudience+Event.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit
import SyncManager

extension MainVMAudience: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
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

extension MainVMAudience: ISyncManagerEventDelegate {
    
    func onUpdated(object: IObject) {
        Log.info(text: "onUpdated",
                 tag: "MainVM")
        do {
            if let userIdPK = try object.getPropertyWith(key: "userIdPK", type: String.self) as? String {
                handleByPKInfo(userIdPK: userIdPK)
            }
        } catch let error {
            Log.errorText(text: error.localizedDescription,
                          tag: "MainVM")
        }
    }
    
    func onCreated(object: IObject) {}
    func onDeleted(object: IObject) {}
    func onSubscribed() {}
    func onError(code: Int, msg: String) {}
}
