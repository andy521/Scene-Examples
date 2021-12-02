//
//  MainVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/17.
//

import UIKit
import SyncManager

class MainVC: UIViewController {
    typealias ConfigHost = MainVMHost.Config
    typealias ConfigAudience = MainVMAudience.Config
    
    let mainView = MainView()
    var vm: MainVMProtocol
    
    public init(config: ConfigAudience,
                syncManager: SyncManager) {
        vm = MainVMAudience(config: config,
                            syncManager: syncManager)
        mainView.setPersonViewHidden(hidden: true)
        super.init(nibName: nil, bundle: nil)
    }
    
    public init(config: ConfigHost,
                syncManager: SyncManager) {
        vm = MainVMHost(config: config,
                        syncManager: syncManager)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    private func setup() {
        view.addSubview(mainView)
        mainView.frame = view.bounds
    }
    
    private func commonInit() {
        mainView.delegate = self
        vm.delegate = self
        vm.start()
    }
}

extension MainVC: MainViewDelegate {
    func mainView(_ view: MainView, didTap action: MainView.Action) {
        switch action {
        case .member:
            let vc = InvitationVC(sceneRef: vm.getSceneRef())
            vc.delegate = self
            vc.show(in: self)
            return
        case .more:
            let vc = ToolVC()
            vc.show(in: self)
            return
        case .close:
            vm.close()
            dismiss(animated: true, completion: nil)
            return
        case .closeRemote:
            vm.cancleConnect()
            return
        }
    }
}

extension MainVC: MainVMDelegate {
    func mainVMShouldStopRenderRemoteView(_ vm: MainVMProtocol) {
        mainView.setRemoteViewHidden(hidden: true)
    }
    
    func mainVMShouldStartRenderRemoteView(_ vm: MainVMProtocol) {
        mainView.setRemoteViewHidden(hidden: false)
    }
    
    func mainVMShouldGetLocalRender(_ vm: MainVMProtocol) -> UIView {
        return mainView.renderViewLocal
    }
    
    func mainVMShouldGetRemoteRender(_ vm: MainVMProtocol) -> UIView {
        return mainView.renderViewRemote
    }
    
    func mainVM(_ vm: MainVMProtocol,
                didJoinRoom info: RoomInfo) {
        let imageName = StorageManager.uuid.headImageName
        let info = MainView.Info(title: info.roomName,
                                 imageName: imageName,
                                 userCount: info.userCount)
        mainView.update(info: info)
    }
    
    func mainVM(_ vm: MainVMProtocol,
                shouldShow tips: String) {
        show(tips)
    }
}

extension MainVC: InvitationVCDelegate {
    func invitationVC(_ vc: InvitationVC, didInvited user: UserInfo) {
        vm.invite(userIdPK: user.userId)
    }
}
