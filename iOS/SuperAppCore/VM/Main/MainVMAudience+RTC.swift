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
            mediaPlayer.open(withAgoraCDNSrc: pullUrlString,
                             startPos: 0)
        }
    }
    
    func joinRtcByPassPush() { /** 旁推方式加入 **/
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
        let size = CGSize(width: 640, height: 360)
        let videoConfig = AgoraVideoEncoderConfiguration(size: size,
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
        agoraKit.addPublishStreamUrl(pushUrlString,
                                     transcodingEnabled: true)
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
        
        if let localView = delegate?.mainVMShouldGetLocalRender(self) {
            subscribeVideoLocal(view: localView)
        }
    }
    
    func leaveRtcByPassPush() { /** 离开旁推方式 **/
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
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        // videoCanvas.channelId = channel.getId()
        agoraKit.setupRemoteVideo(videoCanvas)
    }
}
