//
//  SuperAppPlayerViewControllerHost+RTC.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import AgoraRtcKit

extension SuperAppPlayerViewControllerHost {
    var videoSize: CGSize { .init(width: 640, height: 360) }
    
    func joinRtcByPassPush() { /** 旁推方式加入 **/
        LogUtils.logInfo(message: "旁推方式加入", tag: defaultLogTag)
        
        let channelId = self.config.sceneId
        
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
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        agoraKit.enableVideo()
        setupLocalVideo(view: mainView.renderViewLocal)
        
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)

        let ret = agoraKit.joinChannel(byToken: nil,
                                       channelId: channelId,
                                       info: nil,
                                       uid: 0,
                                       joinSuccess: nil)
        
        if ret != 0 {
            LogUtils.logError(message: "joinRtcByPush error \(ret)",
                              tag: defaultLogTag)
        }
    }
    
    func leaveRtcByPassPush() { /** 离开旁推方式 **/
        agoraKit.removePublishStreamUrl(pushUrlString)
    }
    
    func joinRtcByPush() { /** 直推方式加入 **/
        LogUtils.logInfo(message: "直推方式加入", tag: defaultLogTag)
        
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
                                                         mirrorMode: .disabled)
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        agoraKit.enableVideo()
        setupLocalVideo(view: mainView.renderViewLocal)
        
        agoraKit.setDirectCdnStreamingAudioProfile(.default)
        agoraKit.setDirectCdnStreamingVideoConfiguration(videoConfig)
        let options = AgoraDirectCdnStreamingMediaOptions()
        options.publishCameraTrack = .of(true)
        options.publishMicrophoneTrack = .of(true)
        let ret = agoraKit.startDirectCdnStreaming(self,
                                         publishUrl: pushUrlString,
                                         mediaOptions: options)
        
        agoraKit.enableAudio()
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        
        if ret != 0 {
            LogUtils.logError(message: "joinRtcByPush error \(ret)",
                          tag: defaultLogTag)
        }
    }
    
    func leaveRtcByPush() { /** 离开直推方式 **/
        LogUtils.logInfo(message: "leaveRtcByPush", tag: defaultLogTag)
        agoraKit.stopDirectCdnStreaming()
    }
    
    private func setupLocalVideo(view: UIView) {
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
    
    func switchCamera() {
        agoraKit.switchCamera()
    }
    
    func muteLocalAudio(mute: Bool) {
        agoraKit.muteLocalAudioStream(audioIsMute)
        agoraKit.adjustRecordingSignalVolume(audioIsMute ? 0 : 100)
    }
    
    func setMergeVideoLocal(engine: AgoraRtcEngineKit, uid: UInt) { /** 设置旁路推流合图（本地） **/
        LogUtils.logInfo(message: "设置旁路推流合图（本地）", tag: defaultLogTag)
        let user = AgoraLiveTranscodingUser()
        user.rect = CGRect(x: 0,
                           y: 0,
                           width: videoSize.height,
                           height: videoSize.width)
        user.uid = uid
        user.zOrder = 1
        liveTranscoding.size = CGSize(width: videoSize.height, height: videoSize.width)
        liveTranscoding.videoFramerate = 15
        liveTranscoding.add(user)
        engine.setLiveTranscoding(liveTranscoding)
    }
    
    func setMergeVideoRemote(engine: AgoraRtcEngineKit, uid: UInt) { /** 设置旁路推流合图（远程） **/
        LogUtils.logInfo(message: "旁路合图设置(远程)", tag: defaultLogTag)
        let user = AgoraLiveTranscodingUser()
        user.rect = CGRect(x: 0.5 * videoSize.height,
                           y: 0.1 * videoSize.width,
                           width: 0.5 * videoSize.height,
                           height: 0.5 * videoSize.width)
        user.uid = uid
        user.zOrder = 2
        liveTranscoding.add(user)
        engine.setLiveTranscoding(liveTranscoding)
    }
    
    func destroyRtc() {
        agoraKit.delegate = nil
        
        if mode == .push {
            leaveRtcByPush()
        }
        else {
            leaveRtcByPassPush()
        }
        agoraKit = nil
        AgoraRtcEngineKit.destroy()
    }
}

extension SuperAppPlayerViewControllerHost: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinChannel channel: String,
                   withUid uid: UInt,
                   elapsed: Int) {
        LogUtils.logInfo(message: "didJoinChannel", tag: defaultLogTag)
        setMergeVideoLocal(engine: engine, uid: uid)
        engine.addPublishStreamUrl(pushUrlString,
                                   transcodingEnabled: true)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        LogUtils.logInfo(message: "didJoinChannel", tag: defaultLogTag)
        
        setMergeVideoRemote(engine: engine, uid: uid)
        mainView.setRemoteViewHidden(hidden: false)
        setupRemoteVideo(view: mainView.renderViewRemote,
                             uid: uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, streamUnpublishedWithUrl url: String) {
        LogUtils.logInfo(message: "streamUnpublishedWithUrl", tag: defaultLogTag)
        let option = AgoraLeaveChannelOptions()
        option.stopMicrophoneRecording = false
        agoraKit.leaveChannel(option)

        if mode == .push {
            DispatchQueue.main.async { [weak self] in
                self?.joinRtcByPush()
            }
        }
    }
}

extension SuperAppPlayerViewControllerHost: AgoraDirectCdnStreamingEventDelegate {
    func onDirectCdnStreamingStateChanged(_ state: AgoraDirectCdnStreamingState,
                                          error: AgoraDirectCdnStreamingError,
                                          message: String?) {
        if state == .stopped {
            if mode == .byPassPush {
                DispatchQueue.main.async { [weak self] in
                    self?.joinRtcByPassPush()
                }
            }
        }
    }
}
