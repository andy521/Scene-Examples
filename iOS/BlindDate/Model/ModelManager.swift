//
//  ModelManager.swift
//  BlindDate
//
//  Created by XC on 2021/6/4.
//

import Core
import Foundation
import RxSwift

public extension BlindDateRoom {
    static let TABLE: String = "ROOM_MARRY"
    static let ANCHOR_ID: String = "anchorId"
    static let CHANNEL_NAME: String = "channelName"
}

public extension BlindDateMember {
    static let TABLE: String = "MEMBER_MARRY"
    static let MUTED: String = "isMuted"
    static let SELF_MUTED: String = "isSelfMuted"
    static let ROLE: String = "role"
    static let ROOM: String = "roomId"
    static let STREAM_ID = "streamId"
    static let USER = "userId"
}

public extension BlindDateAction {
    static let TABLE: String = "ACTION_MARRY"
    static let ACTION: String = "action"
    static let MEMBER: String = "memberId"
    static let ROOM: String = "roomId"
    static let STATUS: String = "status"
}

public protocol IBlindDateModelManager {
    func create(room: BlindDateRoom) -> Observable<Result<String>>
    func delete(room: BlindDateRoom) -> Observable<Result<Void>>
    func getRooms() -> Observable<Result<[BlindDateRoom]>>
    func getRoom(by objectId: String) -> Observable<Result<BlindDateRoom>>
    func update(room: BlindDateRoom) -> Observable<Result<String>>
    func getMembers(room: BlindDateRoom) -> Observable<Result<[BlindDateMember]>>
    func getCoverSpeakers(room: BlindDateRoom) -> Observable<Result<[BlindDateMember]>>
    func subscribeMembers(room: BlindDateRoom) -> Observable<Result<[BlindDateMember]>>

    func join(member: BlindDateMember, streamId: UInt) -> Observable<Result<Void>>
    func mute(member: BlindDateMember, mute: Bool) -> Observable<Result<Void>>
    func selfMute(member: BlindDateMember, mute: Bool) -> Observable<Result<Void>>
    func asListener(member: BlindDateMember) -> Observable<Result<Void>>
    func asLeftSpeaker(member: BlindDateMember, agree: Bool) -> Observable<Result<Void>>
    func asRightSpeaker(member: BlindDateMember, agree: Bool) -> Observable<Result<Void>>
    func leave(member: BlindDateMember) -> Observable<Result<Void>>
    func subscribeActions(member: BlindDateMember) -> Observable<Result<BlindDateAction>>
    func handsup(member: BlindDateMember) -> Observable<Result<Void>>
    func requestLeft(member: BlindDateMember) -> Observable<Result<Void>>
    func requestRight(member: BlindDateMember) -> Observable<Result<Void>>
    func inviteSpeaker(master: BlindDateMember, member: BlindDateMember) -> Observable<Result<Void>>
    func rejectInvition(member: BlindDateMember) -> Observable<Result<Void>>
}
