//
//  RoomManager.swift
//  LiveKtv
//
//  Created by XC on 2021/6/8.
//

import Core
import Foundation
import RxSwift

class RoomManager: NSObject {
    fileprivate static var instance: RoomManager?
    private static var lock = os_unfair_lock()
    static func shared() -> IRoomManager {
        os_unfair_lock_lock(&RoomManager.lock)
        if instance == nil {
            instance = RoomManager()
        }
        os_unfair_lock_unlock(&RoomManager.lock)
        return instance!
    }

    var account: User?
    var member: LiveKtvMember?
    var setting: LocalSetting = AppDataManager.getSetting() ?? LocalSetting()
    var room: LiveKtvRoom?
    private var rtcServer = RtcServer()
    // private var rtmServer = RtmServer()
    private var scheduler = SerialDispatchQueueScheduler(internalSerialQueueName: "rtc")
}

extension RoomManager: IRoomManager {
    func destory() {
        RoomManager.instance = nil
    }

    func updateSetting() {
        if rtcServer.isJoinChannel {
            rtcServer.setClientRole(rtcServer.role!, setting.audienceLatency)
        }
    }

    func getAccount() -> Observable<Result<User>> {
        if account == nil {
            let user = AppDataManager.getAccount()
            if user != nil {
                return User.getUser(by: user!.id).map { result in
                    if result.success {
                        self.account = result.data!
                    }
                    return result
                }
            } else {
                return User.randomUser().flatMap { result in
                    result.onSuccess {
                        self.account = result.data!
                        return AppDataManager.saveAccount(user: result.data!)
                    }
                }
            }
        } else {
            return Observable.just(Result(success: true, data: account))
        }
    }

    func getRooms() -> Observable<Result<[LiveKtvRoom]>> {
        return LiveKtvRoom.getRooms()
    }

    func create(room: LiveKtvRoom) -> Observable<Result<LiveKtvRoom>> {
        Logger.log(self, message: "create \(room.channelName)", level: .info)
        if let user = account {
            room.userId = user.id
            return LiveKtvRoom.create(room: room)
                .map { result in
                    if result.success {
                        room.id = result.data!
                        return Result(success: true, data: room)
                    } else {
                        return Result(success: false, message: result.message)
                    }
                }
        } else {
            return Observable.just(Result(success: false, message: "account is nil!"))
        }
    }

    func join(room: LiveKtvRoom) -> Observable<Result<LiveKtvRoom>> {
        Logger.log(self, message: "join \(room.channelName)", level: .info)
        if let user = account {
            if member == nil {
                member = LiveKtvMember(id: "", isMuted: false, isSelfMuted: false, role: LiveKtvRoomRole.listener.rawValue, roomId: room.id, streamId: 0, userId: user.id)
            }
            guard let member = member else {
                return Observable.just(Result(success: false, message: "member is nil!"))
            }
            if rtcServer.channel == room.id {
                return Observable.just(Result(success: true, data: room))
            } else {
                return Observable.just(rtcServer.isJoinChannel)
                    .concatMap { joining -> Observable<Result<Void>> in
                        if joining {
                            return self.leave()
                        } else {
                            return Observable.just(Result(success: true))
                        }
                    }
                    .concatMap { result -> Observable<Result<Void>> in
                        result.onSuccess {
                            // set default status when join room
                            member.isMuted = false
                            member.role = room.userId == user.id ? LiveKtvRoomRole.manager.rawValue : LiveKtvRoomRole.listener.rawValue
                            member.isManager = room.userId == user.id
                            member.isSelfMuted = false
                            // member.room = room
                            member.userId = user.id
                            return Observable.just(result)
                        }
                    }
                    .concatMap { result -> Observable<Result<LiveKtvRoom>> in
                        result.onSuccess { LiveKtvRoom.getRoom(by: room.id) }
                    }
                    .concatMap { result -> Observable<Result<Void>> in
                        result.onSuccess { self.rtcServer.joinChannel(member: member, channel: room.id, setting: self.setting) }
                    }
                    .concatMap { result -> Observable<Result<Void>> in
                        member.roomId = room.id
                        return result.onSuccess { self.member!.join(streamId: self.rtcServer.uid) }
                    }
                    .concatMap { result -> Observable<Result<LiveKtvRoom>> in
                        if result.success {
                            self.room = room
                            return Observable.just(Result(success: true, data: room))
                        } else {
                            self.member = nil
                            self.room = nil
                            if self.rtcServer.isJoinChannel {
                                return self.rtcServer.leaveChannel().map { _ in
                                    Result(success: false, message: result.message)
                                }
                            }
                            return Observable.just(Result(success: false, message: result.message))
                        }
                    }
            }
        } else {
            return Observable.just(Result(success: false, message: "account is nil!"))
        }
    }

