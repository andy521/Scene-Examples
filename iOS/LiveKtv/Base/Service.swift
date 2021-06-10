//
//  Service.swift
//  LiveKtv
//
//  Created by XC on 2021/6/8.
//

import Core
import Foundation
import RxSwift

protocol IRoomManager {
    var account: User? { get set }
    var member: LiveKtvMember? { get set }
    var room: LiveKtvRoom? { get set }
    var setting: LocalSetting { get set }
    func updateSetting()

    func getAccount() -> Observable<Result<User>>
    func getRooms() -> Observable<Result<[LiveKtvRoom]>>
    func create(room: LiveKtvRoom) -> Observable<Result<LiveKtvRoom>>
    func join(room: LiveKtvRoom) -> Observable<Result<LiveKtvRoom>>
    func leave() -> Observable<Result<Void>>

    func closeMicrophone(close: Bool) -> Observable<Result<Void>>
    func isMicrophoneClose() -> Bool
//
//    func enableBeauty(enable: Bool)
//    func isEnableBeauty() -> Bool
    func subscribeRoom() -> Observable<Result<LiveKtvRoom>>
    func subscribeMembers() -> Observable<Result<[LiveKtvMember]>>
//    func subscribeActions() -> Observable<Result<LiveKtvAction>>
//
//    //func inviteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>>
//    //func muteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>>
//    //func unMuteSpeaker(member: LiveKtvMember) -> Observable<Result<Void>>
    func handsUp() -> Observable<Result<Void>>
    func kickSpeaker(member: LiveKtvMember) -> Observable<Result<Void>>
//
//    func process(request: LiveKtvAction, agree: Bool) -> Observable<Result<Void>>
//    func process(invitionLeft: LiveKtvAction, agree: Bool) -> Observable<Result<Void>>
//    func process(invitionRight: LiveKtvAction, agree: Bool) -> Observable<Result<Void>>
//
//    func handsUp(left: Bool?) -> Observable<Result<Void>>

    // func bindLocalVideo(view: UIView?)
    // func bindRemoteVideo(view: UIView?, uid: UInt)

    // func subscribeMessages() -> Observable<Result<LiveKtvMessage>>
    // func sendMessage(message: String) -> Observable<Result<Void>>

    func destory()
}

protocol ErrorDescription {
    associatedtype Item
    static func toErrorString(type: Item, code: Int32) -> String
}
