//
//  RoomController.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/6.
//

import Core
import Foundation
import IGListKit
import RxCocoa
import RxSwift
import UIKit

class RoomController: BaseViewContoller, DialogDelegate, RoomDelegate {
    @IBOutlet var containerView: UIView! {
        didSet {
            containerView.roundCorners([.topLeft, .topRight], radius: 10)
        }
    }

    @IBOutlet var roomNameView: UILabel!
    @IBOutlet var closeButton: UIButton!
    @IBOutlet var listView: UICollectionView! {
        didSet {
            listView.collectionViewLayout = UICollectionViewFlowLayout()
            listView.alwaysBounceVertical = true
        }
    }

    @IBOutlet var meButton: RoundImageButton!

    @IBOutlet var returnView: UIView! {
        didSet {
            returnView.rounded(color: "#10141C", borderWidth: 2)
        }
    }

    @IBOutlet var tapReturn: UITapGestureRecognizer!

    @IBOutlet var toolbarView: UIView!
    private var roomManagerToolbar: RoomManagerToolbar?
    private var roomSpeakerToolbar: RoomSpeakerToolbar?
    private var roomListenerToolbar: RoomListenerToolbar?

    var leaveAction: ((LeaveRoomAction, PodcastRoom?) -> Void)?
    private lazy var adapter: ListAdapter = {
        ListAdapter(updater: ListAdapterUpdater(), viewController: self)
    }()

    private var dataSourceDisposable: Disposable?
    private var actionDisposable: Disposable?

    var viewModel = RoomViewModel()

    private func renderToolbar() {
        switch viewModel.role {
        case .manager:
            if roomManagerToolbar == nil {
                roomManagerToolbar = RoomManagerToolbar()
                toolbarView.addSubview(roomManagerToolbar!)
                roomManagerToolbar!.fill(view: toolbarView).active()
                roomManagerToolbar!.delegate = self

                if roomSpeakerToolbar != nil || roomListenerToolbar != nil {
                    roomSpeakerToolbar?.removeFromSuperview()
                    roomSpeakerToolbar = nil
                    roomListenerToolbar?.removeFromSuperview()
                    roomListenerToolbar = nil
                }

                roomManagerToolbar?.subcribeUIEvent()
                roomManagerToolbar?.subcribeRoomEvent()
            }
        case .speaker:
            if roomSpeakerToolbar == nil {
                roomSpeakerToolbar = RoomSpeakerToolbar()
                toolbarView.addSubview(roomSpeakerToolbar!)
                roomSpeakerToolbar!.fill(view: toolbarView).active()
                roomSpeakerToolbar!.delegate = self

                if roomManagerToolbar != nil || roomListenerToolbar != nil {
                    roomManagerToolbar?.removeFromSuperview()
                    roomManagerToolbar = nil
                    roomListenerToolbar?.removeFromSuperview()
                    roomListenerToolbar = nil
                }

                roomSpeakerToolbar?.subcribeUIEvent()
                roomSpeakerToolbar?.subcribeRoomEvent()
            }
        case .listener:
            if roomListenerToolbar == nil {
                roomListenerToolbar = RoomListenerToolbar()
                toolbarView.addSubview(roomListenerToolbar!)
                roomListenerToolbar!.fill(view: toolbarView).active()
                roomListenerToolbar!.delegate = self
                if roomSpeakerToolbar != nil || roomManagerToolbar != nil {
                    roomSpeakerToolbar?.removeFromSuperview()
                    roomSpeakerToolbar = nil
                    roomManagerToolbar?.removeFromSuperview()
                    roomManagerToolbar = nil
                    show(message: "You've been set as audience".localized, type: .error)
                }

                roomListenerToolbar?.subcribeUIEvent()
                roomListenerToolbar?.subcribeRoomEvent()
            }
        }
    }

