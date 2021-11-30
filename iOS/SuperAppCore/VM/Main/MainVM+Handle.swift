//
//  MainVM+Handle.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit

extension MainVM {
    func handleByPKInfo(userIdPK: String) {
        if userIdPK != "" {
            /// 如果当前是直推，则切换到旁推模式。
            if mode == .push {
                
                return
            }
            
            /// 如果当前是旁推，则改变transEncode
            if mode == .bypassPush {
                
                return
            }
        }
        
        /// 切换到观众拉流模式
    }
    
    func changeToByPassPush() {
        
    }
}
