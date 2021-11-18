//
//  MainVM+Info.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import Foundation

extension MainVM {
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
    }
}
