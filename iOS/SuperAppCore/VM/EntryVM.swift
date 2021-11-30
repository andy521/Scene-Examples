//
//  EntryVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/15.
//

import Foundation
import SyncManager

let defaultChannelName = "PKByCDN"

protocol EntryVMDelegate: NSObjectProtocol {
    func entryVMShouldUpdateInfos(infos: [EntryView.Info])
    func entryVMShouldShowTip(msg: String)
    func entryVMShouldEndRefreshing()
}

class EntryVM: NSObject {
    private let appId: String
    var syncManager: SyncManager!
    var sceneRef: SceneReference?
    var rooms = [RoomItem]()
    weak var delegate: EntryVMDelegate?
    
    init(appId: String) {
        self.appId = appId
        super.init()
        commonInit()
    }
    
    func commonInit() {
        let config = SyncManager.RtmConfig.init(appId: appId,
                                                channelName: defaultChannelName)
        self.syncManager = SyncManager(config: config,
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