    func leave() -> Observable<Result<Void>> {
        Logger.log(self, message: "leave", level: .info)
        if let member = member {
            if rtcServer.isJoinChannel {
                return Observable.zip(
                    rtcServer.leaveChannel(),
                    member.leave()
                ).map { result0, result1 in
                    if !result0.success || !result1.success {
                        Logger.log(self, message: "leaveRoom error: \(result0.message ?? "") \(result1.message ?? "")", level: .error)
                    }
                    // self.member = nil
                    // self.room = nil
                    return Result(success: true)
                }
            } else {
                return Observable.just(Result(success: true))
            }
        } else {
            return Observable.just(Result(success: true))
        }
    }

//    func subscribeActions() -> Observable<Result<LiveKtvAction>> {
//        if let member = member {
//            return member.subscribeActions()
//        } else {
//            return Observable.just(Result(success: false, message: "member is nil!"))
//        }
//    }
//
    func subscribeRoom() -> Observable<Result<LiveKtvRoom>> {
        guard let room = room else {
            return Observable.just(Result(success: false, message: "room is nil!"))
        }
        return room.subscribe()
    }

    func subscribeMembers() -> Observable<Result<[LiveKtvMember]>> {
        guard let room = room else {
            return Observable.just(Result(success: false, message: "room is nil!"))
        }
        return Observable.combineLatest(
            room.subscribeMembers(),
            rtcServer.onSpeakersChanged()
        )
        .filter { [unowned self] _ in
            self.rtcServer.isJoinChannel
        }
        .throttle(RxTimeInterval.milliseconds(20), latest: true, scheduler: scheduler)
        .map { [unowned self] args -> Result<[LiveKtvMember]> in
            let (result, _) = args
            if result.success {
                if let list = result.data {
                    // order members list
                    let managers = list.filter { member in
                        member.isManager
                    }
                    let others = list.filter { member in
                        !member.isManager
                    }
                    let list = managers + others
                    let speakers = list.filter { member in
                        member.isSpeaker()
                    }
                    if speakers.count > 8 {
                        for index in 8 ..< speakers.count {
                            speakers[index].role = LiveKtvRoomRole.listener.rawValue
                        }
                    }
                    // sync local user status
                    let findCurrentUser = list.first { member in
                        member.id == self.member?.id
                    }
                    if let me = findCurrentUser, let old = member {
                        me.isSelfMuted = old.isSelfMuted
                        old.isMuted = me.isMuted
                        old.role = me.role
                        self.rtcServer.setClientRole(me.role != LiveKtvRoomRole.listener.rawValue ? .broadcaster : .audience, self.setting.audienceLatency)
                        self.rtcServer.muteLocalMicrophone(mute: me.isMuted || me.isSelfMuted)
                    }
                    return Result(success: true, data: list)
                }
            }
            return result
        }
    }

//
//    func inviteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>> {
//        if let user = self.member {
//            if rtcServer.isJoinChannel, user.isManager {
//                return user.inviteSpeaker(member: member)
//            }
//        }
//        return Observable.just(Result(success: true))
//    }
//
//    func muteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>> {
//        if let user = self.member {
//            if rtcServer.isJoinChannel, user.isManager {
//                return member.mute(mute: true)
//            }
//        }
//        return Observable.just(Result(success: true))
//    }
//
//    func unMuteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>> {
//        if let user = self.member {
//            if rtcServer.isJoinChannel, user.isManager {
//                return member.mute(mute: false)
//            }
//        }
//        return Observable.just(Result(success: true))
//    }
//
//    func kickSpeaker(member: LiveKtvMember) -> Observable<Result<Void>> {
//        if let user = self.member {
//            if rtcServer.isJoinChannel, user.isManager {
//                return member.asListener()
//            }
//        }
//        return Observable.just(Result(success: true))
//    }
    func handsUp() -> Observable<Result<Void>> {
        if let member = member {
            if rtcServer.isJoinChannel {
                return member.asSpeaker()
            }
        }
        return Observable.just(Result(success: true))
    }

