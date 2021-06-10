//
//  RoomManagerToolBar.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/11.
//

import Core
import Foundation
import RxSwift
import UIKit

class RoomManagerToolbar: UIView {
    weak var delegate: RoomController!
    let disposeBag = DisposeBag()

    var returnView: IconButton = {
        let view = IconButton()
        view.icon = "iconExit"
        view.label = "Leave quietly".localized
        return view
    }()

    var handsupNoticeView: IconButton = {
        let view = IconButton()
        view.icon = "iconUpNotice"
        return view
    }()

    var onMicView: IconButton = {
        let view = IconButton()
        view.icon = "iconMicOn"
        return view
    }()

    var isMuted: Bool = false {
        didSet {
            onMicView.icon = isMuted ? "redMic" : "iconMicOn"
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        addSubview(returnView)
        addSubview(handsupNoticeView)
        addSubview(onMicView)

        onMicView.height(constant: 36)
            .marginTrailing(anchor: trailingAnchor, constant: 16)
            .centerY(anchor: centerYAnchor)
            .active()

        handsupNoticeView.height(constant: 36)
            .marginTrailing(anchor: onMicView.leadingAnchor, constant: 16)
            .centerY(anchor: centerYAnchor)
            .active()

        returnView.height(constant: 36)
            .marginLeading(anchor: leadingAnchor, constant: 16)
            .centerY(anchor: centerYAnchor)
            .active()
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
    }

    func subcribeUIEvent() {
        returnView.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .concatMap { [unowned self] _ in
                self.delegate.showAlert(title: "Leave room".localized, message: "Leaving the room ends the session and removes everyone".localized)
            }
            .filter { close in
                close
            }
            .flatMap { [unowned self] _ in
                self.delegate.viewModel.leaveRoom(action: .leave)
            }
            .filter { [unowned self] result in
                if !result.success {
                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                self.delegate.pop()
            }
            .subscribe(onNext: { [unowned self] _ in
                self.delegate.leaveAction?(.leave, self.delegate.viewModel.isManager ? self.delegate.viewModel.room : nil)
            })
            .disposed(by: disposeBag)

        onMicView.rx.tap
            .debounce(RxTimeInterval.microseconds(300), scheduler: MainScheduler.instance)
            .throttle(RxTimeInterval.seconds(1), scheduler: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                self.delegate.viewModel.selfMute(mute: !self.delegate.viewModel.muted())
            }
            .subscribe(onNext: { [unowned self] result in
                if !result.success {
                    self.delegate.show(message: result.message ?? "unknown error".localized, type: .error)
                }
            })
            .disposed(by: disposeBag)

        handsupNoticeView.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] _ in
                HandsupListDialog().show(delegate: self.delegate)
            })
            .disposed(by: disposeBag)

        delegate.viewModel.isMuted
            .startWith(delegate.viewModel.muted())
            .distinctUntilChanged()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] muted in
                self.isMuted = muted
            })
            .disposed(by: disposeBag)

        delegate.viewModel.syncLocalUIStatus()
    }

    func onReceivedAction(_ result: Result<PodcastAction>) {
        if !result.success {
            Logger.log(message: result.message ?? "unknown error".localized, level: .error)
        } else {
            if let action = result.data {
                switch action.action {
                case .invite:
                    if action.status == .refuse {
                        delegate.show(message: "\(action.member.user.name) \("declines your request".localized)", type: .error)
                    }
                default:
                    Logger.log(message: "\(action.member.user.name)", level: .info)
                }
            }
        }
    }

    func subcribeRoomEvent() {
        delegate.viewModel.onHandsupListChange
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] list in
                self.handsupNoticeView.count = list.count
            })
            .disposed(by: disposeBag)
    }
}
