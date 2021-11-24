//
//  MainVM+Handle.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation

extension MainVM {
    func isPking(attributes: [PKSyncManager.Attribute]) -> Bool {
        return attributes.contains(where: { $0.key == kPKKey})
    }
    
    func removeAllRemoteRenders() {
        let localRoomName = loginInfo.roomName
        renderInfos = renderInfos.filter({ $0.roomName == localRoomName })
    }
    
    func handleAttributiesForRemoveRemote() {
        guard let channel = channelRemote else {
            return
        }
        removeAllRemoteRenders()
        invokeDidUpdateRenderInfos(renders: renderInfos)
        agoraKit.leaveChannelEx(channel) { stats in
            Log.info(text: "channelRemote leave \(stats.description)",
                     tag: "pkSyncDidUpdateAttribute")
        }
        channelRemote = nil
        Log.info(text: "channelRemote has leave", tag: "pkSyncDidUpdateAttribute")
    }
}
