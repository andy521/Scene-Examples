//
//  MainVMAudience+RTC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit

extension MainVMAudience {
    func initMediaPlayer() {
        let rtcConfig = AgoraRtcEngineConfig()
        rtcConfig.appId = config.appId
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcConfig,
                                                  delegate: self)
        mediaPlayer = agoraKit.createMediaPlayer(with: self)
        if let localView = delegate?.mainVMShouldGetLocalRender(self) {
            Log.info(text: pullUrlString)
            mediaPlayer.setView(localView)
            mediaPlayer.setRenderMode(.hidden)
            mediaPlayer.open(withAgoraCDNSrc: pullUrlString,
                             startPos: 0)
        }
    }
    
    func joinRtc() {
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
        let videoConfig = AgoraVideoEncoderConfiguration(size: videoSize,
                                                         frameRate: .fps15,
                                                         bitrate: 700,
                                                         orientationMode: .fixedPortrait,
                                                         mirrorMode: .auto)
        
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config,
                                                  delegate: self)
        agoraKit.enableVideo()
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setClientRole(.broadcaster)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        let ret = agoraKit.joinChannel(byToken: nil,
                                       channelId: self.config.roomId,
                                       info: nil,
                                       uid: 0,
                                       joinSuccess: nil)
        if ret != 0 {
            Log.errorText(text: "joinRtcByPush error \(ret)",
                          tag: "MainVM")
            return
        }
        else {
            Log.info(text: "did joinChannel", tag: "MainVM")
        }
        
        if let localView = delegate?.mainVMShouldGetLocalRender(self) {
            subscribeVideoLocal(view: localView)
        }
    }
    
    func leaveRtc() { /** 离开RTC方式 **/
        agoraKit.leaveChannel(nil)
    }
    
    private func subscribeVideoLocal(view: UIView) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = 0
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupLocalVideo(videoCanvas)
        agoraKit.startPreview()
    }
    
    func subscribeVideoRemote(view: UIView, uid: UInt) {
        view.backgroundColor = .gray
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideo(videoCanvas)
    }
}
