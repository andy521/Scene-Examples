//
//  MainVMHost+Handle.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation
import AgoraRtcKit

extension MainVMHost {
    func handleByPKInfo(userIdPK: String) {
        if lastUserIdPKValue == "",
           userIdPK != "" { /** 观众上麦事件 **/
            Log.info(text: "观众上麦", tag: "MainVMHost")
            lastUserIdPKValue = userIdPK
            changeToByPassPush()
            return
        }
        
        if lastUserIdPKValue != "",
           userIdPK == "" { /** 观众下麦事件 **/
            Log.info(text: "观众下麦", tag: "MainVMHost")
            lastUserIdPKValue = userIdPK
            changeToPush()
            return
        }
    }
    
    private func changeToByPassPush() {
        if mode != .byPassPush {
            mode = .byPassPush
            leaveRtcByPush()
        }
    }
    
    private func changeToPush() {
        mode = .push
        leaveRtcByPassPush()
        invokeMainVMShouldStopRenderRemoteView(self)
    }
    
    func handleLeaveEvent() {
        if mode == .push {
            DispatchQueue.main.async { [weak self] in
                self?.joinRtcByPush()
            }
        }
    }
    
    func handleDirectStopEvent() {
        if mode == .byPassPush {
            DispatchQueue.main.async { [weak self] in
                self?.joinRtcByPassPush()
            }
        }
    }
    
    func closeInternal() {
        sceneRef.unsubscribe()
        sceneRef.delete(success: nil, fail: nil)
        if let id = currentMemberId {
            sceneRef.collection(className: "member")
                .document(id: id)
                .delete(success: nil, fail: nil)
        }
        agoraKit.delegate = nil
        
        if mode == .push {
            leaveRtcByPush()
        }
        else {
            leaveRtcByPassPush()
            updatePKInfo(userIdPK: "")
        }
        
        agoraKit = nil
        syncManager = nil
        sceneRef = nil
        AgoraRtcEngineKit.destroy()
    }
}

