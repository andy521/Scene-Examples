//
//  MainVM+Broadcaster.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import AgoraRtcKit

extension MainVM {
    func joinRtcDirect() {
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
