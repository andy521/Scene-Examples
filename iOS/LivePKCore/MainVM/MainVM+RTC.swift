//
//  MainVM+RTC.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation
import AgoraRtcKit

extension MainVM {
    /// 本地加入自己的频道
    func joinRtcChannelLocal(channelName: String) {
        guard channelLocal == nil else {
            return
        }
        
        let config = AgoraRtcEngineConfig()
        config.appId = appId

        let logConfig = AgoraLogConfig()
        logConfig.level = .info
        config.logConfig = logConfig
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: nil)
        agoraKit.enableVideo()
        
        let channel = AgoraRtcConnection()
        channel.channelId = channelName
        channelLocal = channel
        
        let videoEncodeConfig = AgoraVideoEncoderConfiguration(size: CGSize(width: 360, height: 640),
                                                               frameRate: .fps15,
                                                               bitrate: AgoraVideoBitrateStandard,
                                                               orientationMode: .fixedPortrait,
                                                               mirrorMode: .disabled)
        agoraKit.setVideoEncoderConfigurationEx(videoEncodeConfig,
                                                connection: channel)
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setAudioProfile(.speechStandard, scenario: .chatRoom)
        agoraKit.enableDualStreamMode(false)
        agoraKit.setClientRole(.broadcaster)
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.clientRoleType = AgoraRtcIntOptional.of(1)
        mediaOptions.autoSubscribeAudio = AgoraRtcBoolOptional.of(true)
        mediaOptions.autoSubscribeVideo = AgoraRtcBoolOptional.of(true)
        mediaOptions.publishAudioTrack = AgoraRtcBoolOptional.of(true)
        mediaOptions.publishCameraTrack = AgoraRtcBoolOptional.of(true)
        
        let result =  agoraKit.joinChannel(byToken: nil,
                                           channelId: channel.channelId,
                                           uid: 0,
                                           mediaOptions: mediaOptions,
                                           joinSuccess: { [weak self](_, _, _) in
                                            guard let channelName = self?.loginInfo.roomName else {
                                                return
                                            }
                                            let url = "rtmp://webdemo-push.agora.io/lbhd/\(channelName)"
                                            self?.agoraKit.addPublishStreamUrl(url,
                                                                               transcodingEnabled: false)
                                           })

        if result != 0 {
            let text = "launchLocalChannel error: \(result)"
            Log.errorText(text: text, tag: "joinRtcChannelLocal")
            invokeShouldShowTips(tips: text)
            return
        }
    }
    
    /// 加入远程的频道
    func joinRtcChannelRemote(channelName: String) {
        guard channelRemote == nil else {
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.clientRoleType = AgoraRtcIntOptional.of(1)
        mediaOptions.autoSubscribeAudio = AgoraRtcBoolOptional.of(true)
        mediaOptions.autoSubscribeVideo = AgoraRtcBoolOptional.of(true)
        mediaOptions.publishAudioTrack = AgoraRtcBoolOptional.of(false)
        mediaOptions.publishCameraTrack = AgoraRtcBoolOptional.of(false)
        
        agoraKit.enableVideo()
        
        let channel = AgoraRtcConnection()
        channel.channelId = channelName
        channel.localUid = UInt.random(in: 0...200)
        channelRemote = channel
        
        let remoteHandler = RemoteChannelHandler()
        remoteHandler.delegate = self
        
        let result = agoraKit.joinChannelEx(byToken: nil,
                                            connection: channel,
                                            delegate: remoteHandler,
                                            mediaOptions: mediaOptions,
                                            joinSuccess: nil)
        if result != 0 {
            let text = "joinRtcChannelRemote channel:\(channelName) error: \(result)"
            Log.errorText(text: text)
            invokeShouldShowTips(tips: text)
            return
        }
        Log.info(text: "joinRtcChannelRemote success \(channelName)",
                 tag: "joinRtcChannelRemote")
    }
    
    func switchCamera() {
        agoraKit.switchCamera()
    }
}
