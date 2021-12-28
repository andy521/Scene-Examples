//
//  SuperAppSyncUtil.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import Foundation
import AgoraSyncManager

protocol SuperAppSyncUtilDelegate: NSObjectProtocol {
    func superAppSyncUtilDidPkAccept(util: SuperAppSyncUtil, pkName: String)
    func superAppSyncUtilDidPkCancle(util: SuperAppSyncUtil)
    func superAppSyncUtilDidSceneClose(util: SuperAppSyncUtil)
}

class SuperAppSyncUtil {
    private let appId: String
    /// 场景所在的默认房间id
    private let defaultScenelId: String
    /// 房间id
    private let sceneId: String
    /// 房间名称 用于显示
    private let sceneName: String
    private let userId: String
    private let userName: String
    private var sceneRef: SceneReference!
    private var manager: AgoraSyncManager!
    private var currentMemberId: String!
    let queue = DispatchQueue(label: "queue.SuperAppSyncUtil")
    
    typealias CompltedBlock = (Error?) -> ()
    weak var delegate: SuperAppSyncUtilDelegate?
    
    init(appId: String,
         defaultScenelId: String,
         sceneId: String,
         sceneName: String,
         userId: String,
         userName: String) {
        self.appId = appId
        self.defaultScenelId = defaultScenelId
        self.sceneId = sceneId
        self.sceneName = sceneName
        self.userId = userId
        self.userName = userName
    }
    
    func joinByAudience(complted: CompltedBlock? = nil) {
        queue.async { [weak self] in
            do {
                try self?.joinScene()
                try self?.addMember()
                complted?(nil)
            } catch let error {
                complted?(error)
            }
        }
    }
    
    func joinByHost(createTime: TimeInterval,
                    liveMode: Int,
                    complted: CompltedBlock? = nil) {
        queue.async { [weak self] in
            do {
                try self?.joinScene()
                try self?.addRoomInfo(createTime: createTime, liveMode: liveMode)
                try self?.addMember()
                complted?(nil)
            } catch let error {
                complted?(error)
            }
        }
    }
    
    private func joinScene() throws {
        let semp = DispatchSemaphore(value: 0)
        var error: Error?
        
        /// create
        let config = AgoraSyncManager.RtmConfig(appId: appId, channelName: defaultScenelId)
        manager = AgoraSyncManager(config: config, complete: { code in
            let msg = "AgoraSyncManager init fail \(code)"
            LogUtils.logError(message: msg, tag: .currentTag)
            semp.signal()
        })
        semp.wait()
        
        /// join
        let scene = Scene(id: sceneId,
                          userId: userId,
                          property: ["roomName" : sceneName])
        sceneRef = manager.joinScene(scene: scene,
                                     success: { _ in
            LogUtils.logInfo(message: "joinScene success", tag: .currentTag)
            semp.signal()
        }, fail:  { e in
            error = e
            semp.signal()
            let msg = "joinScene fail \(e.description)"
            LogUtils.logError(message: msg, tag: .currentTag)
        })
        semp.wait()
        
        if let e = error {
            throw e
        }
    }
    
    private func addRoomInfo(createTime: TimeInterval,
                             liveMode: Int) throws {
        let property = RoomInfo(createTime: createTime,
                                expiredTime: 0,
                                roomId: sceneId,
                                roomName: sceneName,
                                userIdPK: "",
                                userCount: 0,
                                liveMode: liveMode).dict
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        
        sceneRef.update(data: property) { _ in
            LogUtils.logInfo(message: "addRoomInfo success",
                             tag: .currentTag)
            semp.signal()
        } fail: { (e) in
            let msg = "addRoomInfo fail: \(e.errorDescription ?? "")"
            LogUtils.logError(message: msg, tag: .currentTag)
            error = e
            semp.signal()
        }
        
        semp.wait()
        
        if let e = error {
            throw e
        }
    }
    
    private func addMember() throws { /** 把本地用户添加到人员列表 **/
        let userInfo = SuperAppUserInfo(expiredTime: 0,
                                        userId: userId,
                                        userName: userName,
                                        roomId: sceneId)
        let semp = DispatchSemaphore(value: 0)
        var error: SyncError?
        sceneRef.collection(className: "member")
            .add(data: userInfo.dict) { [weak self](obj) in
                LogUtils.logInfo(message: "addMember success", tag: .currentTag)
                let id = obj.getId()
                self?.currentMemberId = id
                semp.signal()
            } fail: { (e) in
                let msg = "addMember fail: \(e.errorDescription ?? "")"
                LogUtils.logError(message: msg, tag: .currentTag)
                error = e
                semp.signal()
            }
        semp.wait()
        if let e = error {
            throw e
        }
    }
    
    func subscribePKInfo() {
        sceneRef.subscribe(onUpdated: onPkInfoUpdated(object:),
                           onDeleted: onPkInfoDeleted(object:),
                           fail: { error in
            let msg = "subscribePKInfo fail: \(error.errorDescription ?? "")"
            LogUtils.logError(message: msg, tag: .currentTag)
        })
    }
    
    func updatePKInfo(userIdPK: String) {
        sceneRef.update(data: ["userIdPK" : userIdPK]) { obj in
            LogUtils.logInfo(message: "updatePKInfo success)", tag: .currentTag)
        } fail: { error in
            let msg = "updatePKInfo fail: \(error.errorDescription ?? "")"
            LogUtils.logError(message: msg, tag: .currentTag)
        }
    }
    
    func resetPKInfo() {
        sceneRef.update(data: ["userIdPK" : ""]) { _ in
            LogUtils.logInfo(message: "resetPKInfo success)", tag: .currentTag)
        } fail: { error in
            let msg = "resetPKInfo fail: \(error.errorDescription ?? "")"
            LogUtils.logError(message: msg, tag: .currentTag)
        }
    }
}

extension SuperAppSyncUtil {
    func onPkInfoUpdated(object: IObject) {
        if let userIdPK = object.getPropertyWith(key: "userIdPK", type: String.self) as? String {
            userIdPK.count > 0 ? invokeDidPkAccept(pkName: userIdPK) : invokeDidPkCancle()
        }
    }
    
    func onPkInfoDeleted(object: IObject) {
        if object.getId() == sceneId {
            invokeDidSceneClose()
        }
    }
}

extension SuperAppSyncUtil {
    func invokeDidPkAccept(pkName: String) {
        if Thread.isMainThread {
            delegate?.superAppSyncUtilDidPkAccept(util: self, pkName: pkName)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            guard let `self` = self else { return }
            self.delegate?.superAppSyncUtilDidPkAccept(util: self, pkName: pkName)
        }
    }
    
    func invokeDidPkCancle() {
        if Thread.isMainThread {
            delegate?.superAppSyncUtilDidPkCancle(util: self)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            guard let `self` = self else { return }
            self.delegate?.superAppSyncUtilDidPkCancle(util: self)
        }
    }
    
    func invokeDidSceneClose() {
        if Thread.isMainThread {
            delegate?.superAppSyncUtilDidSceneClose(util: self)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            guard let `self` = self else { return }
            self.delegate?.superAppSyncUtilDidSceneClose(util: self)
        }
    }
}

extension String {
    fileprivate static var currentTag: String {
        return "SuperAppSyncUtil"
    }
}
