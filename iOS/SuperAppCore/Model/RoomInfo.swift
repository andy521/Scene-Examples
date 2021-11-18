//
//  RoomInfo.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/16.
//

import Foundation

struct RoomInfo: Codable {
    let createTime: TimeInterval
    let expiredTime: TimeInterval
    let roomId: String
    let roomName: String
    /// 当前房间正在pk的用户id
    let userIdPK: String
    let userCount: Int
    ///  直播模式. 1: 直推模式, 2: 旁推模式
    let liveMode: Int
    
    var dict: [String : String] {
        return ["createTime" : "\(createTime)",
                "expiredTime" : "\(expiredTime)",
                "roomId" : roomId,
                "roomName" : roomName,
                "userIdPK" : userIdPK,
                "userCount" : "\(userCount)",
                "liveMode" : "\(liveMode)"]
    }
    
    init(createTime: TimeInterval,
         expiredTime: TimeInterval,
         roomId: String,
         roomName: String,
         userIdPK: String,
         userCount: Int,
         liveMode: Int) {
        self.createTime = createTime
        self.expiredTime = expiredTime
        self.roomId = roomId
        self.roomName = roomName
        self.userIdPK = userIdPK
        self.userCount = userCount
        self.liveMode = liveMode
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.createTime = Double(try container.decode(String.self, forKey: .createTime)) ?? 0
        self.expiredTime = Double(try container.decode(String.self, forKey: .expiredTime)) ?? 0
        self.roomId = try container.decode(String.self, forKey: .roomId)
        self.roomName = try container.decode(String.self, forKey: .roomName)
        self.userIdPK = try container.decode(String.self, forKey: .userIdPK)
        self.userCount = Int(try container.decode(String.self, forKey: .userCount)) ?? 0
        self.liveMode = Int(try container.decode(String.self, forKey: .liveMode)) ?? 0
    }
}

