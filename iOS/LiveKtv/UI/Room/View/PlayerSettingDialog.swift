//
//  PlayerSettingDialog.swift
//  LiveKtv
//
//  Created by XC on 2021/6/10.
//

import Core
import Foundation
import UIKit

struct PlayerSettingDialogParam {
    let ear: Bool
    let volume0: Int
    let volume1: Int
}

class PlayerSettingDialog: Dialog {
    weak var delegate: RoomController!

    var param: PlayerSettingDialogParam! {
        didSet {}
    }

    var titleView: UILabel = {
        let view = UILabel()
        view.font = UIFont.systemFont(ofSize: 14)
        view.numberOfLines = 1
        view.textColor = UIColor(hex: Colors.DialogTitle)
        view.text = "参数设置"
        return view
    }()

    var label0: UILabel = {
        let view = UILabel()
        view.font = UIFont.systemFont(ofSize: 14)
        view.numberOfLines = 1
        view.textColor = UIColor(hex: Colors.Text).withAlphaComponent(0.5)
        view.text = "耳返"
        return view
    }()

    var switcher: UISwitch = {
        let view = UISwitch()
        view.onTintColor = UIColor(hex: Colors.Blue)
        return view
    }()

    var label1: UILabel = {
        let view = UILabel()
        view.font = UIFont.systemFont(ofSize: 14)
        view.numberOfLines = 1
        view.textColor = UIColor(hex: Colors.Text).withAlphaComponent(0.5)
        view.text = "人声音量"
        return view
    }()

    var slider1: UISlider = {
        let view = UISlider()
        return view
    }()

    var label2: UILabel = {
        let view = UILabel()
        view.font = UIFont.systemFont(ofSize: 14)
        view.numberOfLines = 1
        view.textColor = UIColor(hex: Colors.Text).withAlphaComponent(0.5)
        view.text = "伴奏音量"
        return view
    }()

    var slider2: UISlider = {
        let view = UISlider()
        return view
    }()

    override func setup() {
        backgroundColor = UIColor(hex: Colors.Primary)

        addSubview(titleView)

        addSubview(label0)
        addSubview(switcher)

        addSubview(label1)
        addSubview(slider1)

        addSubview(label2)
        addSubview(slider2)

        titleView.marginTop(anchor: topAnchor, constant: 16)
            .marginLeading(anchor: leadingAnchor, constant: 15, relation: .greaterOrEqual)
            .centerX(anchor: centerXAnchor)
            .active()

        label0.marginTop(anchor: titleView.bottomAnchor, constant: 16)
            .marginLeading(anchor: leadingAnchor, constant: 15)
            .active()
        switcher.centerY(anchor: label0.centerYAnchor)
            .marginLeading(anchor: label0.trailingAnchor, constant: 6)
            .active()

        label1.marginTop(anchor: label0.bottomAnchor, constant: 26)
            .marginLeading(anchor: leadingAnchor, constant: 15)
            .active()
        slider1.centerY(anchor: label1.centerYAnchor)
            .marginLeading(anchor: label1.trailingAnchor, constant: 6)
            .marginTrailing(anchor: trailingAnchor, constant: 15)
            .active()

        label2.marginTop(anchor: label1.bottomAnchor, constant: 26)
            .marginBottom(anchor: safeAreaLayoutGuide.bottomAnchor, constant: 46)
            .marginLeading(anchor: leadingAnchor, constant: 15)
            .active()
        slider2.centerY(anchor: label2.centerYAnchor)
            .marginLeading(anchor: label2.trailingAnchor, constant: 6)
            .marginTrailing(anchor: trailingAnchor, constant: 15)
            .active()
    }

    override func render() {
        roundCorners([.topLeft, .topRight], radius: 10)
        shadow()
    }

    func show(with param: PlayerSettingDialogParam, delegate: RoomController) {
        self.delegate = delegate
        self.param = param
        show(controller: delegate)
    }
}
