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
        mainView.setPersonViewHidden(hidden: true)
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
        syncUtil.subscribePKInfo()
        
        /// default mode is push
        joinRtcByPush()
    }
    
    private func setupUI() {
        view.addSubview(mainView)
        mainView.frame = view.bounds
        mainView.delegate = self
    }
    
    func startRenderRemoteView() {
        mainView.setRemoteViewHidden(hidden: false)
    }
    
    func stopRenderRemoteView() {
        mainView.setRemoteViewHidden(hidden: true)
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
        stopRenderRemoteView()
    }
    
    private func joinCompleted(error: LocalizedError?) {
        if let e = error {
            let msg = "joinByAudience fail: \(e.errorDescription ?? "")"
            LogUtils.logInfo(message: msg, tag: defaultLogTag)
            return
        }
        LogUtils.logInfo(message: "joinByAudience success", tag: defaultLogTag)
    }
}

// MRK: - SuperAppSyncUtilDelegate
extension SuperAppHostViewController: SuperAppSyncUtilDelegate {
    func superAppSyncUtilDidPkAccept(util: SuperAppSyncUtil, userIdPK: String) { /** userIdPK, no an empty string **/
        LogUtils.logInfo(message: "收到上麦申请", tag: defaultLogTag)
        
    }
    
    func superAppSyncUtilDidPkCancle(util: SuperAppSyncUtil) { /** userIdPK, an empty string **/
        LogUtils.logInfo(message: "下麦", tag: defaultLogTag)
        
    }
    
    func superAppSyncUtilDidSceneClose(util: SuperAppSyncUtil) { }
}

// MARK: - UI Event MainViewDelegate
extension SuperAppHostViewController: MainViewDelegate {
    func mainView(_ view: MainView, didTap action: MainView.Action) {
        
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
