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
    
    var defaultNameList: [String] {
        ["陌上花开等你来", "天天爱你", "我爱你们",
                "有人可以", "风情万种", "强势归来",
                "哈哈哈", "聊聊", "美人舞江山",
                "最美的回忆", "遇见你", "最长情的告白",
                "全力以赴", "简单点", "早上好",
                "春风十里不如你"]
    }
    
    init(appId: String) {
        self.appId = appId
        self.rtcKit = AgoraRtcEngineKit.sharedEngine(withAppId: appId,
                                                     delegate: nil)
        rtcKit.enableVideo()
        super.init()
    }
    
    func genRandomName() {
        let text = defaultNameList.randomElement() ?? defaultNameList.first!
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
}
