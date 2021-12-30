//
//  EntryVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit
import AgoraSyncManager

public class EntryVC: UIViewController {
    private var rightBarButtonItem: UIBarButtonItem!
    private let entryView = EntryView()
    private var syncManager: AgoraSyncManager!
    private var sceneRef: SceneReference?
    private var rooms = [RoomItem]()
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
        let image = UIImage(named: "user-setting")
        tabBarController?.navigationItem.rightBarButtonItems = [UIBarButtonItem(image: image,
                                                                                style: .plain,
                                                                                target: self,
                                                                                action: #selector(tapBarButtonItem(_:))),
                                                                UIBarButtonItem(image: .remove,
                                                                                style: .plain,
                                                                                target: self,
                                                                                action: #selector(tapBarButtonItem(_:)))]
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
                .compactMap({ try? decoder.decode(RoomItem.self, from: $0) })
            self?.udpateRooms(rooms: rooms)
            self?.entryView.endRefreshing()
        } fail: { [weak self](error) in
            self?.show(error.description)
            self?.entryView.endRefreshing()
        }
    }
    
    func deleteAllRooms() {
        let keys = rooms.map({ $0.id })
        syncManager.deleteScenes(sceneIds: keys) { [weak self] in
            self?.udpateRooms(rooms: [])
        } fail: { error in
            Log.error(error: error.description, tag: "deleteAllRooms")
        }
    }
    
    func udpateRooms(rooms: [RoomItem]) {
        let infos = rooms.map({ EntryView.Info(imageName: $0.id.headImageName,
                                               title: $0.roomName,
                                               count: 0) })
        self.rooms = rooms
        entryView.update(infos: infos)
    }
    
    func getRoomInfo(index: Int) -> RoomItem? {
        rooms[index]
    }
    
    @objc func tapBarButtonItem(_ barButtonItem: UIBarButtonItem) {
        if tabBarController?.navigationItem.rightBarButtonItem == barButtonItem {
            let vc = MineVC()
            navigationController?.pushViewController(vc, animated: true)
            return
        }
        if tabBarController?.navigationItem.leftBarButtonItem == barButtonItem {
            deleteAllRooms()
        }
    }
}

extension EntryVC: EntryViewDelegate {
    func entryViewdidPull(_ view: EntryView) {
        fetchRooms()
    }
    
    func entryViewDidTapCreateButton(_ view: EntryView) {
        let vc = CreateLiveVC(appId: appId)
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
        let roomId = roomInfo.id
        let roomName = roomInfo.roomName
        let liveModeValue = Int(roomInfo.liveMode)!
        let config = SuperAppAudienceViewController.Config(appId: appId,
                                                           sceneName: roomName,
                                                           sceneId: roomId,
                                                           liveMode: .init(rawValue: liveModeValue)!)
        let vc = SuperAppAudienceViewController(config: config)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
}

extension EntryVC: CreateLiveVCDelegate {
    func createLiveVC(_ vc: CreateLiveVC,
                      didSart roomName: String,
                      sellectedType: CreateLiveVC.SelectedType) {
        /// 作为主播进入
        let createTime = Double(Int(Date().timeIntervalSince1970 * 1000) )
        let roomId = "\(Int(createTime))"
        let mode: SuperAppHostViewController.Mode = sellectedType == .value1 ? .push : .byPassPush
        let config = SuperAppHostViewController.Config(appId: appId,
                                          sceneName: roomName,
                                          sceneId: roomId,
                                          createdTime: createTime,
                                          mode: mode)
        let vc = SuperAppHostViewController(config: config)
        
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
}
