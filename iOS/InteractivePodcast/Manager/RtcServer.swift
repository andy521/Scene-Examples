//
//  RtcServer.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/7.
//

import AgoraRtcKit
import Core
import Foundation
import RxRelay
import RxSwift

enum RtcServerStateType {
    case join
    case error
    case members
}

class RtcServer: NSObject {
    var rtcEngine: AgoraRtcEngineKit?
    private let statePublisher: PublishRelay<Result<RtcServerStateType>> = PublishRelay()

    var uid: UInt = 0
    var isManager: Bool = false
    var channel: String?
    var members: [UInt] = []
    var speakers = [UInt: Bool]()
    var role: AgoraClientRole?
    var audienceLatencyLevel: AgoraAudienceLatencyLevelType?
    var muted: Bool = false

    var isJoinChannel: Bool {
        return channel != nil && channel?.isEmpty == false
    }

    override init() {
        super.init()
        let config = AgoraRtcEngineConfig()
        config.appId = BuildConfig.AppId
        #if LEANCLOUD
            config.areaCode = AgoraAreaCode.CN.rawValue
        #endif
        #if FIREBASE
            config.areaCode = AgoraAreaCode.GLOB.rawValue
        #endif
        rtcEngine = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        if let engine = rtcEngine {
            engine.setChannelProfile(.liveBroadcasting)
            engine.setAudioProfile(.musicHighQualityStereo, scenario: .chatRoomEntertainment)
            engine.enableAudioVolumeIndication(500, smooth: 3, report_vad: false)
        }
    }

    func setClientRole(_ role: AgoraClientRole, _ audienceLatencyLevel: Bool) {
        Logger.log(message: "rtc setClientRole \(role.rawValue)", level: .info)
        let _audienceLatencyLevel: AgoraAudienceLatencyLevelType = audienceLatencyLevel ? .lowLatency : .ultraLowLatency
        if self.role == role, self.audienceLatencyLevel == _audienceLatencyLevel {
            return
        }
        self.role = role
        self.audienceLatencyLevel = _audienceLatencyLevel
        guard let rtc = rtcEngine else {
            return
        }
        let option = AgoraClientRoleOptions()
        option.audienceLatencyLevel = _audienceLatencyLevel
        Logger.log(message: "setClientRole audienceLatencyLevel: \(_audienceLatencyLevel.rawValue)", level: .info)
        rtc.setClientRole(role, options: option)
    }

    func joinChannel(member: PodcastMember, channel: String, setting: LocalSetting) -> Observable<Result<Void>> {
        guard let rtc = rtcEngine else {
            return Observable.just(Result(success: false, message: "rtcEngine is nil!"))
        }
        role = nil
        audienceLatencyLevel = nil

        members.removeAll()
        isManager = member.isManager
        if member.isSpeaker {
            setClientRole(.broadcaster, setting.audienceLatency)
        } else {
            setClientRole(.audience, setting.audienceLatency)
        }
        muteLocalMicrophone(mute: member.isSelfMuted)
        return Single.create { single in
            let code = rtc.joinChannel(byToken: BuildConfig.Token, channelId: channel, info: nil, uid: 0, options: AgoraRtcChannelMediaOptions())
            single(.success(code))
            return Disposables.create()
        }.asObservable().subscribe(on: MainScheduler.instance)
            .concatMap { (code: Int32) -> Observable<Result<Void>> in
                if code != 0 {
                    return Observable.just(Result(success: false, message: RtcServer.toErrorString(type: .join, code: code)))
                } else {
                    return self.statePublisher.filter { state -> Bool in
                        state.data == RtcServerStateType.join || state.data == RtcServerStateType.error
                    }.take(1).map { state -> Result<Void> in
                        Result(success: state.success, message: state.message)
                    }
                }
            }
    }

    func leaveChannel() -> Observable<Result<Void>> {
        return Single.create { [unowned self] single in
            if isJoinChannel {
                if let rtc = self.rtcEngine {
                    self.channel = nil
                    self.uid = 0
                    self.members.removeAll()
                    self.statePublisher.accept(Result(success: true, data: RtcServerStateType.members))
                    Logger.log(message: "rtc leaveChannel", level: .info)
                    let code = rtc.leaveChannel { _ in
                        single(.success(Result(success: true)))
                    }
                    if code != 0 {
                        single(.success(Result(success: false, message: "rtcEngine is nil!")))
                    }
                } else {
                    single(.success(Result(success: false, message: "rtcEngine is nil!")))
                }
            } else {
                single(.success(Result(success: true)))
            }

            return Disposables.create()
        }.asObservable()
    }

