//
//  MainVM+RTC.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation
import AgoraRtcKit
import CommonCrypto

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
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        agoraKit.setParameters("{\"rtc.video.apas_aa_harq_enable\":true}");
        agoraKit.setParameters("{\"rtc.audio.opensl.mode\": 0}");
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
        mediaOptions.clientRoleType = .of(1)
        mediaOptions.autoSubscribeAudio = .of(true)
        mediaOptions.autoSubscribeVideo = .of(true)
        mediaOptions.publishAudioTrack = .of(true)
        mediaOptions.publishCameraTrack = .of(true)
        
        let result =  agoraKit.joinChannel(byToken: nil,
                                           channelId: channel.channelId,
                                           uid: 0,
                                           mediaOptions: mediaOptions,
                                           joinSuccess: nil)
        
        if result != 0 {
            let text = "launchLocalChannel error: \(result)"
            Log.errorText(text: text, tag: "joinRtcChannelLocal")
            invokeShouldShowTips(tips: text)
            return
        }
        
        Log.info(text: "joinRtcChannelLocal success \(channelName)",
                 tag: "joinRtcChannelLocal")
    }
    
    /// 加入远程的频道
    func joinRtcChannelRemote(channelName: String) {
        guard channelRemote == nil else {
            return
        }
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.clientRoleType = .of(2)
        mediaOptions.autoSubscribeAudio = .of(true)
        mediaOptions.autoSubscribeVideo = .of(true)
        mediaOptions.publishAudioTrack = .of(false)
        mediaOptions.publishCameraTrack = .of(false)
        mediaOptions.isInteractiveAudience = .of(true)
        
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
        agoraKit.setParameters("{\"rtc.video.apas_aa_harq_enable\":true}");
        agoraKit.setParameters("{\"rtc.audio.opensl.mode\": 0}");
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
    
    func createTxSecret(time: String, channelName: String) -> String {
        ("hsP2t2CcM5WfpkJQSaJN" + channelName + time).md5
    }
}


extension String {
    /* ################################################################## */
    /**
     - returns: the String, as an MD5 hash.
     */
    var md5: String {
        let str = self.cString(using: String.Encoding.utf8)
        let strLen = CUnsignedInt(self.lengthOfBytes(using: String.Encoding.utf8))
        let digestLen = 16
        let result = UnsafeMutablePointer<CUnsignedChar>.allocate(capacity: digestLen)
        CC_MD5(str!, strLen, result)
        
        let hash = NSMutableString()
        
        for i in 0..<digestLen {
            hash.appendFormat("%02x", result[i])
        }
        
        result.deallocate()
        return hash as String
    }
}