    func kickSpeaker(member: LiveKtvMember) -> Observable<Result<Void>> {
        if let user = self.member {
            if rtcServer.isJoinChannel, user.isManager {
                return member.asListener()
            }
        }
        return Observable.just(Result(success: true))
    }

//
//    func process(request: LiveKtvAction, agree: Bool) -> Observable<Result<Void>> {
//        switch request.action {
//        case .requestLeft:
//            return request.setLeftSpeaker(agree: agree)
//        case .requestRight:
//            return request.setRightSpeaker(agree: agree)
//        default:
//            return Observable.just(Result(success: true))
//        }
//    }
//
//    func process(invitionLeft: LiveKtvAction, agree: Bool) -> Observable<Result<Void>> {
//        return invitionLeft.setLeftInvition(agree: agree)
//    }
//
//    func process(invitionRight: LiveKtvAction, agree: Bool) -> Observable<Result<Void>> {
//        return invitionRight.setRightInvition(agree: agree)
//    }
//
//    func handsUp(left: Bool?) -> Observable<Result<Void>> {
//        if let member = member {
//            if rtcServer.isJoinChannel {
//                if let left = left {
//                    if left {
//                        return member.requestLeft()
//                    } else {
//                        return member.requestRight()
//                    }
//                } else {
//                    return member.handsup()
//                }
//            }
//        }
//        return Observable.just(Result(success: true))
//    }
//
    func closeMicrophone(close: Bool) -> Observable<Result<Void>> {
        if let member = member {
            member.isSelfMuted = close
            if rtcServer.isJoinChannel {
                rtcServer.muteLocalMicrophone(mute: close)
                return member.selfMute(mute: close)
            } else {
                return Observable.just(Result(success: true))
            }
        } else {
            return Observable.just(Result(success: true))
        }
    }

    func isMicrophoneClose() -> Bool {
        return rtcServer.muted
    }

//    func bindLocalVideo(view: UIView?) {
//        if let view = view {
//            rtcServer.bindLocalVideo(view: view)
//        } else {
//            rtcServer.unbindLocalVideo()
//        }
//    }
//
//    func bindRemoteVideo(view: UIView?, uid: UInt) {
//        if let view = view {
//            rtcServer.bindRemoteVideo(view: view, uid: uid)
//        } else {
//            rtcServer.unbindRemoteVideo(uid: uid)
//        }
//    }

//    func subscribeMessages() -> Observable<Result<LiveKtvMessage>> {
//        if let member = member {
//            return rtmServer.login(user: member.id)
//                .concatMap { [unowned self] result in
//                    result.onSuccess {
//                        self.rtmServer.join(room: member.room.id)
//                    }
//                }
//                .concatMap { [unowned self] result in
//                    result.onSuccess {
//                        self.rtmServer.subscribeMessages(room: member.room.id)
//                    }
//                }
//        } else {
//            return Observable.just(Result(success: false, message: "member is nil!"))
//        }
//    }
//
//    func sendMessage(message: String) -> Observable<Result<Void>> {
//        if let member = member {
//            return rtmServer.sendMessage(room: member.room.id, message: message)
//        } else {
//            return Observable.just(Result(success: false, message: "member is nil!"))
//        }
//    }
}
