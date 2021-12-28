//
//  UIView+Extension.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit

extension UIView {
    var isCycle: Bool {
        get {
            return self.bounds.height == self.layer.cornerRadius * 2
        }
        set {
            guard self.bounds.height == self.bounds.width else {
                return
            }
            
            self.layer.cornerRadius = self.bounds.height * 0.5
        }
    }
}
