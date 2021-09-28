//
//  MainVM.swift
//  LivePK
//
//  Created by ZYP on 2021/9/23.
//

import Foundation
import AgoraRtcKit

protocol MainVMDelegate: NSObjectProtocol {
    func mainVMDidUpdateRenderInfos(renders: [RenderInfo])
    func mainVMShouldShowTips(tips: String)
}

class MainVM: NSObject {
    var roomName: String!
    var agoraKit: AgoraRtcEngineKit!
    weak var delegate: MainVMDelegate?
    var renderInfos = [RenderInfo]()
    let queue = DispatchQueue(label: "MainVM.queue")
    var appId: String!
    var currentIndexPath: IndexPath?
    let baseUid = 10000
    
    deinit {
        agoraKit.leaveChannel(nil)
    }
    
    init(roomName: String,
         appId: String) {
        super.init()
        self.appId = appId
        self.roomName = roomName
    }
    
    func start() {
        renderInfos = Array<UInt>(repeating: 0, count: 9).map({ RenderInfo(isLocal: false, uid: $0) })
        invokeDidUpdateRenderInfos(renders: renderInfos)
        
        joinChannel(channelName: roomName,
                    shouldLeaveChannel: false,
                    isAudience: true)
    }
    
    func addMe(indexPath: IndexPath) {
        
        guard !shouldRenderVideo(uid: renderInfos[indexPath.row].uid) else {
            return
        }
        
        if let oldIndexPath = currentIndexPath {
            renderInfos[oldIndexPath.row] = .empty
            invokeDidUpdateRenderInfos(renders: renderInfos)
        }
        
        currentIndexPath = indexPath
        let uid: UInt = UInt(indexPath.row + baseUid)
        joinChannel(channelName: roomName,
                    shouldLeaveChannel: true,
                    isAudience: false,
                    uid: uid)
    }
    
    func removeMe() {
        guard let indexPath = currentIndexPath else {
            return
        }
        
        renderInfos[indexPath.row] = .empty
        invokeDidUpdateRenderInfos(renders: renderInfos)
        currentIndexPath = nil
        joinChannel(channelName: roomName,
                    shouldLeaveChannel: true,
                    isAudience: true)
    }
    
    func leave(){
        agoraKit.leaveChannel(nil)
    }
    
    func shouldRenderVideo(uid: UInt) -> Bool {
        isValid(uid: uid)
    }
    
}
