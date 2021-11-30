//
//  MainVM+Broadcaster.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import AgoraRtcKit

extension MainVM {
    func joinRtcByPush() { /** 直推方式加入 **/
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
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
        agoraKit.stopDirectCdnStreaming()
    }
    
    func joinRtcByPassPush() { /** 旁推方式加入 **/
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
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
    
    func subscribeVideoLocal(view: UIView) {
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
