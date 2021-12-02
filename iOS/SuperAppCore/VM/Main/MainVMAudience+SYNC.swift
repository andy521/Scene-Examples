//
//  MainVMAudience+SYNC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import SyncManager

extension MainVMAudience {
    func startSignal() throws {
        do {
            try join()
            try addMember()
        } catch let error {
            throw error
        }
    }
    
    private func join() throws { /** 加入房间 **/
        let roomId = config.roomId
        let userId = StorageManager.uuid
        let scene = Scene(id: roomId,
                          userId: userId,
                          property: ["roomName" : config.roomName])
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        
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
                                         },
                                         fail: { (e) in
                                            Log.info(text: "joinScene fail: \(e.errorDescription ?? "")",
                                                     tag: "MainVM")
                                            error = e
                                            semp.signal()
                                            
                                         })
        semp.wait()
        
        if let e = error {
            throw e
        }
    }
    
    private func addMember() throws { /** 把本地用户添加到人员列表 **/
        let userId = StorageManager.uuid
        let userName = StorageManager.userName
        let roomId = roomInfo.roomId
        let userInfo = UserInfo(expiredTime: 0,
                                userId: userId,
                                userName: userName,
                                roomId: roomId)
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        sceneRef.collection(className: "member")
            .add(data: userInfo.dict) { _ in
                Log.info(text: "addMember success",
                         tag: "MainVM")
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
    
    func subscribePKInfo() {
        if let error = sceneRef.subscribe(observer: self) {
            let text = "subscribePKInfo fail: \(error.errorDescription ?? "")"
            Log.info(text: text,
                     tag: "MainVM")
        }
    }
    
    func resetPKInfo() {
        sceneRef.update(data: ["userIdPK" : ""]) { _ in
            Log.info(text: "updatePK success)",
                     tag: "MainVM")
        } fail: { error in
            Log.info(text: "updatePK fail: \(error.errorDescription ?? "")",
                     tag: "MainVM")
        }
    }
}
