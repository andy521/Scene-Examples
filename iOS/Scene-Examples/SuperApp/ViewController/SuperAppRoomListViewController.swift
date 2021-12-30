//
//  SuperAppRoomListViewController.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit
import AgoraSyncManager

public class SuperAppRoomListViewController: UIViewController {
    private var rightBarButtonItem: UIBarButtonItem!
    private let entryView = EntryView()
    private var syncManager: AgoraSyncManager!
    private var sceneRef: SceneReference?
    private var rooms = [SuperAppRoomInfo]()
    private let appId: String
    private let defaultChannelName = "PKByCDN"
    
    public init(appId: String) {
        self.appId = appId
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        fetchRooms()
    }
    
    private func setup() {
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "删除所有房间",
                                                            style: .plain,
                                                            target: self,
                                                            action: #selector(deleteAllRooms))
        entryView.frame = view.bounds
        view.addSubview(entryView)
    }
    
    private func commonInit() {
        entryView.delegate = self
        let config = AgoraSyncManager.RtmConfig.init(appId: appId,
                                                     channelName: defaultChannelName)
        self.syncManager = AgoraSyncManager(config: config,
                                            complete: { [weak self](code) in
            self?.fetchRooms()
        })
    }
    
    func fetchRooms() {
        syncManager.getScenes { [weak self](objs) in
            let decoder = JSONDecoder()
            let rooms = objs.compactMap({ $0.toJson()?.data(using: .utf8) })
                .compactMap({ try? decoder.decode(SuperAppRoomInfo.self, from: $0) })
            self?.udpateRooms(rooms: rooms)
            self?.entryView.endRefreshing()
        } fail: { [weak self](error) in
            self?.show(error.description)
            self?.entryView.endRefreshing()
        }
    }
    
    @objc func deleteAllRooms() {
        let keys = rooms.map({ $0.roomId })
        syncManager.deleteScenes(sceneIds: keys) { [weak self] in
            self?.udpateRooms(rooms: [])
        } fail: { error in
            LogUtils.logError(message: error.description, tag: "deleteAllRooms")
        }
    }
    
    func udpateRooms(rooms: [SuperAppRoomInfo]) {
        let infos = rooms.map({ EntryView.Info(imageName: $0.roomId.headImageName,
                                               title: $0.roomName,
                                               count: 0) })
        self.rooms = rooms
        entryView.update(infos: infos)
    }
    
    func getRoomInfo(index: Int) -> SuperAppRoomInfo? {
        rooms[index]
    }
}

extension SuperAppRoomListViewController: EntryViewDelegate {
    func entryViewdidPull(_ view: EntryView) {
        fetchRooms()
    }
    
    func entryViewDidTapCreateButton(_ view: EntryView) {
        let vc = SuperAppCreateLiveViewController(appId: appId)
        vc.delegate = self
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
    
    func entryView(_ view: EntryView,
                   didSelected info: EntryView.Info,
                   at index: Int) {
        guard let roomInfo = getRoomInfo(index: index) else {
            return
        }
        /// 作为观众进入
        let config = SuperAppAudienceViewController.Config(appId: appId,
                                                           roomInfo: roomInfo)
        let vc = SuperAppAudienceViewController(config: config)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
}

extension SuperAppRoomListViewController: CreateLiveVCDelegate {
    func createLiveVC(_ vc: SuperAppCreateLiveViewController,
                      didSart roomName: String,
                      sellectedType: SuperAppCreateLiveViewController.SelectedType) {
        /// 作为主播进入
        let createTime = Double(Int(Date().timeIntervalSince1970 * 1000) )
        let roomId = "\(Int(createTime))"
        let liveMode: LiveMode = sellectedType == .value1 ? .push : .byPassPush
        let roomItem = SuperAppRoomInfo(roomId: roomId, roomName: roomName, liveMode: liveMode)
        
        let config = SuperAppHostViewController.Config(appId: appId,
                                                       roomItem: roomItem)
        let vc = SuperAppHostViewController(config: config)
        
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
}
