//
//  Main+Event.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import Foundation
import AgoraRtcKit
import SyncManager

extension MainVM: AgoraDirectCdnStreamingEventDelegate,
                  AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinChannel channel: String,
                   withUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinChannel",
                 tag: "MainVM")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinedOfUid",
                 tag: "MainVM")
        if let remoteView = delegate?.mainVMShouldGetRemoteRender(self) {
            subscribeVideoRemote(view: remoteView, uid: uid)
        }
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didLeaveChannelWith stats: AgoraChannelStats) {
        /// 离开了旁推模式，检查是否要切换到其他模式？
        
    }
    
    func onDirectCdnStreamingStateChanged(_ state: AgoraDirectCdnStreamingState,
                                          error: AgoraDirectCdnStreamingError,
                                          message: String?) {
        if state == .stopped { /** 离开直推模式 **/
            joinRtcByPush()
        }
    }
}

extension MainVM: AgoraRtcMediaPlayerDelegate {
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol,
                             didChangedTo state: AgoraMediaPlayerState,
                             error: AgoraMediaPlayerError) {
        Log.info(text: "agoraMediaPlayer didChangedTo \(state.rawValue)",
                 tag: "AgoraRtcChannelDelegate")
        if state == .openCompleted {
            playerKit.play()
        }
    }
}

extension MainVM: ISyncManagerEventDelegate {
    func onCreated(object: IObject) {}
    func onUpdated(object: IObject) {
        Log.info(text: "onUpdated",
                 tag: "MainVM")
        guard let str = object.toJson(),
              let data = str.data(using: .utf8) else {
            return
        }
        do {
            let decoder = JSONDecoder()
            let roomInfo = try decoder.decode(RoomInfo.self, from: data)
            let userIdPK = roomInfo.userIdPK
            handleByPKInfo(userIdPK: userIdPK)
        } catch let error {
            Log.errorText(text: error.localizedDescription,
                          tag: "MainVM")
        }
    }
    func onDeleted(object: IObject) {}
    func onSubscribed() {}
    func onError(code: Int, msg: String) {}
}
