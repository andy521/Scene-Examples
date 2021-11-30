//
//  MainVM+Info.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import Foundation

extension MainVM {
    enum Mode: Int {
        /// 拉流模式（观众）
        case pull = 0
        /// 直推模式（主播）
        case push = 1
        /// 旁推模式（主播）
        case bypassPush = 2
    }
    
    enum EntryType {
        case asCreator
        case asAttend
    }
    
    struct Config {
        let appId: String
        let roomName: String
        let entryType: EntryType
        let roomId: String
        let createdTime: TimeInterval
        let mode: Mode
    }
}
