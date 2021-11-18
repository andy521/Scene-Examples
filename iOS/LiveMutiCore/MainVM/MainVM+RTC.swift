//
//  MainVM+RTC.swift
//  LivePK
//
//  Created by ZYP on 2021/9/26.
//

import Foundation
import AgoraRtcKit

extension MainVM {
    func joinChannel(channelName: String,
                     shouldLeaveChannel: Bool,
                     isAudience: Bool,
                     uid: UInt = 0) {
        
        if shouldLeaveChannel {
            agoraKit.leaveChannel { [weak self](stats) in
                Log.info(text: "leaveChannel")
                self?.join(channelName: channelName,
                           shouldLeaveChannel: shouldLeaveChannel,
                           isAudience: isAudience,
                           uid: uid)
            }
        }
        else {
            join(channelName: channelName,
                       shouldLeaveChannel: shouldLeaveChannel,
                       isAudience: isAudience,
                       uid: uid)
        }
    }
    
    private func join(channelName: String,
                     shouldLeaveChannel: Bool,
                     isAudience: Bool,
                     uid: UInt = 0) {
        let config = AgoraRtcEngineConfig()
        config.appId = appId
        config.areaCode = AgoraAreaCode.GLOB.rawValue
        
        let logConfig = AgoraLogConfig()
        logConfig.level = .info
        config.logConfig = logConfig
        
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        agoraKit.enableVideo()
        let videoEncodeConfig = AgoraVideoEncoderConfiguration(size: CGSize(width: 190, height: 190),
                                                               frameRate: .fps15,
                                                               bitrate: AgoraVideoBitrateStandard,
                                                               orientationMode: .fixedPortrait)
        agoraKit.setVideoEncoderConfiguration(videoEncodeConfig)
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setAudioProfile(.speechStandard, scenario:.chatRoomEntertainment)
        agoraKit.enableDualStreamMode(false)
        
        
        let mediaOptions = AgoraRtcChannelMediaOptions()
        mediaOptions.autoSubscribeAudio = true
        mediaOptions.autoSubscribeVideo = true
        mediaOptions.publishLocalAudio = true
        mediaOptions.publishLocalVideo = true
        
        agoraKit.setClientRole(isAudience ? .audience : .broadcaster)
        let result = agoraKit.joinChannel(byToken: nil, channelId: channelName, info: nil, uid: uid, options: mediaOptions)
        if result != 0 {
            let text = "joinChannel error: \(result)"
            Log.errorText(text: text, tag: "joinChannel")
            invokeShouldShowTips(tips: text)
            return
        }
        Log.info(text: "joinChannel success", tag: "joinChannel")
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
        videoCanvas.channelId = roomName
        agoraKit.setupRemoteVideo(videoCanvas)
    }
}
