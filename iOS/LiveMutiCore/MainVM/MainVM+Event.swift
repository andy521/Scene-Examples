//
//  MainVM+Event.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation
import AgoraRtcKit

extension MainVM: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        Log.info(text: "rtcEngine didOccurWarning \(warningCode.rawValue)", tag: "AgoraRtcEngineDelegate")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        Log.info(text: "rtcEngine didOccurError \(errorCode.rawValue)", tag: "AgoraRtcEngineDelegate")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) { /** remote **/
        Log.info(text: "rtcEngine didJoinedOfUid \(uid) elapsed \(elapsed)", tag: "AgoraRtcEngineDelegate")
        
        guard isValid(uid: uid) else {
            return
        }
        
        let renderInfo = RenderInfo(isLocal: false, uid: uid)
        let index = Int(uid % UInt(baseUid))
        renderInfos[index] = renderInfo
        invokeDidUpdateRenderInfos(renders: renderInfos)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) { /** local **/
        Log.info(text: "rtcEngine didJoinChannel \(uid)", tag: "AgoraRtcEngineDelegate")
        
        guard isValid(uid: uid) else {
            return
        }
        
        let renderInfo = RenderInfo(isLocal: true, uid: uid)
        let index = Int(uid % UInt(baseUid))
        renderInfos[index] = renderInfo
        invokeDidUpdateRenderInfos(renders: renderInfos)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) { /** leave **/
        Log.info(text: "rtcEngine didOfflineOfUid \(uid)", tag: "AgoraRtcEngineDelegate")
        
        guard isValid(uid: uid) else {
            return
        }
        
        let index = Int(uid % UInt(baseUid))
        renderInfos[index] = .empty
        invokeDidUpdateRenderInfos(renders: renderInfos)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didLeaveChannelWith stats: AgoraChannelStats) {
        Log.info(text: "rtcEngine didLeaveChannelWith", tag: "AgoraRtcEngineDelegate")
        if let index = renderInfos.firstIndex(where: { $0.isLocal }) {
            renderInfos[index] = .empty
            invokeDidUpdateRenderInfos(renders: renderInfos)
        }
    }
}

extension MainVM {
    func isValid(uid: UInt) -> Bool {
        return uid >= baseUid && uid <= baseUid + 8
    }
}

