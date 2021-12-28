//
//  MainVMHost+SYNC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraSyncManager

extension MainVMHost {
    func startSignal() throws {
        do {
            join()
            try addRoomInfo()
            try addMember()
        } catch let error {
            throw error
        }
    }
    
    private func join() { /** 加入房间 **/
        let roomId = config.roomId
        let userId = StorageManager.uuid
        let scene = Scene(id: roomId,
                          userId: userId,
                          property: ["roomName" : config.roomName])
        let semp = DispatchSemaphore(value: 0)
        
        self.roomInfo = RoomInfo(createTime: 0,
                                 expiredTime: 0,
                                 roomId: roomId,
                                 roomName: config.roomName,
                                 userIdPK: "",
                                 userCount: 0,
                                 liveMode: 1)
        
        sceneRef = syncManager.joinScene(scene: scene,
                                         success: { _ in
            Log.info(text: "joinScene success",
                     tag: "MainVM")
            semp.signal()
        })
        semp.wait()
    }
    
    func addRoomInfo() throws {
        let roomId = config.roomId
        let createTime = config.createdTime
        let info = RoomInfo(createTime: createTime,
                            expiredTime: 0,
                            roomId: roomId,
                            roomName: config.roomName,
                            userIdPK: "",
                            userCount: 0,
                            liveMode: config.mode.rawValue)
        roomInfo = info
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        
        let property = info.dict
        sceneRef.update(data: property) { _ in
            Log.info(text: "addRoomInfo success",
                     tag: "MainVM")
            semp.signal()
        } fail: { (e) in
            Log.info(text: "addRoomInfo fail: \(e.errorDescription ?? "")",
                     tag: "MainVM")
            error = e
            semp.signal()
        }
        
        semp.wait()
        
        if let e = error {
            throw e
        }
    }
    
    private func addMember() throws { /** 把本地用户添加到人员列表 **/
        let userId = StorageManager.uuid
        let userName = StorageManager.userName
        let roomId = roomInfo.roomId
        let userInfo = SuperAppUserInfo(expiredTime: 0,
                                        userId: userId,
                                        userName: userName,
                                        roomId: roomId)
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        sceneRef.collection(className: "member")
            .add(data: userInfo.dict) { [weak self](obj) in
                Log.info(text: "addMember success",
                         tag: "MainVM")
                let id = obj.getId()
                self?.currentMemberId = id
                semp.signal()
            } fail: { (e) in
                Log.info(text: "addMember fail: \(e.errorDescription ?? "")",
                         tag: "MainVM")
                error = e
                semp.signal()
            }
        semp.wait()
        
        if let e = error {
            throw e
        }
    }
    
    func updatePKInfo(userIdPK: String) {
        sceneRef.update(data: ["userIdPK" : userIdPK]) { obj in
            Log.info(text: "updatePK success",
                     tag: "MainVM")
        } fail: { error in
            Log.info(text: "updatePK fail: \(error.errorDescription ?? "")",
                     tag: "MainVM")
        }
    }

    func subscribePKInfo() {
        sceneRef.subscribe(onUpdated: onUpdated(object:),
                           onDeleted: onDeleted(object:),
                           fail: { error in
            let text = "subscribePKInfo fail: \(error.errorDescription ?? "")"
            Log.info(text: text,
                     tag: "MainVM")
        })
    }
}
