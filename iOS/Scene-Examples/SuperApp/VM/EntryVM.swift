//
//  EntryVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/15.
//

import Foundation
import AgoraSyncManager

let defaultChannelName = "PKByCDN"

protocol EntryVMDelegate: NSObjectProtocol {
    func entryVMShouldUpdateInfos(infos: [EntryView.Info])
    func entryVMShouldShowTip(msg: String)
    func entryVMShouldEndRefreshing()
}

class EntryVM: NSObject {
    private let appId: String
    var syncManager: AgoraSyncManager!
    var sceneRef: SceneReference?
    var rooms = [RoomItem]()
    weak var delegate: EntryVMDelegate?
    
    init(appId: String) {
        self.appId = appId
        super.init()
        commonInit()
    }
    
    func commonInit() {
        let config = AgoraSyncManager.RtmConfig.init(appId: appId,
                                                     channelName: defaultChannelName)
        self.syncManager = AgoraSyncManager(config: config,
                                            complete: { [weak self](code) in
            self?.getRooms()
        })
    }
    
    func getRooms() {
        syncManager.getScenes { [weak self](objs) in
            let decoder = JSONDecoder()
            let rooms = objs.compactMap({ $0.toJson()?.data(using: .utf8) })
                .compactMap({ try? decoder.decode(RoomItem.self, from: $0) })
            self?.udpateRooms(rooms: rooms)
            self?.delegate?.entryVMShouldEndRefreshing()
        } fail: { [weak self](error) in
            self?.delegate?.entryVMShouldShowTip(msg: error.description)
            self?.delegate?.entryVMShouldEndRefreshing()
        }
    }
    
    func deleteAllRooms() {
        let keys = rooms.map({ $0.id })
        syncManager.deleteScenes(sceneIds: keys) { [weak self] in
            self?.udpateRooms(rooms: [])
        } fail: { error in
            Log.error(error: error.description, tag: "deleteAllRooms")
        }
    }
    
    func udpateRooms(rooms: [RoomItem]) {
        let infos = rooms.map({ EntryView.Info(imageName: $0.id.headImageName,
                                               title: $0.roomName,
                                               count: 0) })
        self.rooms = rooms
        delegate?.entryVMShouldUpdateInfos(infos: infos)
    }
    
    func getRoomInfo(index: Int) -> RoomItem? {
        rooms[index]
    }
}

