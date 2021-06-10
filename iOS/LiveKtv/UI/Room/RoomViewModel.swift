//
//  RoomViewModel.swift
//  LiveKtv
//
//  Created by XC on 2021/6/9.
//

import Core
import Foundation
import RxCocoa
import RxRelay
import RxSwift

// class SpeakerGroup: NSObject {
//    let list: [LiveKtvMember]
//
//    init(list: [LiveKtvMember]) {
//        let managers = list.filter { member in
//            member.isManager
//        }
//        let others = list.filter { member in
//            !member.isManager
//        }
//        self.list = managers + others
//    }
// }

class RoomViewModel {
    private let disposeBag = DisposeBag()
    weak var delegate: RoomControlDelegate!

    var room: LiveKtvRoom {
        return RoomManager.shared().room!
    }

    var isSpeaker: Bool {
        return member.isSpeaker()
    }

    var isManager: Bool {
        return member.isManager
    }

    var role: LiveKtvRoomRole {
        return isManager ? .manager : isSpeaker ? .speaker : .listener
    }

    var account: User {
        return RoomManager.shared().account!
    }

    var member: LiveKtvMember {
        return RoomManager.shared().member!
    }

    var roomManager: LiveKtvMember?

    var muted: Bool {
        return RoomManager.shared().isMicrophoneClose()
    }

    func subcribeRoomEvent() {
        RoomManager.shared()
            .subscribeRoom()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] result in
                if !result.success {
                    self.delegate?.onError(message: result.message)
                } else if result.data == nil {
                    self.delegate?.onRoomClosed()
                } else {
                    self.delegate?.onRoomUpdate()
                }
            })
            .disposed(by: disposeBag)

        RoomManager.shared()
            .subscribeMembers()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] result in
                self.delegate?.onMuted(mute: muted)
                if result.success {
                    if let list = result.data {
                        if list.count == 0 {
                            self.roomManager = nil
                        } else {
                            self.roomManager = list.first { member in
                                member.isManager
                            }
                        }
                        self.delegate?.onRoomMemberChanged(list: list)
                    }
                } else {
                    self.delegate?.onError(message: result.message)
                }
            })
            .disposed(by: disposeBag)
    }

    func changeMV(mv: String, onWaiting: @escaping (Bool) -> Void,
                  onSuccess: @escaping () -> Void,
                  onError: @escaping (String) -> Void)
    {
        room.changeMV(localMV: mv)
            .observe(on: MainScheduler.instance)
            .do(onSubscribe: {
                onWaiting(true)
            }, onDispose: {
                onWaiting(false)
            })
            .subscribe { result in
                if result.success {
                    onSuccess()
                } else {
                    onError(result.message ?? "unknown error".localized)
                }
            } onDisposed: {
                onWaiting(false)
            }
            .disposed(by: disposeBag)
    }

    func kickSpeaker(member: LiveKtvMember,
                     onWaiting: @escaping (Bool) -> Void,
                     onSuccess: @escaping () -> Void,
                     onError: @escaping (String) -> Void)
    {
        RoomManager.shared()
            .kickSpeaker(member: member)
            .observe(on: MainScheduler.instance)
            .do(onSubscribe: {
                onWaiting(true)
            }, onDispose: {
                onWaiting(false)
            })
            .subscribe { result in
                if result.success {
                    onSuccess()
                } else {
                    onError(result.message ?? "unknown error".localized)
                }
            } onDisposed: {
                onWaiting(false)
            }
            .disposed(by: disposeBag)
    }

    func handsUp(onWaiting: @escaping (Bool) -> Void,
                 onSuccess: @escaping () -> Void,
                 onError: @escaping (String) -> Void)
    {
        RoomManager.shared()
            .handsUp()
            .observe(on: MainScheduler.instance)
            .do(onSubscribe: {
                onWaiting(true)
            }, onDispose: {
                onWaiting(false)
            })
            .subscribe { result in
                if result.success {
                    onSuccess()
                } else {
                    onError(result.message ?? "unknown error".localized)
                }
            } onDisposed: {
                onWaiting(false)
            }
            .disposed(by: disposeBag)
    }

    func selfMute(mute: Bool) {
        delegate?.onMuted(mute: mute)
        RoomManager.shared()
            .closeMicrophone(close: mute)
            .subscribe(onNext: { [unowned self] result in
                if !result.success {
                    self.delegate?.onMuted(mute: self.muted)
                    self.delegate?.onError(message: result.message)
                }
            })
            .disposed(by: disposeBag)
    }

    func leaveRoom(onWaiting: @escaping (Bool) -> Void,
                   onSuccess: @escaping () -> Void,
                   onError: @escaping (String) -> Void)
    {
        RoomManager.shared()
            .leave()
            .observe(on: MainScheduler.instance)
            .do(onSubscribe: {
                onWaiting(true)
            }, onDispose: {
                onWaiting(false)
            })
            .subscribe { result in
                if result.success {
                    onSuccess()
                } else {
                    onError(result.message ?? "unknown error".localized)
                }
            } onDisposed: {
                onWaiting(false)
            }
            .disposed(by: disposeBag)
    }
}
