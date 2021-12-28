//
//  CreateLiveVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/16.
//

import UIKit

protocol CreateLiveVCDelegate: NSObjectProtocol {
    func createLiveVC(_ vc: CreateLiveVC,
                      didSart roomName: String,
                      sellectedType: CreateLiveVC.SelectedType)
}

class CreateLiveVC: UIViewController {
    let createLiveView = CreateLiveView(frame: .zero)
    var vm: CreateLiveVM!
    weak var delegate: CreateLiveVCDelegate?
    
    public init(appId: String) {
        self.vm = CreateLiveVM(appId: appId)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    private func setup() {
        view.addSubview(createLiveView)
        createLiveView.frame = view.bounds
    }
    
    private func commonInit() {
        vm.delegate = self
        vm.startRenderLocalVideoStream(view: createLiveView.cameraPreview)
        createLiveView.delegate = self
        vm.genRandomName()
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
}

extension CreateLiveVC: CreateLiveViewDelegate {
    func createLiveViewDidTapCloseButton(_ view: CreateLiveView) {
        dismiss(animated: true, completion: nil)
    }
    
    func createLiveViewDidTapCameraButton(_ view: CreateLiveView) {
        vm.switchCamera()
    }
    
    func createLiveViewDidTapStartButton(_ view: CreateLiveView) {
        vm.destory()
        let text = createLiveView.text
        let sellectedType: SelectedType = createLiveView.currentSelectedType == .value1 ? .value1 : .value2
        dismiss(animated: true) { [weak self] in
            if self != nil {
                
                self?.delegate?.createLiveVC(self!,
                                             didSart: text,
                                             sellectedType: sellectedType)
            }
        }
    }
    
    func createLiveViewDidTapRandomButton(_ view: CreateLiveView) {
        vm.genRandomName()
    }
}

extension CreateLiveVC: CreateLiveVMDelegate {
    func createLiveVM(_ vm: CreateLiveVM, didUpdate roomName: String) {
        createLiveView.set(text: roomName)
    }
}

extension CreateLiveVC {
    enum SelectedType {
        case value1
        case value2
    }
}
