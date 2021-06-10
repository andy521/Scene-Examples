//
//  RoundImageView.swift
//  BlindDate
//
//  Created by XC on 2021/4/21.
//

import Core
import Foundation
import UIKit

class RoundImageView: UIImageView {
    var color: String? = "#FFFFFF"
    var borderWidth: CGFloat = 1
    var radius: CGFloat?

    override func layoutSubviews() {
        super.layoutSubviews()
        clipsToBounds = true
        rounded(color: !isHidden ? color : nil, borderWidth: borderWidth, radius: radius)
    }
}
