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
}
