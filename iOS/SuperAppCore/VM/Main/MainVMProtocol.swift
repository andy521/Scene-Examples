//
//  MainVMProtocol.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import UIKit
import SyncManager

protocol MainVMDelegate: NSObjectProtocol {
    func mainVM(_ vm: MainVMProtocol, didJoinRoom info: RoomInfo)
    func mainVM(_ vm: MainVMProtocol, shouldShow tips: String)
    func mainVMShouldGetLocalRender(_ vm: MainVMProtocol) -> UIView
    func mainVMShouldGetRemoteRender(_ vm: MainVMProtocol) -> UIView
    func mainVMShouldDidStartRenderRemoteView(_ vm: MainVMProtocol) 
}

protocol MainVMProtocol {
    var delegate: MainVMDelegate? { get set }
    func start()
    func invite(userIdPK: String)
    func getSceneRef() -> SceneReference
}

