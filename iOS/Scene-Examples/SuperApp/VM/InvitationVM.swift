//
//  InvitationVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import AgoraSyncManager

protocol InvitationVMDelegate: NSObjectProtocol {
    func invitationVM(_ vm: InvitationVM,
                      didUpdate infos: [InvitationVM.Info],
                      errorMsg: String?)
}

class InvitationVM: NSObject {
    typealias Info = InvitationCell.Info
    var sceneRef: SceneReference!
    weak var delegate: InvitationVMDelegate?
    private var userInfos = [SuperAppUserInfo]()
    
    init(sceneRef: SceneReference) {
        self.sceneRef = sceneRef
    }
    
    func start() {
        sceneRef.collection(className: "member")
            .get { [weak self](objs) in
                guard let `self` = self else { return }
                
                let localUserId = StorageManager.uuid
                let decoder = JSONDecoder()
                var userInfos = objs.compactMap({ $0.toJson() })
                    .compactMap({ $0.data(using: .utf8) })
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
                self.delegate?.invitationVM(self, didUpdate: infos, errorMsg: nil)
            } fail: { [weak self](error) in
                guard let `self` = self else { return }
                self.delegate?.invitationVM(self, didUpdate: [],
                                            errorMsg: error.description)
                
            }
    }
    
    func getUserInfo(index: Int) -> SuperAppUserInfo? {
        return userInfos[index]
    }
}
