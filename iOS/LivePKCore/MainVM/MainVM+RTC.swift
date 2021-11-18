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
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.areaCode = AgoraAreaCode.GLOB.rawValue

        let logConfig = AgoraLogConfig()
        logConfig.level = .info
        config.logConfig = logConfig
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        agoraKit.enableVideo()
        agoraKit.setVideoEncoderConfiguration(AgoraVideoEncoderConfiguration(size: CGSize(width: 360, height: 640),
                                                                             frameRate: .fps15,
                                                                             bitrate: AgoraVideoBitrateStandard,
                                                                             orientationMode: .fixedPortrait))
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setAudioProfile(.speechStandard, scenario:.chatRoomEntertainment)
        agoraKit.enableDualStreamMode(false)
        agoraKit.setParameters("{\"che.video.retransDetectEnable\":true}")
        agoraKit.setParameters("{\"che.video.captureFpsLowPower\":true}")
        agoraKit.setParameters("{\"che.video.setQuickVideoHighFec\":true}")
        agoraKit.setParameters("{\"rtc.enable_quick_rexfer_keyframe\":true}")
        agoraKit.setParameters("{\"rtc.enable_audio_rsfec_in_video\":true}")
        agoraKit.setParameters("{\"che.audio.specify.codec\":\"OPUSFB\"}")
        agoraKit.setParameters("{\"che.video.default_encode_complexity\":\"0x403\"}")
        agoraKit.setParameters("{\"che.video.max_slices\":4}")
        agoraKit.setParameters("{\"rtc.dual_signaling_mode\":2}")
        agoraKit.setParameters("{\"rtc.work_manager_account_list\":[\"mix-worker-182.18.83.206-30002\"]}")
        agoraKit.setParameters("{\"rtc.work_manager_addr_list\":[\"182.18.83.206:30002\"]}")
        agoraKit.setParameters("{\"rtc.enable_crypto_access\":false}")
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishLocalAudio = true
        mediaOptions.publishLocalVideo = true
        
        channelLocal = agoraKit.createRtcChannel(channelName)
        channelLocal?.setClientRole(.broadcaster)
        channelLocal?.setRtcChannelDelegate(self)
        let result = channelLocal?.join(byToken: nil, info: nil, uid: 0, options: mediaOptions) ?? -1
        if result != 0 {
            let text = "launchLocalChannel error: \(result)"
            Log.errorText(text: text, tag: "joinRtcChannelLocal")
            invokeShouldShowTips(tips: text)
            return
        }
        Log.info(text: "launchLocalChannel success", tag: "joinRtcChannelLocal")
    }
    
    /// 加入远程的频道
    func joinRtcChannelRemote(channelName: String) {
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishLocalAudio = false
        mediaOptions.publishLocalVideo = false
        
        channelRemote = agoraKit.createRtcChannel(channelName)
        channelRemote?.setClientRole(.broadcaster)
        channelRemote?.setRtcChannelDelegate(self)
        let result = channelRemote?.join(byToken: nil, info: nil, uid: 0, options: mediaOptions) ?? -1
        if result != 0 {
            let text = "joinRtcChannelRemote error: \(result)"
            Log.errorText(text: text)
            invokeShouldShowTips(tips: text)
            return
        }
        Log.info(text: "joinRtcChannelRemote success \(channelName)", tag: "joinRtcChannelRemote")
    }
    
    func switchCamera() {
        agoraKit.switchCamera()
    }
}
