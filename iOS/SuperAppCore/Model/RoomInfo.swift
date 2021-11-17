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
    let userIdPK: String
    let userCount: Int
    let liveMode: Int
}
