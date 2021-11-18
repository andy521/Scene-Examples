//
//  Main+Event.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import Foundation
import AgoraRtcKit

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
