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
        let user = AgoraLiveTranscodingUser()
        user.rect = CGRect(x: 0,
                           y: 0,
                           width: videoSize.width,
                           height: videoSize.height)
        user.uid = uid
        user.zOrder = 1
        liveTranscoding.size = videoSize
        liveTranscoding.videoFramerate = 15
        liveTranscoding.add(user)
        engine.setLiveTranscoding(liveTranscoding)
        engine.addPublishStreamUrl(pushUrlString,
                                   transcodingEnabled: true)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        guard let view = delegate?.mainVMShouldGetRemoteRender(self) else {
            return
        }

        let user = AgoraLiveTranscodingUser()
        user.rect = CGRect(x: 0.5 * videoSize.width,
                           y: 0.1 * videoSize.height,
                           width: 0.5 * videoSize.width,
                           height: 0.5 * videoSize.height)
        user.uid = uid
        user.zOrder = 2
        liveTranscoding.add(user)
        engine.setLiveTranscoding(liveTranscoding)
        invokeMainVMShouldStartRenderRemoteView(self)
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


