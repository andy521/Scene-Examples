//
//  MainVM+RemoteChannelHandler.swift
//  LivePKCore
//
//  Created by ZYP on 2021/11/24.
//

import AgoraRtcKit

protocol RemoteChannelHandlerDelegate: NSObject {
    /// 加入对方房间后，收到对方的join回调
    func remoteChannelHandler(_ handler: MainVM.RemoteChannelHandler,
                              didJoinedOfUid uid: UInt)
}

extension MainVM {
    class RemoteChannelHandler: NSObject, AgoraRtcEngineDelegate {
        weak var delegate: RemoteChannelHandlerDelegate?
        
        deinit {
            Log.info(text: "RemoteChannelHandler deinit",
                     tag: "RemoteChannelHandler")
        }
        
        func rtcEngine(_ engine: AgoraRtcEngineKit,
                       didJoinedOfUid uid: UInt,
                       elapsed: Int) {
            Log.info(text: "rtcChannel didJoinedOfUid: \(uid)",
                     tag: "RemoteChannelHandler")
            delegate?.remoteChannelHandler(self,
                                           didJoinedOfUid: uid)
        }
    }
}
