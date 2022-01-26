//
//  OneToOneViewController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/12/21.
//

import UIKit
import AgoraRtcKit
import AgoraUIKit_iOS

class OneToOneViewController: BaseViewController, FUEditViewControllerDelegate {
    public lazy var localView = AGEView()
    public lazy var remoteView: AGEButton = {
        let button = AGEButton()
        button.setTitle("远程视频", for: .normal)
        button.layer.cornerRadius = 5
        button.shadowOffset = CGSize(width: 0, height: 0)
        button.shadowColor = .init(hex: "#000000")
        button.shadowRadius = 5
        button.shadowOpacity = 0.5
        button.buttonStyle = .filled(backgroundColor: .gray)
        return button
    }()
    private lazy var containerView: AGEView = {
        let view = AGEView()
        return view
    }()
    private lazy var onoToOneGameView: OnoToOneGameView = {
        let topControlView = OnoToOneGameView()
        return topControlView
    }()
    private lazy var controlView = OneToOneControlView()
       
    public var agoraKit: AgoraRtcEngineKit?
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    private var videoPixelSize: CGSize?
    private let videoHandler = VideoFrameHandler()
    private lazy var channelMediaOptions: AgoraRtcChannelMediaOptions = {
        let option = AgoraRtcChannelMediaOptions()
        option.publishCameraTrack = AgoraRtcBoolOptional.of(false)
        option.publishAvatarTrack = AgoraRtcBoolOptional.of(true)
        option.clientRoleType = .of((Int32)(AgoraClientRole.broadcaster.rawValue))
        option.autoSubscribeVideo = AgoraRtcBoolOptional.of(true)
        return option
    }()
    
    public lazy var viewModel = GameViewModel(channleName: channelName,
                                              ownerId: UserInfo.uid)
    
    private(set) var channelName: String = ""
    public var canvasDataArray = [LiveCanvasModel]()
    private(set) var sceneType: SceneType = .singleLive
    private var roleType: GameRoleType {
        currentUserId == UserInfo.uid ? .broadcast : .audience
    }
    private(set) var currentUserId: String = ""
    private var isCloseGame: Bool = false
    private var isSelfExitGame: Bool = false

    var avaterEngine: AvatarEngineProtocol!
    var currentMode = Mode.avatar
    
