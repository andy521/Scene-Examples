//
//  ModelManager.swift
//  InteractivePodcast
//
//  Created by XC on 2021/6/4.
//

import Core
import Foundation
import RxSwift

public extension PodcastRoom {
    static let TABLE: String = "ROOM"
    static let ANCHOR_ID: String = "anchorId"
    static let CHANNEL_NAME: String = "channelName"
}

public extension PodcastMember {
    static let TABLE: String = "MEMBER"
    static let MUTED: String = "isMuted"
    static let SELF_MUTED: String = "isSelfMuted"
    static let IS_SPEAKER: String = "isSpeaker"
    static let ROOM: String = "roomId"
    static let STREAM_ID = "streamId"
    static let USER = "userId"
}

public extension PodcastAction {
    static let TABLE: String = "ACTION"
    static let ACTION: String = "action"
    static let MEMBER: String = "memberId"
    static let ROOM: String = "roomId"
    static let STATUS: String = "status"
}

public protocol IPodcastModelManager {
    func create(room: PodcastRoom) -> Observable<Result<String>>
    func delete(room: PodcastRoom) -> Observable<Result<Void>>
    func getRooms() -> Observable<Result<[PodcastRoom]>>
    func getRoom(by objectId: String) -> Observable<Result<PodcastRoom>>
    func update(room: PodcastRoom) -> Observable<Result<String>>
    func getMembers(room: PodcastRoom) -> Observable<Result<[PodcastMember]>>
    func getCoverSpeakers(room: PodcastRoom) -> Observable<Result<[PodcastMember]>>
    func subscribeMembers(room: PodcastRoom) -> Observable<Result<[PodcastMember]>>

    func join(member: PodcastMember, streamId: UInt) -> Observable<Result<Void>>
    func mute(member: PodcastMember, mute: Bool) -> Observable<Result<Void>>
    func selfMute(member: PodcastMember, mute: Bool) -> Observable<Result<Void>>
    func asSpeaker(member: PodcastMember, agree: Bool) -> Observable<Result<Void>>
    func leave(member: PodcastMember) -> Observable<Result<Void>>
    func subscribeActions(member: PodcastMember) -> Observable<Result<PodcastAction>>
    func handsup(member: PodcastMember) -> Observable<Result<Void>>
    func inviteSpeaker(master: PodcastMember, member: PodcastMember) -> Observable<Result<Void>>
    func rejectInvition(member: PodcastMember) -> Observable<Result<Void>>
}
