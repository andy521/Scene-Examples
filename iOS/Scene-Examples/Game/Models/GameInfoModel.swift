//
//  GameInfoModel.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/22.
//

import UIKit

struct GameInfoModel: Codable {
    var objectId: String = ""
    
    var status: GameStatus = .no_start
    
    var gameUid: String = ""
    
    var gameId: GameCenterType = .you_draw_i_guess
}