    init(channelName: String,
         sceneType: SceneType,
         userId: String,
         agoraKit: AgoraRtcEngineKit? = nil,
         avaterEngine: AvatarEngineProtocol? = nil) {
        super.init(nibName: nil, bundle: nil)
        self.channelName = channelName
        self.currentUserId = userId
        self.sceneType = sceneType
        self.agoraKit = agoraKit
        self.avaterEngine = avaterEngine
    }
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupAgoraKit()
        eventHandler()
        // 设置屏幕常亮
        UIApplication.shared.isIdleTimerDisabled = true
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationTransparent(isTransparent: true)
    }
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        joinChannel(channelName: channelName)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
        var controllers = navigationController?.viewControllers ?? []
        guard let index = controllers.firstIndex(where: { $0 is CreateLiveController }) else { return }
        controllers.remove(at: index)
        navigationController?.viewControllers = controllers
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        agoraKit?.muteAllRemoteAudioStreams(true)
        agoraKit?.muteAllRemoteVideoStreams(true)
        navigationTransparent(isTransparent: false)
        UIApplication.shared.isIdleTimerDisabled = false
        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    private func eventHandler() {
        onoToOneGameView.onClickControlButtonClosure = { [weak self] type, isSelected in
            guard let self = self else { return }
            if type == .exit {
                self.controlView.isHidden = false
                self.viewModel.leaveGame(roleType: self.roleType)
                self.isCloseGame = false
                let gameInfo = GameInfoModel(status: .end, gameUid: UserInfo.uid, gameId: .you_draw_i_guess)
                SyncUtil.update(id: self.channelName, key: SYNC_MANAGER_GAME_INFO, params: JSONObject.toJson(gameInfo))
                self.isSelfExitGame = true
                return
            }
            self.clickControlViewHandler(controlType: type, isSelected: isSelected)
        }
        controlView.onClickControlButtonClosure = { [weak self] type, isSelected in
            self?.clickControlViewHandler(controlType: type, isSelected: isSelected)
        }
        /// 监听游戏开始
        SyncUtil.subscribe(id: channelName, key: SYNC_MANAGER_GAME_INFO, onUpdated: { [weak self] object in
            guard let self = self else { return }
            let model = JSONObject.toModel(GameInfoModel.self, value: object.toJson())
            if model?.status == .playing {
                self.isSelfExitGame = false
                self.showAlert(title: "对方邀请您玩游戏", message: "") {
                    self.onoToOneGameView.setLoadUrl(urlString: model?.gameId?.gameUrl ?? "",
                                                     roomId: self.channelName,
                                                     roleType: self.roleType)
                    AlertManager.show(view: self.onoToOneGameView, alertPostion: .bottom, didCoverDismiss: false)
                }
            } else if model?.status == .end && !self.isSelfExitGame{
                AlertManager.hiddenView()
                ToastView.show(text: "游戏已结束")
                self.viewModel.leaveGame(roleType: self.roleType)
                self.isSelfExitGame = false
            }
        }, onSubscribed: {
            LogUtils.log(message: "onSubscribed One To One", level: .info)
        })
    }
    
    private func clickControlViewHandler(controlType: OneToOneControlType, isSelected: Bool) {
        switch controlType {
        case .switchCamera:
            
            if currentMode == .avatar {
                currentMode = .ar
                changeToArMode()
            }
            else {
                currentMode = .avatar
                changeToAvatarMode()
            }
            
            
            break
            
        case .game:
//            clickAvaterHandler()
            break
        case .mic:
            agoraKit?.muteLocalAudioStream(isSelected)
            
        case .exit:
            showAlert(title: "退出游戏", message: "确定退出退出游戏 ？") {
                self.controlView.isHidden = false
                AlertManager.hiddenView()
                self.viewModel.leaveGame(roleType: self.roleType)
            }
            
        case .back:
//            let name = channelName
            showAlert(title: "关闭直播间", message: "关闭直播间后，其他用户将不能再和您连线。确定关闭 ？") {
//                SyncUtil.delete(id: name, success: nil, fail: nil)
                self.navigationController?.popViewController(animated: true)
            }
            
        case .close:
            AlertManager.hiddenView()
            controlView.isHidden = false
            isCloseGame = true
        case .edit:
            guard currentMode == .avatar else {
                return
            }
            let vc = FUEditViewController.instacneFromStoryBoard()!
            vc.modalPresentationStyle = .fullScreen
            vc.delegate = self
            if let size = videoPixelSize {
                vc.setPixelBufferSize(size)
            }
            self.present(vc, animated: true, completion: nil)
            
            let renderView = vc.getVideoView()!
            createAgoraVideoCanvas(uid: UserInfo.userId,
                                   isLocal: true,
                                   specialView: renderView)
            
            break
        }
    }
    
    private func clickAvaterHandler() {
        let poseTrackView = FUPoseTrackView()
        poseTrackView.delegate = self
        poseTrackView.translatesAutoresizingMaskIntoConstraints = false
        poseTrackView.widthAnchor.constraint(equalToConstant: Screen.width).isActive = true
        poseTrackView.heightAnchor.constraint(equalToConstant: 100).isActive = true
        AlertManager.show(view: poseTrackView, alertPostion: .bottom)
        poseTrackView.setupData()
    }
    
    private func setupUI() {
        view.backgroundColor = .init(hex: "#404B54")
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIView())
        containerView.translatesAutoresizingMaskIntoConstraints = false
        localView.translatesAutoresizingMaskIntoConstraints = false
        remoteView.translatesAutoresizingMaskIntoConstraints = false
        controlView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(localView)
        view.addSubview(containerView)
        containerView.addSubview(remoteView)
        containerView.addSubview(controlView)
        
        containerView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        containerView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        containerView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        containerView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: Screen.safeAreaBottomHeight()).isActive = true
    
        localView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        localView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        localView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        localView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        
        remoteView.trailingAnchor.constraint(equalTo: localView.trailingAnchor, constant: -15).isActive = true
        remoteView.topAnchor.constraint(equalTo: localView.safeAreaLayoutGuide.topAnchor, constant: 15).isActive = true
        remoteView.widthAnchor.constraint(equalToConstant: 105.fit).isActive = true
        remoteView.heightAnchor.constraint(equalToConstant: 140.fit).isActive = true
        
        controlView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        controlView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        controlView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
    }
    
    private func setupAgoraKit() {
        guard agoraKit == nil else {
            agoraKit?.delegate = self
            agoraKit?.setVideoFrameDelegate(videoHandler)
            videoHandler.delegate = self
            agoraKit?.enableAudio()
            return
        }
    
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        avaterEngine = agoraKit!.queryAvatarEngine()
        agoraKit?.enableAudio()
        agoraKit?.setLogFile(LogUtils.sdkLogPath())
        agoraKit?.setVideoFrameDelegate(videoHandler)
        let _ = AuthPack.bytes.withUnsafeBytes { pointer in
            avaterEngine?.initialize(Data(bytes: pointer))
        }
        
        let avatarConfigs = AgoraAvatarConfigs()
        avatarConfigs.mode = .avatar
        avatarConfigs.enable_face_detection = 1
        avatarConfigs.enable_human_detection = 0
        avaterEngine?.enableOrUpdateLocalAvatarVideo(true, configs: avatarConfigs)
        
        let _ = FUManager.shareInstance()
        let renderer = FURendererObj()
        renderer.avatarEngine = avaterEngine
        FUManager.shareInstance().renderer = renderer
        FUManager.shareInstance().setAvatarStyleDefault()
        FUManager.shareInstance().setupForHalfMode()
        
        videoHandler.delegate = self
    }
    
    private func changeToArMode() {
        avaterEngine = agoraKit!.queryAvatarEngine()
        let avatarConfigs = AgoraAvatarConfigs()
        avatarConfigs.mode = .AR
        avatarConfigs.enable_face_detection = 1
        avatarConfigs.enable_human_detection = 0
        avaterEngine?.enableOrUpdateLocalAvatarVideo(true, configs: avatarConfigs)
        
        let renderer = FURendererObj()
        renderer.avatarEngine = avaterEngine
        FUManager.shareInstance().renderer = renderer
        FUManager.shareInstance().setAsArMode()
    }
    
    private func changeToAvatarMode() {
        FUManager.shareInstance().quitARMode()
        avaterEngine = agoraKit!.queryAvatarEngine()
        let avatarConfigs = AgoraAvatarConfigs()
        avatarConfigs.mode = .avatar
        avatarConfigs.enable_face_detection = 1
        avatarConfigs.enable_human_detection = 0
        avaterEngine?.enableOrUpdateLocalAvatarVideo(true, configs: avatarConfigs)
        
        let renderer = FURendererObj()
        renderer.avatarEngine = avaterEngine
        FUManager.shareInstance().renderer = renderer
        FUManager.shareInstance().setAvatarStyleDefault()
        FUManager.shareInstance().setupForHalfMode()
        FUManager.shareInstance().enableFaceCapture(1)
    }
    
    private func createAgoraVideoCanvas(uid: UInt,
                                        isLocal: Bool = false,
                                        specialView: UIView? = nil) {
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = uid
        canvas.renderMode = .hidden
        if isLocal {
            canvas.view = specialView ?? localView
            avaterEngine?.setupLocalVideoCanvas(canvas)
            agoraKit?.startPreview()
        } else {
            canvas.view = specialView ?? remoteView
            agoraKit?.setupRemoteVideo(canvas)
        }
    }
    
    public func joinChannel(channelName: String) {
        self.channelName = channelName
        
        let result = agoraKit?.joinChannel(byToken: KeyCenter.Token,
                                           channelId: channelName,
                                           uid: UserInfo.userId,
                                           mediaOptions: channelMediaOptions)
        guard result != 0 else { return }
        // Error code description can be found at:
        // en: https://docs.agora.io/en/Voice/API%20Reference/oc/Constants/AgoraErrorCode.html
        // cn: https://docs.agora.io/cn/Voice/API%20Reference/oc/Constants/AgoraErrorCode.html
        self.showAlert(title: "Error", message: "joinChannel call failed: \(String(describing: result)), please check your params")
    }
    public func leaveChannel() {
        agoraKit?.leaveChannel({ state in
            LogUtils.log(message: "left channel, duration: \(state.duration)", level: .info)
        })
    }
    
    deinit {
        LogUtils.log(message: "释放 === \(self)", level: .info)
        leaveChannel()
    }
    
    func getPath(_ name: String, dir: String? = "Resource/4") -> String? {
        return Bundle.main.path(forResource: name, ofType: "bundle", inDirectory: dir)
    }
    
    func editViewControllerDidClose() {
        createAgoraVideoCanvas(uid: UserInfo.userId,
                               isLocal: true,
                               specialView: nil)
    }
}
extension OneToOneViewController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        LogUtils.log(message: "warning: \(warningCode.description)", level: .warning)
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        LogUtils.log(message: "error: \(errorCode)", level: .error)
        showAlert(title: "Error", message: "Error \(errorCode.description) occur")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        LogUtils.log(message: "Join \(channel) with uid \(uid) elapsed \(elapsed)ms", level: .info)
        createAgoraVideoCanvas(uid: uid, isLocal: true)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        LogUtils.log(message: "remote user join: \(uid) \(elapsed)ms", level: .info)
        createAgoraVideoCanvas(uid: uid, isLocal: false)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        LogUtils.log(message: "remote user leval: \(uid) reason \(reason)", level: .info)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, reportRtcStats stats: AgoraChannelStats) {
//        localVideo.statsInfo?.updateChannelStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStats stats: AgoraRtcLocalVideoStats) {
//        localVideo.statsInfo?.updateLocalVideoStats(stats)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localAudioStats stats: AgoraRtcLocalAudioStats) {
//        localVideo.statsInfo?.updateLocalAudioStats(stats)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteVideoStats stats: AgoraRtcRemoteVideoStats) {
//        remoteVideo.statsInfo?.updateVideoStats(stats)
        LogUtils.log(message: "remoteVideoWidth== \(stats.width) Height == \(stats.height)", level: .info)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, remoteAudioStats stats: AgoraRtcRemoteAudioStats) {
//        remoteVideo.statsInfo?.updateAudioStats(stats)
    }
    
}


