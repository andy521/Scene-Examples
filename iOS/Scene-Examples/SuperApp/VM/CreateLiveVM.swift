//
//  CreateLiveVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/17.
//

import UIKit
import AgoraRtcKit

protocol CreateLiveVMDelegate: NSObjectProtocol {
    func createLiveVM(_ vm: CreateLiveVM, didUpdate roomName: String)
}

class CreateLiveVM: NSObject {
    private let appId: String
    private let rtcKit: AgoraRtcEngineKit
    weak var delegate: CreateLiveVMDelegate?
    
    init(appId: String) {
        self.appId = appId
        self.rtcKit = AgoraRtcEngineKit.sharedEngine(withAppId: appId,
                                                     delegate: nil)
        rtcKit.enableVideo()
        super.init()
    }
    
    func genRandomName() {
        let text: String = .randomRoomName
        delegate?.createLiveVM(self, didUpdate: text)
    }
    
    func startRenderLocalVideoStream(view: UIView) {
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = 0
        canvas.view = view
        rtcKit.setupLocalVideo(canvas)
        rtcKit.startPreview()
    }
    
    func switchCamera() {
        rtcKit.switchCamera()
    }
    
    func destory() {
        
    }
}
