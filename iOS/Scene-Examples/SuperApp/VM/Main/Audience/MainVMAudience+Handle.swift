//
//  MainVMAudience+Handle.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation

extension MainVMAudience {
    func handleByPKInfo(userIdPK: String) {
        let localUserId = StorageManager.uuid
        if lastUserIdPKValue != localUserId,
           userIdPK == localUserId { /** 收到上麦邀请 **/
            Log.info(text: "收到上麦申请", tag: "handleByPKInfo")
            lastUserIdPKValue = userIdPK
            changeToRtc()
            return
        }
        
        if lastUserIdPKValue == localUserId,
           userIdPK != localUserId { /** 下麦 **/
            Log.info(text: "下麦", tag: "handleByPKInfo")
            lastUserIdPKValue = userIdPK
            changeToPull()
            return
        }
    }
    
    func changeToRtc() { /** 切换rtc模式 **/
        Log.info(text: "切换到rtc模式", tag: "MainVMAudience")
        mode = .rtc
        mediaPlayer.stop()
        agoraKit.destroyMediaPlayer(mediaPlayer)
        mediaPlayer = nil
        
        joinRtc()
    }
    
    func changeToPull() { /** 切换到拉流模式 **/
        Log.info(text: "切换到拉流模式", tag: "MainVMAudience")
        mode = .pull
        leaveRtc()
        initMediaPlayer()
        invokeMainVMShouldStoptRenderRemoteView(self)
    }
    
    func closeInternal() {
        if let id = currentMemberId {
            sceneRef.collection(className: "member")
                .document(id: id)
                .delete(success: nil, fail: nil)
        }
        if mode == .pull {
            mediaPlayer.stop()
            agoraKit.destroyMediaPlayer(mediaPlayer)
            mediaPlayer = nil
        }
        else {
            resetPKInfo()
            agoraKit.leaveChannel(nil)
        }
    }
}
