//
//  InvitationVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import SyncManager

protocol InvitationVMDelegate: NSObjectProtocol {
    func invitationVM(_ vm: InvitationVM,
                      didUpdate infos: [InvitationVM.Info],
                      errorMsg: String?)
}

class InvitationVM: NSObject {
    typealias Info = InvitationCell.Info
    var sceneRef: SceneReference!
    weak var delegate: InvitationVMDelegate?
    private var userInfos = [UserInfo]()
    
    init(sceneRef: SceneReference) {
        self.sceneRef = sceneRef
    }
    
    func start() {
        sceneRef.collection(className: "member")
            .get { [weak self](objs) in
                guard let `self` = self else { return }
                
                let localUserId = StorageManager.uuid
                let decoder = JSONDecoder()
                let userInfos = objs.compactMap({ $0.toJson() })
                    .compactMap({ $0.data(using: .utf8) })
                    .compactMap({ try? decoder.decode(UserInfo.self, from: $0) })
                    .filter({ $0.userId != localUserId })
                self.userInfos = userInfos
                
                var infos = [Info]()
                for (index, userInfo) in userInfos.enumerated() {
                    let info = Info(idnex: index,
                                    title: userInfo.userName,
                                    imageName: userInfo.userId.headImageName,
                                    isInvited: false)
                    infos.append(info)
                }
                self.delegate?.invitationVM(self, didUpdate: infos,
                                            errorMsg: nil)
            } fail: { [weak self](error) in
                guard let `self` = self else { return }
                self.delegate?.invitationVM(self, didUpdate: [],
                                            errorMsg: error.description)
                
            }
    }
    
    func getUserInfo(index: Int) -> UserInfo? {
        return userInfos[index]
    }
}
