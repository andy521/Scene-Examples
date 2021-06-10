//
//  RoundLabelView.swift
//  BlindDate
//
//  Created by XC on 2021/4/22.
//

import Core
import Foundation
import UIKit

class RoundLabelView: UILabel {
    override func layoutSubviews() {
        super.layoutSubviews()
        widthAnchor.constraint(greaterThanOrEqualToConstant: bounds.height).isActive = text?.isEmpty == false
        clipsToBounds = true
        rounded()
    }
}
