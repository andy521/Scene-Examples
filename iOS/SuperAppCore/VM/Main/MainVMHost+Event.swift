//
//  MainVMHost+Event.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit
import SyncManager

extension MainVMHost: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinChannel channel: String,
                   withUid uid: UInt,
                   elapsed: Int) {
        agoraKit.addPublishStreamUrl(pushUrlString,
                                     transcodingEnabled: false)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        guard let view = delegate?.mainVMShouldGetRemoteRender(self) else {
            return
        }
        subscribeVideoRemote(view: view,
                             uid: uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didLeaveChannelWith stats: AgoraChannelStats) {
        handleLeaveEvent()
    }
}

extension MainVMHost: ISyncManagerEventDelegate {
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

extension MainVMHost: AgoraDirectCdnStreamingEventDelegate {
    func onDirectCdnStreamingStateChanged(_ state: AgoraDirectCdnStreamingState,
                                          error: AgoraDirectCdnStreamingError,
                                          message: String?) {
        if state == .stopped {
            handleDirectStopEvent()
        }
    }
}