    func onSpeakersChanged() -> Observable<[UInt: Bool]> {
        return statePublisher
            .filter { state -> Bool in
                state.data == RtcServerStateType.members
            }
            .startWith(Result(success: true, data: RtcServerStateType.members))
            .map { [unowned self] _ in
                var speakers = [UInt: Bool]()
                self.members.forEach { member in
                    speakers[member] = self.speakers[member] ?? true
                }
                return speakers
            }
    }

    func muteLocalMicrophone(mute: Bool) {
        Logger.log(message: "rtc muteLocalMicrophone: \(mute)", level: .info)
        muted = mute
        rtcEngine?.muteLocalAudioStream(mute)
    }
}

extension RtcServer: AgoraRtcEngineDelegate {
    func rtcEngine(_: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        Logger.log(message: "didOccurError \(AgoraRtcEngineKit.getErrorDescription(errorCode.rawValue) ?? "\(errorCode)")", level: .info)
        statePublisher.accept(Result(success: false, data: RtcServerStateType.error, message: AgoraRtcEngineKit.getErrorDescription(errorCode.rawValue)))
    }

    func rtcEngine(_: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed _: Int) {
        Logger.log(message: "didJoinChannel:\(channel) uid:\(uid)", level: .info)
        self.uid = uid
        self.channel = channel
        members.append(uid)
        speakers[uid] = role == .audience
        statePublisher.accept(Result(success: true, data: RtcServerStateType.join))
    }

    func rtcEngine(_: AgoraRtcEngineKit, didLeaveChannelWith stats: AgoraChannelStats) {
        Logger.log(message: "didLeaveChannelWith:\(stats)", level: .info)
    }

    func rtcEngine(_: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed _: Int) {
        Logger.log(message: "didJoinedOfUid uid:\(uid)", level: .info)
        members.append(uid)
        speakers[uid] = true
        statePublisher.accept(Result(success: true, data: RtcServerStateType.members))
    }

    func rtcEngine(_: AgoraRtcEngineKit, didAudioMuted muted: Bool, byUid uid: UInt) {
        Logger.log(message: "didAudioMuted uid:\(uid) muted:\(muted)", level: .info)
        speakers[uid] = muted
        statePublisher.accept(Result(success: true, data: RtcServerStateType.members))
    }

    func rtcEngine(_: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason _: AgoraUserOfflineReason) {
        Logger.log(message: "didOfflineOfUid uid:\(uid)", level: .info)
        if let index = members.firstIndex(of: uid) {
            members.remove(at: index)
        }
        speakers[uid] = false
        statePublisher.accept(Result(success: true, data: RtcServerStateType.members))
    }

    func rtcEngine(_: AgoraRtcEngineKit, reportAudioVolumeIndicationOfSpeakers speakers: [AgoraRtcAudioVolumeInfo], totalVolume _: Int) {
        speakers.forEach { speaker in
            if speaker.volume > 0 {
                // Logger.log(message: "reportAudioVolumeIndicationOfSpeakers \(speaker.uid)", level: .info)
            }
        }
    }
}

enum RtcServerError: Int {
    case join = 0
    case register = 1
    case leave = 2
}

extension RtcServer: ErrorDescription {
    static func toErrorString(type: RtcServerError, code: Int32) -> String {
        switch type {
        case RtcServerError.join:
            switch code {
            case -2:
                return "Invalid Argument".localized
            case -3:
                return "SDK Not Ready".localized
            case -5:
                return "SDK Refused".localized
            case -7:
                return "SDK Not Initialized".localized
            default:
                return "Unknown Error".localized
            }
        case RtcServerError.register:
            return "Unknown Error".localized
        case RtcServerError.leave:
            switch code {
            case -2:
                return "Invalid Argument".localized
            case -7:
                return "SDK Not Initialized".localized
            default:
                return "Unknown Error".localized
            }
        }
    }
}
