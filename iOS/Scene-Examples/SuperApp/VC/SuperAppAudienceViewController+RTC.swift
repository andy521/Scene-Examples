//
//  SuperAppAudienceViewController+RTC.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import UIKit
import AgoraRtcKit

extension SuperAppAudienceViewController {
    var videoSize: CGSize { .init(width: 640, height: 360) }
    
    func initMediaPlayer() {
        let rtcConfig = AgoraRtcEngineConfig()
        rtcConfig.appId = config.appId
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcConfig,
                                                  delegate: self)
        mediaPlayer = agoraKit.createMediaPlayer(with: self)
        mediaPlayer.setView(mainView.renderViewLocal)
        mediaPlayer.setRenderMode(.hidden)
        mediaPlayer.open(withAgoraCDNSrc: pullUrlString,
                         startPos: 0)
        LogUtils.logInfo(message: pullUrlString, tag: defaultLogTag)
    }
    
    func joinRtc() {
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
        
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config,
                                                  delegate: self)
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setClientRole(.broadcaster)
        
        let videoConfig = AgoraVideoEncoderConfiguration(size: videoSize,
                                                         frameRate: .fps15,
                                                         bitrate: 700,
                                                         orientationMode: .fixedPortrait,
                                                         mirrorMode: .auto)
        agoraKit.enableVideo()
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        setupLocalVideo(view: mainView.renderViewLocal)
        
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        
        let ret = agoraKit.joinChannel(byToken: nil,
                                       channelId: self.config.sceneId,
                                       info: nil,
                                       uid: 0,
                                       joinSuccess: nil)
        if ret != 0 {
            Log.errorText(text: "joinRtcByPush error \(ret)",
                          tag: defaultLogTag)
            return
        }
    }
    
    func leaveRtc() { /** 离开RTC方式 **/
        agoraKit.leaveChannel(nil)
    }
    
    func setupLocalVideo(view: UIView) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = 0
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupLocalVideo(videoCanvas)
        agoraKit.startPreview()
    }
    
    func setupRemoteVideo(view: UIView, uid: UInt) {
        view.backgroundColor = .gray
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideo(videoCanvas)
    }
    
    func destroyRtc() {
        if mode == .pull {
            mediaPlayer.stop()
            agoraKit.destroyMediaPlayer(mediaPlayer)
            mediaPlayer = nil
        }
        else {
            agoraKit.leaveChannel(nil)
        }
    }
}

// MARK: - AgoraRtcEngineDelegate
extension SuperAppAudienceViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinChannel channel: String,
                   withUid uid: UInt,
                   elapsed: Int) {
        LogUtils.logInfo(message: "didJoinChannel channel: \(channel), uid: \(uid) ", tag: defaultLogTag)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        LogUtils.logInfo(message: "didJoinedOfUid", tag: defaultLogTag)
        mainView.setRemoteViewHidden(hidden: false)
        setupRemoteVideo(view: mainView.renderViewRemote,
                         uid: uid)
    }
}

// MARK: - AgoraRtcMediaPlayerDelegate
extension SuperAppAudienceViewController: AgoraRtcMediaPlayerDelegate {
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol,
                             didChangedTo state: AgoraMediaPlayerState,
                             error: AgoraMediaPlayerError) {
        LogUtils.logInfo(message: "agoraRtcMediaPlayer didChangedTo \(state.rawValue) \(error.rawValue)", tag: defaultLogTag)
        if state == .openCompleted {
            LogUtils.logInfo(message: "openCompleted", tag: "MainVMAudience")
            mediaPlayer.play()
        }
    }
}
