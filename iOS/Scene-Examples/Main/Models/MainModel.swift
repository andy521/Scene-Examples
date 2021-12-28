//
//  MainModel.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit

enum SceneType: String {
    /// 单直播
    case singleLive = "SignleLive"
    case superApp = "superApp"
    
    var alertTitle: String {
        switch self {
        default: return ""
        }
    }
}

struct MainModel {
    var title: String = ""
    var desc: String = ""
    var imageNmae: String = ""
    var sceneType: SceneType = .singleLive
    
    static func mainDatas() -> [MainModel] {
        var dataArray = [MainModel]()
        var model = MainModel()
        model.title = "单主播直播"
        model.desc = "单主播直播"
        model.imageNmae = "pic-single"
        model.sceneType = .singleLive
        dataArray.append(model)
        
        model = MainModel()
        model.title = "Super App"
        model.desc = "融合CDN"
        model.imageNmae = "pic-multiple"
        model.sceneType = .superApp
        dataArray.append(model)
        
        return dataArray
    }
    
    static func sceneId(type: SceneType) -> String {
        type.rawValue
    }
}
