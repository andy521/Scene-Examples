//
//  MainVMHost+Handle.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation

extension MainVMHost {
    func handleByPKInfo(userIdPK: String) {
        if lastUserIdPKValue == "",
           userIdPK != "" { /** 观众上麦事件 **/
            lastUserIdPKValue = userIdPK
            changeToByPassPush()
            return
        }
        
        if lastUserIdPKValue != "",
           userIdPK == "" { /** 观众下麦事件 **/
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
}

