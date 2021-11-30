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
            lastUserIdPKValue = userIdPK
            changeToByPassPush()
            return
        }
        
        if lastUserIdPKValue == localUserId,
           userIdPK != localUserId { /** 下麦 **/
            lastUserIdPKValue = userIdPK
            changeToPull()
            return
        }
    }
    
    func changeToByPassPush() { /** 切换到旁路推流模式 **/
        /// stop mediaPlayer
        mediaPlayer.stop()
        agoraKit.destroyMediaPlayer(mediaPlayer)
        mediaPlayer = nil
        
        joinRtcByPassPush()
    }
    
    func changeToPull() { /** 切换到拉流模式 **/
        leaveRtcByPassPush()
        initMediaPlayer()
    }
}
