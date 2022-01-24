//
//  CreateLiveController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit
import AgoraRtcKit
import AgoraSyncManager

class CreateLiveController: BaseViewController, FUEditViewControllerDelegate {
    
    
    private lazy var randomNameView: LiveRandomNameView = {
        let view = LiveRandomNameView()
        return view
    }()
    private lazy var localView: UIView = {
        let view = UIView()
        return view
    }()
    private lazy var cameraChangeButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "icon-camera-change"), for: .normal)
        button.addTarget(self, action: #selector(clickCameraChangeButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var settingButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "icon-setting-normal"), for: .normal)
        button.setImage(UIImage(named: "icon-setting"), for: .selected)
        button.addTarget(self, action: #selector(clickSettingLiveButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var editButton: UIButton = {
        let button = UIButton()
        button.setTitle("编辑", for: .normal)
        button.addTarget(self, action: #selector(clickEditButton(sender:)), for: .touchUpInside)
        return button
    }()
    private lazy var startLiveButton: UIButton = {
        let button = UIButton()
        button.setTitle("Create_Start".localized, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14)
        button.backgroundColor = .blueColor
        button.layer.cornerRadius = 20
        button.layer.masksToBounds = true
        button.addTarget(self, action: #selector(clickStartLiveButton), for: .touchUpInside)
        return button
    }()
    private var agoraKit: AgoraRtcEngineKit?
    private var avaterEngine: AvatarEngineProtocol!
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
       let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        config.channelProfile = .liveBroadcasting
        return config
    }()
    private lazy var channelMediaOptions: AgoraRtcChannelMediaOptions = {
       let option = AgoraRtcChannelMediaOptions()
        option.publishAudioTrack = .of(true)
        option.publishCameraTrack = .of(true)
        option.clientRoleType = .of((Int32)(AgoraClientRole.broadcaster.rawValue))
        option.autoSubscribeVideo = .of(true)
        option.autoSubscribeAudio = .of(true)
        return option
    }()
    private var liveSettingModel: LiveSettingUseData?
    private var sceneType: SceneType = .singleLive
    
    init(sceneType: SceneType) {
        super.init(nibName: nil, bundle: nil)
        self.sceneType = sceneType
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupAgoraKit()
        setupUI()
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationTransparent(isTransparent: true)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationTransparent(isTransparent: false)
    }
    
    private func setupUI() {
        view.backgroundColor = .init(hex: "#404B54")
        randomNameView.translatesAutoresizingMaskIntoConstraints = false
        startLiveButton.translatesAutoresizingMaskIntoConstraints = false
        settingButton.translatesAutoresizingMaskIntoConstraints = false
        localView.translatesAutoresizingMaskIntoConstraints = false
        editButton.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(localView)
        view.addSubview(randomNameView)
        view.addSubview(startLiveButton)
        view.addSubview(settingButton)
        view.addSubview(editButton)
        
        localView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        localView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        localView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        localView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
        
        randomNameView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10).isActive = true
        randomNameView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 20).isActive = true
        randomNameView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10).isActive = true
        randomNameView.heightAnchor.constraint(equalToConstant: 40).isActive = true
        
        startLiveButton.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        startLiveButton.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -35).isActive = true
        startLiveButton.widthAnchor.constraint(equalToConstant: 100).isActive = true
        startLiveButton.heightAnchor.constraint(equalToConstant: 40).isActive = true
        
        settingButton.centerYAnchor.constraint(equalTo: startLiveButton.centerYAnchor).isActive = true
        settingButton.leadingAnchor.constraint(equalTo: startLiveButton.trailingAnchor, constant: 25).isActive = true
        
        editButton.centerYAnchor.constraint(equalTo: startLiveButton.centerYAnchor).isActive = true
        editButton.rightAnchor.constraint(equalTo: startLiveButton.leftAnchor, constant: -25).isActive = true
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(customView: cameraChangeButton)
        
        ToastView.show(text: "Limit_Toast".localized,
                       tagImage: UIImage(named: "icon-yellow-caution"),
                       postion: .bottom,
                       view: view)
    }
    
    private func setupAgoraKit() {
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: nil)
        avaterEngine = agoraKit!.queryAvatarEngine()
        agoraKit?.setLogFile(LogUtils.sdkLogPath())
        
        let _ = AuthPack.bytes.withUnsafeBytes { pointer in
            avaterEngine?.initialize(Data(bytes: pointer))
        }
        
        let avatarConfigs = AgoraAvatarConfigs()
        avatarConfigs.mode = .avatar
        avatarConfigs.enable_face_detection = 1
        avatarConfigs.enable_human_detection = 1
        avaterEngine?.enableOrUpdateLocalAvatarVideo(true, configs: avatarConfigs)
        
