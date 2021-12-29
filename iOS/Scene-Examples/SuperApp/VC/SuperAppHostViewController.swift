//
//  SuperAppHostViewController.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import UIKit
import AgoraRtcKit

class SuperAppHostViewController: UIViewController {
    let mainView = MainView()
    var syncUtil: SuperAppSyncUtil!
    var pushUrlString: String!
    var agoraKit: AgoraRtcEngineKit!
    var config: Config!
    var mode: Mode!
    let liveTranscoding = AgoraLiveTranscoding.default()
    /// log tag
    let defaultLogTag = "HostVC"
    var audioIsMute = false

    public init(config: Config) {
        self.config = config
        self.mode = config.mode
        self.pushUrlString = "rtmp://examplepush.agoramdn.com/live/" + config.sceneId
        let userId = StorageManager.uuid
        let userName = StorageManager.userName
        self.syncUtil = SuperAppSyncUtil(appId: config.appId,
                                         sceneId: config.sceneId,
                                         sceneName: config.sceneName,
                                         userId: userId,
                                         userName: userName)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        
        syncUtil.delegate = self
        syncUtil.joinByHost(createTime: config.createdTime,
                            liveMode: config.mode.rawValue,
                            complted: joinCompleted(error:))
        
        config.mode == .push ? joinRtcByPush() : joinRtcByPassPush()
    }
    
    private func setupUI() {
        mainView.setPersonViewHidden(hidden: false)
        view.addSubview(mainView)
        mainView.frame = view.bounds
        mainView.delegate = self
        mainView.setPersonViewHidden(hidden: false)
        let imageName = StorageManager.uuid.headImageName
        let info = MainView.Info(title: config.sceneName + "(\(config.sceneId))",
                                 imageName: imageName,
                                 userCount: 0)
        mainView.update(info: info)
    }

    private func changeToByPassPush() {
        if mode != .byPassPush {
            mode = .byPassPush
            leaveRtcByPush()
        }
    }
    
    private func changeToPush() {
        mode = .push
        leaveRtcByPassPush()
        mainView.setRemoteViewHidden(hidden: true)
    }
    
    /// `true` is mute
    func getLocalAudioMuteState() -> Bool {
        return audioIsMute
    }
    
    private func destroy() {
        syncUtil.leaveByHost()
        destroyRtc()
    }
    
    private func joinCompleted(error: LocalizedError?) {
        if let e = error {
            let msg = "joinByAudience fail: \(e.errorDescription ?? "")"
            LogUtils.logInfo(message: msg, tag: defaultLogTag)
            return
        }
        syncUtil.subscribePKInfo()
        LogUtils.logInfo(message: "joinByAudience success", tag: defaultLogTag)
    }
}

// MRK: - SuperAppSyncUtilDelegate
extension SuperAppHostViewController: SuperAppSyncUtilDelegate {
    func superAppSyncUtilDidPkAccept(util: SuperAppSyncUtil, userIdPK: String) {}
    func superAppSyncUtilDidSceneClose(util: SuperAppSyncUtil) {}
    
    func superAppSyncUtilDidPkCancle(util: SuperAppSyncUtil) { /** userIdPK, an empty string **/
        LogUtils.logInfo(message: "下麦", tag: defaultLogTag)
        changeToPush()
    }
}

// MARK: - UI Event MainViewDelegate
extension SuperAppHostViewController: MainViewDelegate {
    func mainView(_ view: MainView, didTap action: MainView.Action) {
        switch action {
        case .member:
            let vc = InvitationVC(syncUtil: syncUtil)
            vc.delegate = self
            vc.show(in: self)
            return
        case .more:
            let vc = ToolVC()
            let open = getLocalAudioMuteState()
            vc.setMicState(open: open)
            vc.delegate = self
            vc.show(in: self)
            return
        case .close:
            destroy()
            dismiss(animated: true, completion: nil)
            return
        case .closeRemote:
            syncUtil.resetPKInfo()
            return
        }
    }
}

extension SuperAppHostViewController: ToolVCDelegate, InvitationVCDelegate {
    func toolVC(_ vc: ToolVC, didTap action: ToolVC.Action) {
        switch action {
        case .camera:
            switchCamera()
        case .mic:
            audioIsMute = !audioIsMute
            muteLocalAudio(mute: audioIsMute)
        }
    }
    
    func invitationVC(_ vc: InvitationVC, didInvited user: SuperAppUserInfo) {
        changeToByPassPush()
        syncUtil.updatePKInfo(userIdPK: user.userId)
    }
}

// MARK: - Data Struct
extension SuperAppHostViewController {
    enum Mode: Int {
        /// 直推模式
        case push = 1
        /// 旁路推流模式
        case byPassPush = 2
    }
    
    struct Config {
        let appId: String
        let sceneName: String
        let sceneId: String
        let createdTime: TimeInterval
        let mode: Mode
    }
}