    private func subcribeUIEvent() {
        meButton.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] _ in
                Logger.log(message: "pushViewController \(self.navigationController != nil)", level: .info)
                self.navigationController?.pushViewController(
                    MeController.instance(),
                    animated: true
                )
            })
            .disposed(by: disposeBag)

        tapReturn.rx.event
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .flatMap { [unowned self] _ in
                self.viewModel.leaveRoom(action: .mini)
            }
            .filter { [unowned self] result in
                if !result.success {
                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .flatMap { [unowned self] _ in
                self.pop()
            }
            .subscribe(onNext: { [unowned self] _ in
                self.leaveAction?(.mini, self.viewModel.room)
            })
            .disposed(by: disposeBag)

        closeButton.rx.tap
            .throttle(RxTimeInterval.seconds(2), scheduler: MainScheduler.instance)
            .concatMap { [unowned self] _ -> Observable<Bool> in
                if self.viewModel.isManager {
                    return self.showAlert(title: "Close room".localized, message: "Leaving the room ends the session and removes everyone".localized)
                } else {
                    return Observable.just(true)
                }
            }
            .filter { close in
                close
            }
            .concatMap { [unowned self] _ in
                self.viewModel.leaveRoom(action: .closeRoom)
            }
            .filter { [unowned self] result in
                if !result.success {
                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                }
                return result.success
            }
            .concatMap { [unowned self] _ in
                self.pop()
            }
            .subscribe(onNext: { [unowned self] _ in
                self.leaveAction?(.closeRoom, self.viewModel.room)
            })
            .disposed(by: disposeBag)
    }

    private func subcribeRoomEvent() {
        dataSourceDisposable?.dispose()
        dataSourceDisposable = viewModel.roomMembersDataSource()
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] result -> Observable<Result<Bool>> in
                let roomClosed = result.data
                if roomClosed == true {
                    return self.viewModel.leaveRoom(action: .leave).map { _ in result }
                } else {
                    return Observable.just(result)
                }
            }
            .observe(on: MainScheduler.instance)
            .flatMap { [unowned self] result -> Observable<Result<Bool>> in
                if result.data == true {
                    Logger.log(message: "subcribeRoomEvent roomClosed", level: .info)
                    return self.popAsObservable().map { _ in result }
                } else {
                    self.adapter.performUpdates(animated: false)
                    return Observable.just(result)
                }
            }
            .subscribe(onNext: { [unowned self] result in
                let roomClosed = result.data
                if !result.success {
                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                } else if roomClosed == true {
                    self.leaveAction?(.leave, self.viewModel.room)
                    self.disconnect()
                } else {
                    self.renderToolbar()
                }
            })

        actionDisposable = viewModel.actionsSource()
            .observe(on: MainScheduler.instance)
            .subscribe(onNext: { [unowned self] result in
                switch self.viewModel.role {
                case .manager:
                    roomManagerToolbar?.onReceivedAction(result)
                case .speaker:
                    roomSpeakerToolbar?.onReceivedAction(result)
                case .listener:
                    roomListenerToolbar?.onReceivedAction(result)
                }
            })
    }

    func disconnect() {
        Logger.log(self, message: "disconnect", level: .info)
        dataSourceDisposable?.dispose()
        dataSourceDisposable = nil
        actionDisposable?.dispose()
        actionDisposable = nil
    }

    private func popAsObservable() -> Observable<Bool> {
        return super.pop().asObservable()
    }

    override func pop() -> Single<Bool> {
        disconnect()
        return super.pop()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        meButton.setImage(UIImage(named: viewModel.account.getLocalAvatar(), in: Utils.bundle, with: nil), for: .normal)

        adapter.collectionView = listView
        adapter.dataSource = self
        roomNameView.text = viewModel.room.channelName
        closeButton.isHidden = !viewModel.isManager

        renderToolbar()
        subcribeUIEvent()
        subcribeRoomEvent()
    }

    override func viewDidAppear(_: Bool) {
        navigationController?.interactivePopGestureRecognizer?.delegate = self
    }

    static func instance(leaveAction: @escaping ((LeaveRoomAction, PodcastRoom?) -> Void)) -> RoomController {
        let storyBoard = UIStoryboard(name: "Main", bundle: Utils.bundle)
        let controller = storyBoard.instantiateViewController(withIdentifier: "RoomController") as! RoomController
        controller.leaveAction = leaveAction
        controller.modalPresentationStyle = .fullScreen
        return controller
    }
}

protocol RoomControlDelegate: AnyObject {
    func onTap(member: PodcastMember)
}

extension RoomController: RoomControlDelegate {
    func onTap(member: PodcastMember) {
        if viewModel.isManager {
            if !member.isSpeaker {
                InviteSpeakerDialog().show(with: member, delegate: self)
            } else if member.id != viewModel.member.id {
                ManageSpeakerDialog().show(with: member, delegate: self)
            } else {
                // block self?
                ManageSpeakerDialog().show(with: member, delegate: self)
            }
        }
    }
}

extension RoomController: ListAdapterDataSource {
    func objects(for _: ListAdapter) -> [ListDiffable] {
        return viewModel.memberList as! [ListDiffable]
    }

    func listAdapter(_: ListAdapter, sectionControllerFor object: Any) -> ListSectionController {
        switch object {
        case is String:
            return SectionController()
        case is SpeakerGroup:
            return SpeakersController(delegate: self)
        default:
            return ListenersController(delegate: self)
        }
    }

    func emptyView(for _: ListAdapter) -> UIView? {
        return nil
    }
}

extension RoomController: UIGestureRecognizerDelegate {
    func gestureRecognizerShouldBegin(_: UIGestureRecognizer) -> Bool {
        false
    }
}
