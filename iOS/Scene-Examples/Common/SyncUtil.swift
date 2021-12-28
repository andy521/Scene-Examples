//
//  SyncUtil.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/3.
//

import UIKit
import AgoraSyncManager

class SyncUtil: NSObject {
    private static var manager: AgoraSyncManager?
    private override init() { }
    private static var sceneRefs: [String: SceneReference] = [String: SceneReference]()
    
    static func initSyncManager(sceneId: String) {
        let config = AgoraSyncManager.RtmConfig(appId: KeyCenter.AppId, channelName: sceneId)
        manager = AgoraSyncManager(config: config, complete: { _ in })
    }
    
    class func joinScene(id: String,
                         userId: String,
                         property: [String: Any]?,
                         success: SuccessBlockObj?,
                         fail: FailBlock? = nil) {
        guard let manager = manager else { return }
        let jsonString = JSONObject.toJsonString(dict: property) ?? ""
        let params = JSONObject.toDictionary(jsonStr: jsonString)
        let scene = Scene(id: id, userId: userId, property: params)
        let sceneRef = manager.joinScene(scene: scene, success: success, fail: fail)
        sceneRefs[id] = sceneRef
    }
    
    class func fetchAll(success: SuccessBlock?,
                        fail: FailBlock?) {
        manager?.getScenes(success: success, fail: fail)
    }
    
    class func fetch(id: String, key: String?,
                     success: SuccessBlockObjOptional?,
                     fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.get(key: key,
                      success: success,
                      fail: fail)
    }
    
    class func update(id: String,
                      key: String?,
                      params: [String: Any],
                      success: SuccessBlock?,
                      fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.update(key: key,
                         data: params,
                         success: success,
                         fail: fail)
    }
    
    class func subscribe(id: String,
                         key: String?,
                         onCreated: OnSubscribeBlock? = nil,
                         onUpdated: OnSubscribeBlock? = nil,
                         onDeleted: OnSubscribeBlock? = nil,
                         onSubscribed: OnSubscribeBlockVoid? = nil) {
        let sceneRef = sceneRefs[id]
        sceneRef?.subscribe(key: key,
                            onCreated: onCreated,
                            onUpdated: onUpdated,
                            onDeleted: onDeleted,
                            onSubscribed: onSubscribed,
                            fail: nil)
    }
    
    class func unsubscribe(id: String, key: String?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.unsubscribe(key: key)
    }
    
    class func delete(id: String,
                      success: SuccessBlock? = nil,
                      fail: FailBlock? = nil) {
        let sceneRef = sceneRefs[id]
        sceneRef?.delete(success: success, fail: fail)
    }
    
    class func fetchCollection(id: String,
                               className: String,
                               success: SuccessBlock?,
                               fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.collection(className: className).get(success: success, fail: fail)
    }
    
    class func addCollection(id: String,
                             className: String,
                             params: [String: Any],
                             success: SuccessBlockObj?,
                             fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.collection(className: className)
            .add(data: params,
                 success: success,
                 fail: fail)
    }
    
    class func updateCollection(id: String,
                                className: String,
                                objectId: String,
                                params: [String: Any],
                                success: SuccessBlock?,
                                fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        sceneRef?.collection(className: className)
            .document(id: objectId)
            .update(key: nil,
                    data: params,
                    success: success,
                    fail: fail)
    }
    
    class func subscribeCollection(id: String,
                                   className: String,
                                   documentId: String? = nil,
                                   onCreated: OnSubscribeBlock? = nil,
                                   onUpdated: OnSubscribeBlock? = nil,
                                   onDeleted: OnSubscribeBlock? = nil,
                                   onSubscribed: OnSubscribeBlockVoid? = nil) {
        let sceneRef = sceneRefs[id]
        if documentId == nil {
            sceneRef?.collection(className: className)
                .document()
                .subscribe(key: nil,
                           onCreated: onCreated,
                           onUpdated: onUpdated,
                           onDeleted: onDeleted,
                           onSubscribed: onSubscribed,
                           fail: nil)
        } else {
            sceneRef?.collection(className: className)
                .document(id: documentId ?? "")
                .subscribe(key: nil,
                           onCreated: onCreated,
                           onUpdated: onUpdated,
                           onDeleted: onDeleted,
                           onSubscribed: onSubscribed,
                           fail: nil)
        }
    }
    
    class func unsubscribeCollection(id: String, className: String) {
        let sceneRef = sceneRefs[id]
        sceneRef?.collection(className: className).document().unsubscribe(key: nil)
    }
    
    class func leaveScene(id: String) {
        sceneRefs.removeValue(forKey: id)
    }
    
    class func deleteDocument(id: String,
                              className: String,
                              objectId: String?,
                              success: SuccessBlock?,
                              fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        /// 删除单条数据
        sceneRef?.collection(className: className).document(id: objectId ?? "").delete(success: success, fail: fail)
    }
    
    class func deleteCollection(id: String,
                                className: String,
                                success: SuccessBlock?,
                                fail: FailBlock?) {
        let sceneRef = sceneRefs[id]
        /// 删除数据
        sceneRef?.collection(className: className).delete(success: success, fail: fail)
    }
}
