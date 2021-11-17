//
//  EntryVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/15.
//

import Foundation
import SyncManager

let defaultChannelName = "PKByCDN"
let sync_collection_room_info = "RoomInfoCollection"
let sync_collection_user_info = "UserInfoCollection"

protocol EntryVMDelegate: NSObjectProtocol {
    func entryVMShouldUpdateInfos(infos: [EntryView.Info])
    func entryVMShouldShowTip(msg: String)
    func entryVMShouldEndRefreshing()
}

class EntryVM: NSObject {
    private let appId: String
    private var syncManager: SyncManager!
    private var sceneRef: SceneReference?
    private var syncSceneId = defaultChannelName
    weak var delegate: EntryVMDelegate?
    
    init(appId: String) {
        self.appId = appId
        super.init()
        commonInit()
    }
    
    func commonInit() {
        let config = SyncManager.RtmConfig.init(appId: appId,
                                             channelName: syncSceneId)
        self.syncManager = SyncManager(config: config,
                                       complete: { [weak self](code) in
                                        self?.join()
                                       })
    }
    
    func join() {
        let userId = StorageManager.uuid
        let scene = Scene(id: syncSceneId,
                          userId: userId,
                          property: [:])
        sceneRef = syncManager.joinScene(scene: scene,
                                         success: { [weak self](obj) in
                                            self?.getRooms()
                                         }, fail: { [weak self](error) in
                                            self?.delegate?.entryVMShouldShowTip(msg: error.description)
                                         })
    }
    
    func getRooms() {
        syncManager.getScenes { [weak self](objs) in
            let decoder = JSONDecoder()
            let rooms = objs.compactMap({ $0.toJson()?.data(using: .utf8) })
                .compactMap({ try? decoder.decode(RoomInfo.self, from: $0) })
            self?.udpateRooms(rooms: rooms)
            self?.delegate?.entryVMShouldEndRefreshing()
        } fail: { [weak self](error) in
            self?.delegate?.entryVMShouldShowTip(msg: error.description)
            self?.delegate?.entryVMShouldEndRefreshing()
        }
    }
    
    func udpateRooms(rooms: [RoomInfo]) {
        let infos = rooms.map({ EntryView.Info(imageName: $0.roomId.headImageName,
                                               title: $0.roomName,
                                               count: $0.userCount) })
        delegate?.entryVMShouldUpdateInfos(infos: infos)
    }
}

extension EntryVM: ISyncManagerEventDelegate {
    func onCreated(object: IObject) {
        print(object.toJson() ?? "")
    }
    
    func onUpdated(object: IObject) {
        print(object.toJson() ?? "")
    }
    
    func onDeleted(object: IObject) {
        print(object.toJson() ?? "")
    }
    
    func onSubscribed() {
        
    }
    
    func onError(code: Int, msg: String) {
        
    }
}
