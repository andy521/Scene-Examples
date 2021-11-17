//
//  UIViewController+Extension.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/16.
//

import Foundation
import Toast_Swift

extension UIViewController {
    func show(_ text: String) {
        if Thread.current.isMainThread {
            showNoQueue(text: text)
            return
        }
        DispatchQueue.main.sync { [unowned self] in
            self.showNoQueue(text: text)
        }
    }
    
    private func showNoQueue(text: String) {
        self.view.makeToast(text, position: .center)
    }
}
