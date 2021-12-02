//
//  MainVMHost+RTC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import AgoraRtcKit

extension MainVMHost {
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
        
        let channelId = self.config.roomId
        let ret = agoraKit.joinChannel(byToken: nil,
                                       channelId: channelId,
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
        agoraKit.removePublishStreamUrl(pushUrlString)
        agoraKit.leaveChannel(nil)
    }
    
    func joinRtcByPush() { /** 直推方式加入 **/
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
        agoraKit.setDirectCdnStreamingAudioProfile(.default)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        agoraKit.setDirectCdnStreamingVideoConfiguration(videoConfig)
        let options = AgoraDirectCdnStreamingMediaOptions()
        options.publishCameraTrack = .of(true)
        options.publishMicrophoneTrack = .of(true)
        agoraKit.startDirectCdnStreaming(self,
                                         publishUrl: pushUrlString,
                                         mediaOptions: options)
        if let localView = delegate?.mainVMShouldGetLocalRender(self) {
            subscribeVideoLocal(view: localView)
        }
    }
    
    func leaveRtcByPush() { /** 离开直推方式 **/
        Log.debug(text: "leaveRtcByPush", tag: "MainVM")
        agoraKit.stopDirectCdnStreaming()
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
        agoraKit.setupRemoteVideo(videoCanvas)
    }
}
