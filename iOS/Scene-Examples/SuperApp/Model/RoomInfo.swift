//
//  RoomItem.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/29.
//

import Foundation

struct RoomInfoWapper: Codable {
    let roomInfo: String
    
    var roomInfoObj: RoomInfo? {
        let decoder = JSONDecoder()
        guard let data = roomInfo.data(using: .utf8) else {
            return nil
        }
        do {
            let result = try decoder.decode(RoomInfo.self, from: data)
            return result
        } catch let error {
            LogUtils.logError(message: error.localizedDescription, tag: "RoomInfoWapper.roomInfoObj")
            return nil
        }
    }
}

struct RoomInfo: Codable {
    let id: String
    let roomName: String
    let userId: String
    let liveMode: LiveMode
    
    var jsonString: String {
        let encoder = JSONEncoder()
        let data = try! encoder.encode(self)
        return String(data: data, encoding: .utf8)!
    }
    
    static func create(jsonString: String) -> RoomInfo? {
        let decoder = JSONDecoder()
        guard let data = jsonString.data(using: .utf8) else {
            return nil
        }
        
        do {
            let item = try decoder.decode(RoomInfo.self, from: data)
            return item
        } catch let error {
            LogUtils.logError(message: error.localizedDescription)
            return nil
        }
    }
}

enum LiveMode: Int, Codable {
    case push = 1
    case byPassPush = 2
}
