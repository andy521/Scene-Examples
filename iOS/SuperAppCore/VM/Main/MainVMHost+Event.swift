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
        Log.info(text: "didJoinChannel", tag: "MainVMHost")
        setMergeVideoLocal(engine: engine, uid: uid)
        engine.addPublishStreamUrl(pushUrlString,
                                   transcodingEnabled: true)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinChannel", tag: "MainVMHost")
        guard let view = delegate?.mainVMShouldGetRemoteRender(self) else {
            return
        }
        setMergeVideoRemote(engine: engine, uid: uid)
        invokeMainVMShouldStartRenderRemoteView(self)
        subscribeVideoRemote(view: view,
                             uid: uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, streamUnpublishedWithUrl url: String) {
        Log.info(text: "streamUnpublishedWithUrl", tag: "MainVMHost")
        let option = AgoraLeaveChannelOptions()
        option.stopMicrophoneRecording = false
        agoraKit.leaveChannel(option)
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


