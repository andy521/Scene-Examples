//
//  InvitationVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/18.
//

import UIKit
import Presentr
import SyncManager

protocol InvitationVCDelegate: NSObjectProtocol {
    func invitationVC(_ vc: InvitationVC, didInvited user: UserInfo)
}

class InvitationVC: UIViewController {
    typealias Info = InvitationCell.Info
    let invitedView = InvitationView()
    private let presenter = Presentr(presentationType: .bottomHalf)
    private var vm: InvitationVM!
    weak var delegate: InvitationVCDelegate?
    
    init(sceneRef: SceneReference) {
        vm = InvitationVM(sceneRef: sceneRef)
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
        title = "在线用户"
        view.addSubview(invitedView)
        invitedView.frame = view.bounds
    }
    
    private func commonInit() {
        invitedView.delegate = self
        vm.delegate = self
        vm.start()
    }
    
    func show(in vc: UIViewController) {
        presenter.backgroundTap = .dismiss
        vc.customPresentViewController(presenter,
                                       viewController: self,
                                       animated: true,
                                       completion: nil)
    }
    
    func update(infos: [Info]) {
        invitedView.update(infos: infos)
    }
}

extension InvitationVC: InvitationViewDelegate {
    func invitationView(_ view: InvitationView,
                        didSelectedAt index: Int) {
        guard let userInfo = vm.getUserInfo(index: index) else {
            return
        }
        delegate?.invitationVC(self, didInvited: userInfo)
    }
}

extension InvitationVC: InvitationVMDelegate {
    func invitationVM(_ vm: InvitationVM,
                      didUpdate infos: [InvitationVM.Info],
                      errorMsg: String?) {
        if let text = errorMsg {
            show(text)
            return
        }
        
        update(infos: infos)
    }
}
