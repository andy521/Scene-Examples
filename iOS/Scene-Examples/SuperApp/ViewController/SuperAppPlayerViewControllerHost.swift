//
//  SuperAppPlayerViewControllerHost.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import UIKit
import AgoraRtcKit

class SuperAppPlayerViewControllerHost: UIViewController {
    let mainView = SuperAppMainView()
    var syncUtil: SuperAppSyncUtil!
    var pushUrlString: String!
    var agoraKit: AgoraRtcEngineKit!
    var config: Config!
    var mode: Mode!
    let liveTranscoding = AgoraLiveTranscoding.default()
    /// log tag
    let defaultLogTag = "HostVC"
    var audioIsMute = false
    var allowChangeToPushMode: Bool!

    public init(config: Config) {
        self.config = config
        self.mode = config.mode
        self.pushUrlString = "rtmp://examplepush.agoramdn.com/live/" + config.sceneId
        self.allowChangeToPushMode = mode == .push
        let userId = SupperAppStorageManager.uuid
        let userName = SupperAppStorageManager.userName
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
        syncUtil.joinByHost(roomInfo: config.roomItem, complted: joinCompleted(error:))
        
        config.mode == .push ? joinRtcByPush() : joinRtcByPassPush()
    }
    
    private func setupUI() {
        mainView.setPersonViewHidden(hidden: false)
        view.addSubview(mainView)
        mainView.frame = view.bounds
        mainView.delegate = self
        mainView.setPersonViewHidden(hidden: false)
        let imageName = SupperAppStorageManager.uuid.headImageName
        let info = SuperAppMainView.Info(title: config.sceneName + "(\(config.sceneId))",
                                 imageName: imageName,
                                 userCount: 0)
        mainView.update(info: info)
    }

    private func changeToByPassPush() {
        guard mode != .byPassPush else {
            return
        }
        LogUtils.logInfo(message: "changeToByPassPush", tag: defaultLogTag)
        mode = .byPassPush
        leaveRtcByPush()
    }
    
    private func changeToPush() {
        guard mode != .push else {
            return
        }
        LogUtils.logInfo(message: "changeToPush", tag: defaultLogTag)
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
extension SuperAppPlayerViewControllerHost: SuperAppSyncUtilDelegate {
    func superAppSyncUtilDidPkCancleForOther(util: SuperAppSyncUtil) {
        LogUtils.logInfo(message: "下麦", tag: defaultLogTag)
        if allowChangeToPushMode { changeToPush() }
        else { mainView.setRemoteViewHidden(hidden: true) }
    }
    
    func superAppSyncUtilDidPkAcceptForMe(util: SuperAppSyncUtil, userIdPK: String) {}
    func superAppSyncUtilDidPkCancleForMe(util: SuperAppSyncUtil) {}
    func superAppSyncUtilDidPkAcceptForOther(util: SuperAppSyncUtil) {}
    func superAppSyncUtilDidSceneClose(util: SuperAppSyncUtil) {}
}

// MARK: - UI Event MainViewDelegate
extension SuperAppPlayerViewControllerHost: SuperAppMainViewDelegate {
    func mainView(_ view: SuperAppMainView, didTap action: SuperAppMainView.Action) {
        switch action {
        case .member:
            let vc = SuperAppInvitationSheetViewController(syncUtil: syncUtil)
            vc.delegate = self
            vc.show(in: self)
            return
        case .more:
            let vc = SuperAppToolSheetViewController()
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
            if allowChangeToPushMode { changeToPush() }
            else { mainView.setRemoteViewHidden(hidden: true) }
            return
        }
    }
}

extension SuperAppPlayerViewControllerHost: SuperAppToolSheetDelegate, SuperAppInvitationSheetDelegate {
    func toolVC(_ vc: SuperAppToolSheetViewController, didTap action: SuperAppToolSheetViewController.Action) {
        switch action {
        case .camera:
            switchCamera()
        case .mic:
            audioIsMute = !audioIsMute
            muteLocalAudio(mute: audioIsMute)
        }
    }
    
    func invitationVC(_ vc: SuperAppInvitationSheetViewController, didInvited user: SuperAppUserInfo) {
        changeToByPassPush()
        syncUtil.updatePKInfo(userIdPK: user.userId)
    }
}

// MARK: - Data Struct
extension SuperAppPlayerViewControllerHost {
    enum Mode: Int {
        /// 直推模式
        case push = 1
        /// 旁路推流模式
        case byPassPush = 2
    }
    
    struct Config {
        let appId: String
        let roomItem: SuperAppRoomInfo
        
        var sceneId: String {
            return roomItem.roomId
        }
        
        var sceneName: String {
            return roomItem.roomName
        }
        
        var mode: Mode {
            return roomItem.liveMode == .push ? .push : .byPassPush
        }
    }
}
