//
//  ToolVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/18.
//

import UIKit
import Presentr

protocol ToolVCDelegate: NSObjectProtocol {
    func toolVC(_ vc: ToolVC, didTap action: ToolVC.Action)
}

class ToolVC: UIViewController {
    typealias Action = ToolView.Action
    private let toolView = ToolView()
    private let presenter = Presentr(presentationType: .bottomHalf)
    weak var delegate: ToolVCDelegate?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    private func setup() {
        view.backgroundColor = .white
        view.addSubview(toolView)
        toolView.frame = view.bounds
    }
    
    private func commonInit() {
        toolView.delegate = self
    }

    func show(in vc: UIViewController) {
        presenter.backgroundTap = .dismiss
        vc.customPresentViewController(presenter,
                                       viewController: self,
                                       animated: true,
                                       completion: nil)
    }
}

extension ToolVC: ToolViewDelegate {
    func toolView(_ view: ToolView, didTap action: ToolView.Action) {
        delegate?.toolVC(self, didTap: action)
    }
}
