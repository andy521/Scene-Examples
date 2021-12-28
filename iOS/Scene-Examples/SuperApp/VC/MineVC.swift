//
//  MineVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit

class MineVC: UIViewController {
    let mineView = MineView(frame: .zero)
    let vm = MineVM()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    func setup() {
        view.addSubview(mineView)
        mineView.frame = view.bounds
        mineView.delegate = self
    }
    
    func commonInit() {
        vm.delegate = self
        vm.start()
    }
    
    func showAlertVC() {
        let alertVC = UIAlertController(title: "名称", message: nil, preferredStyle: .alert)
        let action1 = UIAlertAction(title: "取消", style: .cancel, handler: nil)
        let action2 = UIAlertAction(title: "确定", style: .default) { _ in
            if let text = alertVC.textFields?.first?.text {
                self.vm.udpateName(name: text)
            }
        }
        alertVC.addTextField { textField in
            textField.placeholder = "输入名称"
            textField.text = StorageManager.userName
        }
        alertVC.addAction(action1)
        alertVC.addAction(action2)
        present(alertVC, animated: true, completion: nil)
    }
    
}

extension MineVC: MineVMDelegate {
    func mineVMDidUpdateInfo(_ vm: MineVM,
                             info: MineView.Info) {
        mineView.update(info: info)
    }
}

extension MineVC: MineViewDelegate {
    func mineViewDidTapChangeName (_ view: MineView) {
        showAlertVC()
    }
}
