//
//  PKLiveController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/15.
//

import UIKit

class PKLiveController: LivePlayerController {
    public lazy var stopBroadcastButton: UIButton = {
        let button = UIButton()
        button.setTitle("停止连麦", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 13)
        button.backgroundColor = .init(hex: "#000000", alpha: 0.7)
        button.layer.cornerRadius = 19
        button.layer.masksToBounds = true
        button.isHidden = true
        button.addTarget(self, action: #selector(clickStopBroadcast), for: .touchUpInside)
        return button
    }()
    private lazy var vsImageView: UIImageView = {
        let imageView = UIImageView(image: UIImage(named: "PK/pic-VS"))
        imageView.isHidden = true
        return imageView
    }()
    private lazy var countTimeLabel: UILabel = {
        let label = UILabel()
        label.text = "00:00"
        label.textColor = .blueColor
        label.font = .systemFont(ofSize: 14)
        label.isHidden = true
        return label
    }()
    private lazy var pkProgressView: PKLiveProgressView = {
        let view = PKLiveProgressView()
        view.isHidden = true
        return view
    }()
    
    private lazy var timer = GCDTimer()
    private var targetChannelName: String = ""
    private var pkApplyInfoModel: PKApplyInfoModel?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        let bottomType: [LiveBottomView.LiveBottomType] = currentUserId == "\(UserInfo.userId)" ? [.pk, .tool, .close] : [.gift, .close]
        bottomView.updateButtonType(type: bottomType)
        
        stopBroadcastButton.translatesAutoresizingMaskIntoConstraints = false
        vsImageView.translatesAutoresizingMaskIntoConstraints = false
        countTimeLabel.translatesAutoresizingMaskIntoConstraints = false
        pkProgressView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stopBroadcastButton)
        stopBroadcastButton.translatesAutoresizingMaskIntoConstraints = false
        stopBroadcastButton.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -15).isActive = true
        stopBroadcastButton.widthAnchor.constraint(equalToConstant: 83).isActive = true
        stopBroadcastButton.heightAnchor.constraint(equalToConstant: 38).isActive = true
        stopBroadcastButton.bottomAnchor.constraint(equalTo: bottomView.topAnchor, constant: -10).isActive = true
        
        view.addSubview(vsImageView)
        vsImageView.centerXAnchor.constraint(equalTo: liveCanvasView.centerXAnchor).isActive = true
        
        view.addSubview(countTimeLabel)
        countTimeLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        countTimeLabel.bottomAnchor.constraint(equalTo: liveCanvasView.topAnchor, constant: -1).isActive = true
        
        view.addSubview(pkProgressView)
        pkProgressView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 60).isActive = true
        pkProgressView.topAnchor.constraint(equalTo: liveCanvasView.topAnchor, constant: 15).isActive = true
        pkProgressView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -60).isActive = true
        pkProgressView.heightAnchor.constraint(equalToConstant: 40).isActive = true
    }
    
    override func closeLiveHandler() {
        super.closeLiveHandler()
        updatePKInfoStatusToEnd()
    }
    
    override func eventHandler() {
        super.eventHandler()
        // 监听主播发起PK
        SyncUtil.subscribeCollection(id: channleName,
                                     className: sceneType.rawValue,
                                     delegate: PKInviteInfoDelegate(vc: self))
        
        // 监听PKinfo 让观众加入到PK的channel
        SyncUtil.subscribeCollection(id: channleName,
                                     className: SYNC_MANAGER_PK_INFO,
                                     delegate: PKInfoDelegate(vc: self))

        // pk开始回调
        pkLiveStartClosure = { [weak self] applyModel in
            guard let self = self else { return }
            self.pkApplyInfoModel = applyModel
            self.updatePKUIStatus(isStart: true)
        }
        
        // pk 结束回调
        pkLiveEndClosure = { [weak self] applyModel in
            self?.pkApplyInfoModel = applyModel
            self?.updatePKUIStatus(isStart: false)
            self?.deleteSubscribe()
            self?.stopBroadcastButton.isHidden = true
        }
        // 收到礼物回调
        LiveReceivedGiftClosure = { [weak self] giftModel, type in
            if type == .me {
                self?.pkProgressView.updateProgressValue(at: giftModel.coin)
            } else {
                self?.pkProgressView.updateTargetProgressValue(at: giftModel.coin)
            }
        }
    }
    
    private func updatePKUIStatus(isStart: Bool) {
        vsImageView.isHidden = !isStart
        countTimeLabel.isHidden = !isStart
        pkProgressView.isHidden = !isStart
        if currentUserId == "\(UserInfo.userId)" && isStart {
            bottomView.updateButtonType(type: [.tool, .close])
        } else if currentUserId == "\(UserInfo.userId)" && !isStart {
            bottomView.updateButtonType(type: [.pk, .tool, .close])
        } else {
            bottomView.updateButtonType(type: [.gift, .close])
        }
        stopBroadcastButton.isHidden = getRole(uid: "\(UserInfo.userId)") == .audience ? true : !isStart
        if isStart {
            vsImageView.centerYAnchor.constraint(equalTo: view.topAnchor,
                                                 constant: liveCanvasViewHeight).isActive = true
            timer.scheduledSecondsTimer(withName: sceneType.rawValue, timeInterval: 180, queue: .main) { [weak self] _, duration in
                self?.countTimeLabel.text = "".timeFormat(secounds: duration)
                if duration <= 0 {
                    self?.updatePKInfoStatusToEnd()
                }
            }
        } else {
            pkProgressView.reset()
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        let channelName = targetChannelName.isEmpty ? channleName : targetChannelName
        SyncUtil.unsubscribe(id: channelName, className: SYNC_MANAGER_PK_INFO)
        SyncUtil.deleteCollection(id: channelName, className: sceneType.rawValue, delegate: nil)
        deleteSubscribe()
    }
    
    override func didOfflineOfUid(uid: UInt) {
        super.didOfflineOfUid(uid: uid)
        LogUtils.log(message: "pklive leave == \(uid)", level: .info)
        guard let applyModel = pkApplyInfoModel,
              applyModel.userId == "\(uid)" || applyModel.targetUserId == "\(uid)" else { return }
        updatePKInfoStatusToEnd()
    }
    
    private func updatePKInfoStatusToEnd() {
        guard var applyModel = pkApplyInfoModel else { return }
        applyModel.status = .end
        let channelName = targetChannelName.isEmpty ? channleName : targetChannelName
        SyncUtil.updateCollection(id: channelName,
                                  className: sceneType.rawValue,
                                  objectId: applyModel.objectId,
                                  params: JSONObject.toJson(applyModel),
                                  delegate: nil)
        
        guard var pkInfoModel = pkInfoModel else {
            return
        }
        pkInfoModel.status = .end
        SyncUtil.updateCollection(id: channelName,
                                  className: SYNC_MANAGER_PK_INFO,
                                  objectId: pkInfoModel.objectId,
                                  params: JSONObject.toJson(pkInfoModel),
                                  delegate: nil)
    }
    
    private func deleteSubscribe() {
        if !targetChannelName.isEmpty {
            leaveChannel(uid: UserInfo.userId, channelName: targetChannelName)
            SyncUtil.unsubscribe(id: targetChannelName, className: sceneType.rawValue)
            SyncUtil.unsubscribe(id: targetChannelName, className: SYNC_MANAGER_GIFT_INFO)
            SyncUtil.leaveScene(id: targetChannelName)
        }
        timer.destoryTimer(withName: sceneType.rawValue)
    }
    
    override func clickPKHandler() {
        let pkInviteListView = PKLiveInviteView(channelName: channleName, sceneType: sceneType)
        pkInviteListView.pkInviteSubscribe = { [weak self] id in
            guard let self = self else { return }
            self.targetChannelName = id
            // 加入到对方的channel 订阅对方
            SyncUtil.subscribeCollection(id: id,
                                         className: self.sceneType.rawValue,
                                         delegate: PKInviteInfoTargetDelegate(vc: self))
            
            // 订阅对方收到的礼物
            SyncUtil.subscribeCollection(id: id,
                                         className: SYNC_MANAGER_GIFT_INFO,
                                         delegate: LiveGiftDelegate(vc: self, type: .target))
        }
        AlertManager.show(view: pkInviteListView, alertPostion: .bottom)
    }
    
    @objc
    private func clickStopBroadcast() { /// 停止连麦
        showAlert(title: "终止连麦", message: "", cancel: nil) { [weak self] in
            self?.updatePKInfoStatusToEnd()
            self?.stopBroadcastButton.isHidden = true
        }
    }
}
