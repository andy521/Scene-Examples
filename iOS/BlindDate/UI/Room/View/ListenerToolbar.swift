//
//  ListenerToolbar.swift
//  BlindDate
//
//  Created by XC on 2021/4/22.
//

import Core
import Foundation
import RxSwift
import UIKit

class ListenerToolbar: UIView {
    weak var delegate: RoomController!
    let disposeBag = DisposeBag()

    var sendMsgBtn: IconButton = {
        let view = IconButton()
        view.icon = "tool_send_message"
        return view
    }()

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        addSubview(sendMsgBtn)

        sendMsgBtn.height(constant: 34)
            .marginLeading(anchor: leadingAnchor, constant: 12)
            .centerY(anchor: centerYAnchor)
            .active()
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func subcribeUIEvent() {
        sendMsgBtn.rx.tap
            .debounce(RxTimeInterval.microseconds(300), scheduler: MainScheduler.instance)
            .throttle(RxTimeInterval.seconds(1), scheduler: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] _ in
                self.delegate.enableInputMessage()
            })
            .disposed(by: disposeBag)
    }

    func onReceivedAction(_ result: Result<BlindDateAction>) {
        if !result.success {
            Logger.log(message: result.message ?? "unknown error".localized, level: .error)
        } else {
            if let action = result.data {
                switch action.action {
                case .invite:
                    if action.status == .ing {
                        InvitedDialog().show(with: action, delegate: delegate)
                    }
                default:
                    Logger.log(message: "received action \(action.action)", level: .info)
                }
            }
        }
    }

    func subcribeRoomEvent() {}
}
