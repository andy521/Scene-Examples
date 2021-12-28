//
//  MainVMAudience+Info.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation

extension MainVMAudience {
    enum Mode {
        /// 拉流模式
        case pull
        /// rtc模式
        case rtc
    }
    
    struct Config {
        let appId: String
        let roomName: String
        let roomId: String
    }
}
