//
//  EntryVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit

public class EntryVC: UIViewController {
    var rightBarButtonItem: UIBarButtonItem!
    let entryView = EntryView(frame: .zero)
    var vm: EntryVM!
    let appId: String
    
    public init(appId: String) {
        self.appId = appId
        self.vm = EntryVM(appId: appId)
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
    
    private func setup() {
        let image = UIImage(named: "user-setting")
        tabBarController?.navigationItem.rightBarButtonItem = UIBarButtonItem(image: image,
                                                                              style: .plain,
                                                                              target: self,
                                                                              action: #selector(tapBarButtonItem(_:)))
        
        entryView.frame = view.bounds
        view.addSubview(entryView)
    }
    
    private func commonInit() {
        vm.delegate = self
        entryView.delegate = self
    }
    
    @objc func tapBarButtonItem(_ barButtonItem: UIBarButtonItem) {
        let vc = MineVC()
        navigationController?.pushViewController(vc, animated: true)
    }
}

extension EntryVC: EntryViewDelegate, EntryVMDelegate {
    func entryViewdidPull(_ view: EntryView) {
        vm.getRooms()
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
        guard let roomInfo = vm.getRoomInfo(index: index) else {
            return
        }
        let roomId = roomInfo.id
        let roomName = roomInfo.roomName
        let config = MainVMAudience.Config(appId: appId,
                                           roomName: roomName,
                                           roomId: roomId)
        let vc = MainVC(config: config,
                        syncManager: vm.syncManager)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
    
    func entryVMShouldUpdateInfos(infos: [EntryView.Info]) {
        entryView.update(infos: infos)
    }
    
    func entryVMShouldShowTip(msg: String) {
        Log.info(text: msg, tag: "EntryVC")
        show(msg)
    }
    
    func entryVMShouldEndRefreshing() {
        entryView.endRefreshing()
    }
}

extension EntryVC: CreateLiveVCDelegate {
    func createLiveVC(_ vc: CreateLiveVC,
                      didSart roomName: String,
                      sellectedType: CreateLiveVC.SelectedType) {
        let createTime = Double(Int(Date().timeIntervalSince1970 * 1000) )
        let roomId = "\(Int(createTime))"
        let mode: MainVMHost.Mode = sellectedType == .value1 ? .push : .byPassPush
        let config = MainVMHost.Config(appId: appId,
                                       roomName: roomName,
                                       roomId: roomId,
                                       createdTime: createTime,
                                       mode: mode)
        let vc = MainVC(config: config,
                        syncManager: vm.syncManager)
        vc.modalPresentationStyle = .fullScreen
        present(vc, animated: true, completion: nil)
    }
}
