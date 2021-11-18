//
//  MainVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/17.
//

import UIKit
import SyncManager

class MainVC: UIViewController {
    typealias Config = MainVM.Config
    let mainView = MainView()
    let vm: MainVM
    
    public init(config: Config,
                syncManager: SyncManager) {
        vm = MainVM(config: config,
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
            let vc = InvitationVC(sceneRef: vm.sceneRef)
            vc.show(in: self)
            return
        case .more:
            let vc = ToolVC()
            vc.show(in: self)
            return
        default:
            return
        }
    }
}

extension MainVC: MainVMDelegate {
    func mainVMShouldGetLocalRender(_ vm: MainVM) -> UIView {
        return mainView.renderViewLocal
    }
    
    func mainVMShouldGetRemoteRender(_ vm: MainVM) -> UIView {
        return mainView.renderViewRemote
    }
    
    func mainVM(_ vm: MainVM,
                didJoinRoom info: RoomInfo) {
        let info = MainView.Info(title: info.roomName,
                                 imageName: "pic-11",
                                 userCount: info.userCount)
        mainView.update(info: info)
    }
    
    func mainVM(_ vm: MainVM,
                shouldShow tips: String) {
        show(tips)
    }
}