//        var path:String!
//        path = getPath("hair_hat_6")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 0, bundle: path)
//
//        path = getPath("head")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 1, bundle: path)
//
//        path = getPath("wuguan_mesh", dir: "Resource/QItems/background/dress_2d/mid/")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 2, bundle: path)
//
//        path = getPath("midBody_male0", dir: nil)
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 3, bundle: path)
//
//
//        path = getPath("taozhuang_2_shoes", dir: "/Resource/QItems/cloth/shoes/mid")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 4, bundle: path)
//
//        path = getPath("yiqun_3", dir: "Resource/QItems/cloth/dress/mid")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 5, bundle: path)
//
//
//        path = getPath("cam_02", dir: "Resource/page_cam")
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 6, bundle: path)
//
//        path = getPath("ani_idle", dir: nil)
//        avaterEngine?.enableAvatarGeneratorItem(true, type: 7, bundle: path)
        
        let _ = FUManager.shareInstance()
        FUManager.shareInstance().avatarEngine = avaterEngine
        FUManager.shareInstance().setAvatarStyleDefault()
        FUManager.shareInstance().setupForHalfMode()
        
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = UserInfo.userId
        canvas.renderMode = .hidden
        canvas.view = localView
        avaterEngine.setupLocalVideoCanvas(canvas)
        agoraKit?.startPreview()
    }
    
    @objc
    private func clickCameraChangeButton(sender: UIButton) {
        agoraKit?.switchCamera()
    }
    @objc
    private func clickSettingLiveButton(sender: UIButton) {
        let settingView = LiveSettingView(title: "Live_Room_Settings".localized,
                                          datas: LiveSettingModel.settingsData(),
                                          useModel: liveSettingModel)
        settingView.liveSettingFinishedClosure = { [weak self] model in
            guard let self = self else { return }
            self.liveSettingModel = model
            self.agoraKit?.setVideoEncoderConfiguration(
                AgoraVideoEncoderConfiguration(size: model.resolution,
                                               frameRate: model.framedate,
                                               bitrate: model.sliderValue,
                                               orientationMode: .fixedPortrait,
                                               mirrorMode: .auto))
        }
        AlertManager.show(view: settingView,
                          alertPostion: .bottom)
    }
    @objc
    private func clickEditButton(sender: UIButton) {
        let vc = FUEditViewController.instacneFromStoryBoard()!
        vc.delegate = self
        vc.modalPresentationStyle = .fullScreen
        self.present(vc, animated: true, completion: nil)
        let renderView = vc.getVideoView()!
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = UserInfo.userId
        canvas.renderMode = .hidden
        canvas.view = renderView
        avaterEngine?.setupLocalVideoCanvas(canvas)
        agoraKit?.startPreview()
    }
    
    @objc
    private func clickStartLiveButton() {
        let roomInfo = LiveRoomInfo(roomName: randomNameView.text)
        let params = JSONObject.toJson(roomInfo)
        SyncUtil.joinScene(id: roomInfo.roomId, userId: roomInfo.userId, property: params, success: { results in
            guard let result = results.first else { return }
            self.startLiveHandler(result: result)
        })
    }
    
    private func startLiveHandler(result: IObject) {
        LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
        let channelName = result.getPropertyWith(key: "roomId", type: String.self) as? String
        
        switch sceneType {
        case .singleLive:
            let livePlayerVC = SignleLiveController(channelName: channelName ?? "",
                                                    sceneType: sceneType,
                                                    userId: "\(UserInfo.userId)",
                                                    agoraKit: agoraKit)
            navigationController?.pushViewController(livePlayerVC, animated: true)
            
        case .pkApply:
            let pkLiveVC = PKLiveController(channelName: channelName ?? "",
                                            sceneType: sceneType,
                                            userId: "\(UserInfo.userId)",
                                            agoraKit: agoraKit)
            navigationController?.pushViewController(pkLiveVC, animated: true)
            
        case .game:
            let dgLiveVC = GameLiveController(channelName: channelName ?? "",
                                            sceneType: sceneType,
                                            userId: "\(UserInfo.userId)",
                                            agoraKit: agoraKit)
            navigationController?.pushViewController(dgLiveVC, animated: true)
            
        case .playTogether:
            let playTogetherVC = PlayTogetherViewController(channelName: channelName ?? "",
                                                            sceneType: sceneType,
                                                            userId: "\(UserInfo.userId)",
                                                            agoraKit: agoraKit)
            navigationController?.pushViewController(playTogetherVC, animated: true)
            
        case .oneToOne:
            
            let canvas = AgoraRtcVideoCanvas()
            canvas.uid = UserInfo.userId
            canvas.renderMode = .hidden
            canvas.view = nil
            avaterEngine.setupLocalVideoCanvas(canvas)
            let oneToOneVC = OneToOneViewController(channelName: channelName ?? "",
                                                    sceneType: sceneType,
                                                    userId: UserInfo.uid,
                                                    agoraKit: agoraKit,
                                                    avaterEngine: avaterEngine)
            navigationController?.pushViewController(oneToOneVC, animated: true)
        
        default: break
        }
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
    
    func getPath(_ name: String, dir: String? = "Resource/4") -> String? {
        return Bundle.main.path(forResource: name,
                                ofType: "bundle",
                                inDirectory: dir)
    }
    
    func editViewControllerDidClose() {
        let canvas = AgoraRtcVideoCanvas()
        canvas.uid = UserInfo.userId
        canvas.renderMode = .hidden
        canvas.view = localView
        avaterEngine.setupLocalVideoCanvas(canvas)
        agoraKit?.startPreview()
    }
}




