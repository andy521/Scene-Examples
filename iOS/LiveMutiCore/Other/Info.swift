//
//  Info.swift
//  LivePK
//
//  Created by ZYP on 2021/9/23.
//

import Foundation

struct RenderInfo {
    let isLocal: Bool
    let uid: UInt
    
    static var empty: RenderInfo {
        return RenderInfo(isLocal: false, uid: 0)
    }
}


