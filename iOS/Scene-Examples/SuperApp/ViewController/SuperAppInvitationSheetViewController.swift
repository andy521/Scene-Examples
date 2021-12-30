//
//  InvitationVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/18.
//

import UIKit
import Presentr
import AgoraSyncManager

protocol SuperAppInvitationSheetDelegate: NSObjectProtocol {
    func invitationVC(_ vc: SuperAppInvitationSheetViewController, didInvited user: SuperAppUserInfo)
}

class SuperAppInvitationSheetViewController: BaseViewController {
    typealias Info = SuperAppInvitationViewCell.Info
    let invitedView = SuperAppInvitationView()
    private let presenter = Presentr(presentationType: .bottomHalf)
    var syncUtil: SuperAppSyncUtil!
    weak var delegate: SuperAppInvitationSheetDelegate?
    private var userInfos = [SuperAppUserInfo]()
    
    init(syncUtil: SuperAppSyncUtil) {
        self.syncUtil = syncUtil
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
        fetchInfos()
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
    
    func fetchInfos() {
        syncUtil.getMembers { [weak self](strings) in
            guard let `self` = self else { return }
            
            let localUserId = SupperAppStorageManager.uuid
            let decoder = JSONDecoder()
            var userInfos = strings.compactMap({ $0.data(using: .utf8) })
                .compactMap({ try? decoder.decode(SuperAppUserInfo.self, from: $0) })
                .filter({ $0.userId != localUserId })
            
            /// 查重
            var dict = [String : SuperAppUserInfo]()
            for userInfo in userInfos {
                dict[userInfo.userId] = userInfo
            }
            userInfos = dict.map({ $0.value })
            
            self.userInfos = userInfos
            
            var infos = [Info]()
            for (index, userInfo) in userInfos.enumerated() {
                let info = Info(idnex: index,
                                title: userInfo.userName,
                                imageName: userInfo.userId.headImageName,
                                isInvited: false)
                infos.append(info)
            }
            self.update(infos: infos)
        } fail: { [weak self](error) in
            guard let `self` = self else { return }
            self.show(error.errorDescription ?? "unknow error (fetchInfos)")
        }
    }
    
    func getUserInfo(index: Int) -> SuperAppUserInfo? {
        return userInfos[index]
    }
}

extension SuperAppInvitationSheetViewController: SuperAppInvitationViewDelegate {
    func invitationView(_ view: SuperAppInvitationView,
                        didSelectedAt index: Int) {
        guard let userInfo = getUserInfo(index: index) else {
            return
        }
        dismiss(animated: true, completion: { [weak self] in
            guard let `self` = self else { return }
            self.delegate?.invitationVC(self, didInvited: userInfo)
        })
    }
}

