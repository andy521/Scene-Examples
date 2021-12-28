//
//  SuperAppAudienceViewController.swift
//  Scene-Examples
//
//  Created by ZYP on 2021/12/28.
//

import UIKit
import AgoraRtcKit

class SuperAppAudienceViewController: UIViewController {
    let mainView = MainView()
    var syncUtil: SuperAppSyncUtil!
    var pushUrlString: String!
    var pullUrlString: String!
    var agoraKit: AgoraRtcEngineKit!
    var mediaPlayer: AgoraRtcMediaPlayerProtocol!
    var config: Config!
    var mode: Mode = .pull
    /// log tag
    let defaultLogTag = "AudienceVC"
    
    public init(config: Config) {
        self.config = config
        self.pushUrlString = "rtmp://examplepush.agoramdn.com/live/" + config.sceneId
        self.pullUrlString = "http://examplepull.agoramdn.com/live/\(config.sceneId).flv"
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
        syncUtil.joinByAudience(complted: joinCompleted(error:))
        syncUtil.subscribePKInfo()
        /// default mode is pull
        initMediaPlayer()
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
    
    func changeToRtc() { /** 切换rtc模式 **/
        LogUtils.logInfo(message: "切换到rtc模式", tag: defaultLogTag)
        mode = .rtc
        mediaPlayer.stop()
        agoraKit.destroyMediaPlayer(mediaPlayer)
        mediaPlayer = nil
        joinRtc()
    }
    
    func changeToPull() { /** 切换到拉流模式 **/
        LogUtils.logInfo(message: "切换到拉流模式", tag: defaultLogTag)
        mode = .pull
        leaveRtc()
        initMediaPlayer()
        stopRenderRemoteView()
    }
    
    func showCloseAlert() {
        let vc = UIAlertController(title: "提示", message: "房间已关闭", preferredStyle: .alert)
        vc.addAction(.init(title: "确定", style: .default, handler: { [unowned self](_) in
            self.close()
            self.dismiss(animated: true, completion: nil)
        }))
        present(vc, animated: true, completion: nil)
    }
    
    private func close() {
        syncUtil.unsubscribePKInfo()
        syncUtil.leaveByAudience()
        closeRtc()
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
extension SuperAppAudienceViewController: SuperAppSyncUtilDelegate {
    func superAppSyncUtilDidPkAccept(util: SuperAppSyncUtil, userIdPK: String) { /** userIdPK, no an empty string **/
        LogUtils.logInfo(message: "收到上麦申请", tag: defaultLogTag)
        changeToRtc()
    }
    
    func superAppSyncUtilDidPkCancle(util: SuperAppSyncUtil) { /** userIdPK, an empty string **/
        LogUtils.logInfo(message: "下麦", tag: defaultLogTag)
        changeToPull()
    }
    
    func superAppSyncUtilDidSceneClose(util: SuperAppSyncUtil) { /** scene was delete **/
        showCloseAlert()
    }
}

// MARK: - UI Event MainViewDelegate
extension SuperAppAudienceViewController: MainViewDelegate {
    func mainView(_ view: MainView, didTap action: MainView.Action) {
        switch action {
        case .close:
            close()
            dismiss(animated: true, completion: nil)
            return
        case .closeRemote:
            syncUtil.resetPKInfo()
            return
        case .member, .more:
            break
        }
    }
}

// MARK: - Data Struct
extension SuperAppAudienceViewController {
    struct Config {
        let appId: String
        let sceneName: String
        let sceneId: String
    }
    
    enum Mode {
        /// 拉流模式
        case pull
        /// rtc模式
        case rtc
    }
}
