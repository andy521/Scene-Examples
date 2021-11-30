//
//  MainVM+Handle+Audience.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import AgoraRtcKit

extension MainVM {
    func initMediaPlayer(config: Config) {
        let rtcConfig = AgoraRtcEngineConfig()
        rtcConfig.appId = config.appId
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcConfig,
                                                  delegate: self)
        mediaPlayer = agoraKit.createMediaPlayer(with: self)
        if let localView = delegate?.mainVMShouldGetLocalRender(self) {
            Log.info(text: pullUrlString)
            mediaPlayer.setView(localView)
            mediaPlayer.open(withAgoraCDNSrc: pullUrlString,
                             startPos: 0)
        }
    }
}
