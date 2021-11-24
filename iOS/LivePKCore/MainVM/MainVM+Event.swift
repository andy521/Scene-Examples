//
//  MainVM+Event.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation
import AgoraRtcKit

extension MainVM: PKSyncManagerDelegate {
    func pkSyncDidUpdateAttribute(manager: PKSyncManager, channelName: String, attributes: [PKSyncManager.Attribute]) {
        let text = "channelName:\(channelName), attributeUpdate update: \(attributes.map({ $0.key + ":" + $0.value }))"
        Log.info(text: text, tag: "pkSyncDidUpdateAttribute")
        
        if loginInfo.role == .audience { /** handle if audience **/
            handleAttributiesForAudience(channelName: channelName, attributes: attributes)
            return
        }
        
        if loginInfo.role == .broadcaster, attributes.filter({ $0.key == kPKKey }).map({ $0.value }).first != nil { /** handle if broadcaster **/
            handleAttributiesForBroadcaster(channelName: channelName, attributes: attributes)
            return
        }
        
        if attributes.filter({ $0.key == kPKKey }).map({ $0.value }).first == nil { /** remove remote **/
            handleAttributiesForRemoveRemote()
            return
        }
    }
}

extension MainVM: RemoteChannelHandlerDelegate {
    func remoteChannelHandler(_ handler: RemoteChannelHandler,
                              didJoinedOfUid uid: UInt) {
        Log.info(text: "remoteChannelHandler didJoinedOfUid: \(uid)",
                 tag: "RemoteChannelHandlerDelegate")
        guard let localChannel = channelLocal, let remoteChannel = channelRemote else {
            return
        }
        if uid != localChannel.localUid, uid != remoteChannel.localUid, uid > 200 {
            let info = RenderInfo(isLocal: false,
                                  uid: uid,
                                  roomName: remoteChannel.channelId,
                                  type: .rtc)
            
            if !renderInfos.contains(where: { $0.uid == uid }) {
                renderInfos.append(info)
                invokeDidUpdateRenderInfos(renders: renderInfos)
            }
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
