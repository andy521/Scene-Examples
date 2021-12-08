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
    func mainVMShouldStartRenderRemoteView(_ vm: MainVMProtocol)
    func mainVMShouldStopRenderRemoteView(_ vm: MainVMProtocol)
}

protocol MainVMProtocol {
    var delegate: MainVMDelegate? { get set }
    /// 开始运行
    func start()
    /// 邀请连麦
    func invite(userIdPK: String)
    func getSceneRef() -> SceneReference
    /// 取消连麦
    func cancleConnect()
    func close()
    func switchCamera()
    func muteLocalAudio(mute: Bool)
}

