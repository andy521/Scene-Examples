//
//  MainVMHost+Info.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation

extension MainVMHost {
    enum Mode: Int {
        /// 直推模式
        case push = 1
        /// 旁路推流模式
        case byPassPush = 2
    }
    
    struct Config {
        let appId: String
        let roomName: String
        let roomId: String
        let createdTime: TimeInterval
        let mode: Mode
    }
}