extension OneToOneViewController: FUPoseTrackViewDelegate { /** 选择形象列表的回调 **/
    func poseTrackViewDidSelectedAvatar(_ avatar: FUAvatar) {
//        avatar.loadIdleModePose()
//        FUManager.shareInstance().reloadAvatarToController(with: avatar, isBg: false)
//        FUManager.shareInstance().setupForHalfMode()
    }
    
    func poseTrackViewDidShowTopView(_ show: Bool) {}
    
    func poseTrackViewDidSelectedInput(_ filterName: String) {}
}

extension OneToOneViewController: VideoFrameHandlerDelegate {
    func videoHandlerDidRecvPixelData(_ pixelBuffer: CVPixelBuffer) {
        let width = CVPixelBufferGetWidth(pixelBuffer)
        let height = CVPixelBufferGetHeight(pixelBuffer)
        videoPixelSize = CGSize(width: width,
                                height: height)
        assert(width != 0, "not 0");
        
//        let size = AppManager.getSuitablePixelBufferSizeForCurrentDevice()
//        videoPixelSize = size
    }
}

extension OneToOneViewController {
    enum Mode {
        case avatar
        case ar
    }
}


struct AuthPack {
    static let bytes:[Int8] = [
        72,38,-76,16,52,45,-81,75,-100,83,64,38,62,86,-77,23,-40,-39,25,-100,-98,-58,-51,5,17,-49,23,56,-80,101,-39,118,14,-39,-41,-91,64,64,-49,-17,-5,96,-77,-80,-100,-122,48,-41,85,-70,-81,-32,-68,-79,111,60,-121,-57,63,-23,-109,-122,-74,-122,16,73,23,-44,64,81,-78,-50,14,-14,37,-75,28,8,62,117,106,76,89,120,20,101,15,88,-37,80,-5,116,-10,-3,19,126,85,20,79,-120,25,-92,-54,49,-15,113,45,10,31,31,60,75,-93,-8,-125,47,21,108,-91,-82,32,30,41,-99,77,-27,92,-126,-100,69,15,-31,16,71,68,126,19,92,109,-15,-95,-114,-20,-11,96,-18,-9,119,-30,46,46,126,96,-99,-28,-54,123,50,109,-101,0,-3,-25,-63,-98,9,47,-26,89,-118,74,41,-6,28,-88,27,60,-99,-115,13,27,-73,109,86,39,-54,12,81,-74,94,-7,82,-83,55,-126,125,118,-13,-104,-65,-36,51,-62,47,0,98,-8,-88,65,-25,-124,103,5,37,-99,-67,113,-103,12,-120,63,-42,16,124,107,12,85,101,-56,-41,-79,31,58,-65,123,-43,95,-8,-32,22,-127,-19,-19,126,21,21,-25,-21,-47,-11,-51,-25,-86,-31,-76,75,-45,27,106,-81,-1,48,-45,-23,127,-73,-1,-125,29,-98,120,42,-3,-22,26,104,28,94,-29,110,-93,5,15,23,-70,21,-32,101,-15,91,63,-65,113,100,101,102,-78,-84,-108,82,-71,-34,101,53,106,118,-115,-58,-71,-127,59,-63,-17,95,-35,-31,41,-109,120,-32,-9,-87,67,-46,-103,35,42,119,-12,-42,91,-93,78,112,56,50,-25,-19,-119,126,-38,116,121,-104,-83,47,2,87,2,83,-86,-18,-37,48,118,-52,31,-64,125,41,41,56,-51,-71,106,-8,-41,28,-125,-7,67,125,16,80,-103,45,61,-69,-19,74,88,-109,-109,-27,66,-71,-77,113,-90,-104,-122,-101,-11,-48,90,15,98,20,-29,125,101,-105,111,-123,-121,-38,127,77,-119,89,-42,-58,-97,-58,38,27,60,83,103,44,-7,35,70,-27,-48,37,25,122,-14,-22,97,49,15,65,45,39,-107,-65,-113,-44,-90,92,-49,21,37,24,15,71,22,109,20,3,92,-29,12,21,-33,44,117,-109,93,80,3,-128,-79,-4,59,-98,-50,-105,-50,66,108,74,-85,119,-102,-125,-51,57,83,46,-22,119,-80,23,-75,-44,-124,-93,113,97,-128,80,69,-94,77,68,-99,100,100,-77,-68,124,-6,-24,-35,-17,-12,-35,11,117,57,88,-102,120,-16,-8,-108,35,22,86,-97,115,92,-43,25,-14,113,-116,27,121,-95,0,-4,-88,-59,43,-83,-70,-47,-34,32,-24,105,75,-125,-33,93,30,-35,84,19,-47,-41,-9,-90,100,-31,104,75,-8,-110,40,-41,-94,70,81,-104,37,84,67,-56,27,-50,21,113,84,-106,-114,101,33,-49,71,17,-43,-47,72,-96,-102,14,88,87,49,38,-105,-72,-5,118,-118,-40,63,-82,-65,-63,-47,-126,26,14,102,-74,70,-64,84,-115,13,80,-114,78,106,-7,-128,0,41,-86,-55,-111,-20,31,-72,100,-85,127,68,-5,27,-82,72,116,85,39,-116,59,52,-125,96,-37,11,33,-62,-106,-111,-127,42,126,117,-56,-17,35,5,-89,102,51,47,41,-61,-103,-91,63,44,-68,111,-126,-96,114,31,-97,-95,-74,-9,-103,-30,-49,-94,-76,-121,60,55,95,75,21,126,-88,-58,62,56,-14,60,-45,-55,-3,119,-72,-82,-78,-59,-36,-5,64,101,107,32,26,-58,82,66,-59,32,19,-125,-20,27,-109,5,-122,-126,124,81,35,-62,-39,79,116,71,-4,-123,-126,75,-123,-85,-98,98,109,-100,80,-49,-34,40,46,-87,-79,-47,64,13,23,70,-122,-118,17,15,119,17,-117,86,91,-90,-23,120,-123,64,118,-36,80,-96,-58,-7,-36,-39,-47,-118,-80,-108,8,34,14,92,-77,-109,122,-115,51,69,-64,-109,89,99,105,-109,-34,8,36,-111,114,7,-9,-114,87,20,122,89,38,4,84,44,-19,60,36,-32,-125,69,41,-33,-2,-6,-16,46,-5,119,-19,-95,123,87,59,-94,-90,31,94,-102,-45,119,-62,14,-55,-5,-65,-69,107,102,-75,25,125,95,-47,-88,-115,-55,126,44,30,-3,-99,72,-70,105,-103,-106,-118,43,-115,-20,-107,-46,121,-43,-30,-34,-24,-41,27,16,104,-79,-43,53,-43,-106,107,59,-47,-103,79,-70,106,10,98,-106,-11,82,-6,-86,62,98,45,-77,-26,-84,120,20,-90,104,63,-128,71,94,84,103,-61,51,15,89,95,-83,70,59,11,57,-27,-22,73,98,-40,-7,31,15,81,111,50,69,-118,-113,-128,121,44,125,93,-76,-113,-57,-57,-7,12,-78,55,-100,-45,-20,-117,82,-7,-125,55,72,-97,-86,-90,-86,-109,-109,-16,-94,32,-112,89,72,-37,86,-78,6,117,7,-73,9,42,26,-104,19,117,88,19,-93,96,-101,50,-38,41,34,-64,-4,85,98,-100,7,-90,51,102,-13,-91,-10,41,51,-105,11,-109,-26,-36,-7,62,-56,-70,96,103,-30,108,91,-69,-92,-86,-122,-57,54,102,106,-26,-46,87,-7,-63,91,-52,51,14,85,44,109,84,90,-114,-58,-117,7,80,20,-31,75,28,-123,42,84,-74,5,-105,2,-5,12,12,82,-108,-5,65,16,76,-36,-122,109,-60,48,-118,-14,-85,-95,-27,-126,80,-75,107,-75,112,-80,43,112,-13,-107,-75,114,86,-90,40,64,-104,62,116,40,5,88,-35,-79,-91,-78,119,-50,-27,65,13,127,28,-113,-29,31,65,-37,-79,71,-68,-18,83,67,-27,115,-81,-5,-19,-12,-65,63,-76,45,-10,-3,-19,-79,-109,-67,16,94,-46,71,63,-52,50,19,81,-55,47,-110,-126,-7,-87,-36,66,126,-18,34,-117,-39,68,68,-14,21,93,-73,-113,106,-70,116,-33,-1,-105,-86,100,77,45,4,-80,-116,32,18,69,20,-60,48,76,0,-77,-75,-25,39,36,18,45,-5,-41,5,52,9,-64,90,-119,-107,110,-21,-102,-39,-28,16,-53,-1,-113,85,-88,110,84,14,45,-29,123,78,7,-17,15,-125,-43,73,-8,-120,-52,61,45,66,37,-108,2,-61,-77,-54,53,-47,-109,62,-10,16,-42,-48,-11,103,-119,-53,-30,55,31,91,81,79,26,-35,113,-72,-122,-24,47,-97,-80,43,38,-8,16,48,-44,45,20
    ]
}
