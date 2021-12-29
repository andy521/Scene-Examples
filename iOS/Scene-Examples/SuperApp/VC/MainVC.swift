//
//  MainVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/17.
//

import UIKit
import AgoraSyncManager

class MainVC: UIViewController {
    typealias ConfigHost = MainVMHost.Config
    typealias ConfigAudience = MainVMAudience.Config
    
    let mainView = MainView()
    var vm: MainVMProtocol
    
    public init(config: ConfigAudience,
                syncManager: AgoraSyncManager) {
        vm = MainVMAudience(config: config,
                            syncManager: syncManager)
        mainView.setPersonViewHidden(hidden: true)
        super.init(nibName: nil, bundle: nil)
    }
    
    public init(config: ConfigHost,
                syncManager: AgoraSyncManager) {
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
//            let vc = InvitationVC(sceneRef: vm.getSceneRef())
//            vc.delegate = self
//            vc.show(in: self)
            return
        case .more:
            let vc = ToolVC()
            let open = !vm.getLocalAudioMuteState()
            vc.setMicState(open: open)
            vc.delegate = self
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
        let info = MainView.Info(title: info.roomName + "(\(info.roomId))",
                                 imageName: imageName,
                                 userCount: info.userCount)
        mainView.update(info: info)
    }
    
    func mainVM(_ vm: MainVMProtocol,
                shouldShow tips: String) {
        show(tips)
    }
    
    func mainVMShouldCloseRoom(_ vm: MainVMProtocol) {
        let vc = UIAlertController(title: "提示", message: "房间已关闭", preferredStyle: .alert)
        vc.addAction(.init(title: "确定", style: .default, handler: { [unowned self](_) in
            vm.close()
            self.dismiss(animated: true, completion: nil)
        }))
        present(vc, animated: true, completion: nil)
    }
    
}

extension MainVC: InvitationVCDelegate {
    func invitationVC(_ vc: InvitationVC, didInvited user: SuperAppUserInfo) {
        vm.invite(userIdPK: user.userId)
    }
}

extension MainVC: ToolVCDelegate {
    func toolVC(_ vc: ToolVC, didTap action: ToolVC.Action) {
        switch action {
        case .camera:
            vm.switchCamera()
        case .mic:
            vm.revertMuteLocalAudio()
        }
    }
}
