//
//  UserInfo.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/16.
//

import Foundation

struct UserInfo: Codable {
    let expiredTime: TimeInterval
    let userId: String
    let userName: String
    let roomId: String
    
    var dict: [String : String] {
        return ["expiredTime" : "\(expiredTime)",
                "userId" : userId,
                "userName" : userName,
                "roomId" : roomId]
    }
    
    init(expiredTime: TimeInterval,
         userId: String,
         userName: String,
         roomId: String) {
        self.expiredTime = expiredTime
        self.roomId = roomId
        self.userId = userId
        self.userName = userName
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        self.expiredTime = Double(try container.decode(String.self, forKey: .expiredTime)) ?? 0
        self.userId = try container.decode(String.self, forKey: .userId)
        self.userName = try container.decode(String.self, forKey: .userName)
        self.roomId = try container.decode(String.self, forKey: .roomId)
    }
}
