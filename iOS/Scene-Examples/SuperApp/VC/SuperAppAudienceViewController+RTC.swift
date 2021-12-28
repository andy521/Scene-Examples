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
        Log.info(text: pullUrlString)
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
                                       channelId: self.config.sceneId,
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
        
        renderVideoLocal(view: mainView.renderViewLocal)
    }
    
    func leaveRtc() { /** 离开RTC方式 **/
        agoraKit.leaveChannel(nil)
    }
    
    func renderVideoLocal(view: UIView) {
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = 0
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupLocalVideo(videoCanvas)
        agoraKit.startPreview()
    }
    
    func renderVideoRemote(view: UIView, uid: UInt) {
        view.backgroundColor = .gray
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideo(videoCanvas)
    }
    
    func closeRtc() {
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
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinedOfUid", tag: "MainVM")
        startRenderRemoteView()
        renderVideoRemote(view: mainView.renderViewRemote,
                          uid: uid)
    }
}

// MARK: - AgoraRtcMediaPlayerDelegate
extension SuperAppAudienceViewController: AgoraRtcMediaPlayerDelegate {
    func agoraRtcMediaPlayer(_ playerKit: AgoraRtcMediaPlayerProtocol,
                             didChangedTo state: AgoraMediaPlayerState,
                             error: AgoraMediaPlayerError) {
        Log.info(text: "agoraRtcMediaPlayer didChangedTo \(state.rawValue) \(error.rawValue)", tag: "MainVMAudience")
        if state == .openCompleted {
            Log.info(text: "openCompleted", tag: "MainVMAudience")
            playerKit.play()
        }
    }
}
