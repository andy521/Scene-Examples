//
//  SuperAppHostViewController+RTC.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import AgoraRtcKit

extension SuperAppHostViewController {
    var videoSize: CGSize { .init(width: 640, height: 360) }
    
    func joinRtcByPassPush() { /** 旁推方式加入 **/
        Log.info(text: "旁推方式加入", tag: "MainVMHost")
        
        let channelId = self.config.sceneId
        
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
        
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config,
                                                  delegate: self)
        agoraKit.enableVideo()
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setClientRole(.broadcaster)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        
        let videoConfig = AgoraVideoEncoderConfiguration(size: videoSize,
                                                         frameRate: .fps15,
                                                         bitrate: 700,
                                                         orientationMode: .fixedPortrait,
                                                         mirrorMode: .auto)
        agoraKit.setVideoEncoderConfiguration(videoConfig)

        
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
        subscribeVideoLocal(view: mainView.renderViewLocal)
    }
    
    func leaveRtcByPassPush() { /** 离开旁推方式 **/
        agoraKit.removePublishStreamUrl(pushUrlString)
    }
    
    func joinRtcByPush() { /** 直推方式加入 **/
        Log.info(text: "直推方式加入", tag: "MainVMHost")
        let config = AgoraRtcEngineConfig()
        config.appId = config.appId
        
        let videoConfig = AgoraVideoEncoderConfiguration(size: videoSize,
                                                         frameRate: .fps15,
                                                         bitrate: 700,
                                                         orientationMode: .fixedPortrait,
                                                         mirrorMode: .disabled)
        
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config,
                                                  delegate: self)
        
        agoraKit.enableVideo()
        agoraKit.enableAudio()
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setClientRole(.broadcaster)
        agoraKit.setDirectCdnStreamingAudioProfile(.default)
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        agoraKit.setVideoEncoderConfiguration(videoConfig)
        agoraKit.setDirectCdnStreamingVideoConfiguration(videoConfig)
        agoraKit.startPreview()
        let options = AgoraDirectCdnStreamingMediaOptions()
        options.publishCameraTrack = .of(true)
        options.publishMicrophoneTrack = .of(true)
        agoraKit.startDirectCdnStreaming(self,
                                         publishUrl: pushUrlString,
                                         mediaOptions: options)
        subscribeVideoLocal(view: mainView.renderViewLocal)
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
        view.backgroundColor = .gray
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        agoraKit.setupRemoteVideo(videoCanvas)
    }
    
    func setMergeVideoLocal(engine: AgoraRtcEngineKit, uid: UInt) { /** 设置旁路推流合图（本地） **/
        Log.info(text: "设置旁路推流合图（本地）", tag: "MainVMHost")
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
        Log.info(text: "旁路合图设置(远程)", tag: "MainVMHost")
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
}

extension SuperAppHostViewController: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinChannel channel: String,
                   withUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinChannel", tag: "MainVMHost")
        setMergeVideoLocal(engine: engine, uid: uid)
        engine.addPublishStreamUrl(pushUrlString,
                                   transcodingEnabled: true)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit,
                   didJoinedOfUid uid: UInt,
                   elapsed: Int) {
        Log.info(text: "didJoinChannel", tag: "MainVMHost")
        
        setMergeVideoRemote(engine: engine, uid: uid)
        startRenderRemoteView()
        subscribeVideoRemote(view: mainView.renderViewRemote,
                             uid: uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, streamUnpublishedWithUrl url: String) {
        Log.info(text: "streamUnpublishedWithUrl", tag: "MainVMHost")
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

extension SuperAppHostViewController: AgoraDirectCdnStreamingEventDelegate {
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
