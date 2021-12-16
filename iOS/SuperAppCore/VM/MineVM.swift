//
//  MineVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

protocol MineVMDelegate: NSObjectProtocol {
    func mineVMDidUpdateInfo(_ vm: MineVM,
                             info: MineView.Info)
}

class MineVM: NSObject {
    weak var delegate: MineVMDelegate?
    
    func start() {
        updateInfo()
    }
    
    func updateInfo() {
        let name = StorageManager.userName
        let imageName = StorageManager.uuid.headImageName
        let info = MineView.Info(name: name,
                                 imageName: imageName)
        delegate?.mineVMDidUpdateInfo(self,
                                      info: info)
    }
    
    func udpateName(name: String) {
        StorageManager.userName = name
        updateInfo()
    }
}

