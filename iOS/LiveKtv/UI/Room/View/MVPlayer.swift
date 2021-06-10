//
//  MVPlayer.swift
//  LiveKtv
//
//  Created by XC on 2021/6/7.
//

import Core
import Foundation
import UIKit

class MVPlayer {
    enum Status {
        case stop
        case play
        case pause
    }

    weak var delegate: RoomController!
    weak var player: UIView!
    weak var mv: UIImageView!
    weak var originSettingView: UISwitch!
    weak var settingsView: UIButton!
    weak var playerControlView: UIButton!
    weak var switchMusicView: UIButton!

    var member: LiveKtvMember? {
        didSet {
            onChange()
        }
    }

    var status: Status! {
        didSet {
            onChange()
        }
    }

    var stopView: UIView = {
        let view = UIView()
        let icon = UIImageView()
        icon.image = UIImage(named: "empty", in: Utils.bundle, with: nil)
        let tips1 = UILabel()
        tips1.font = UIFont.systemFont(ofSize: 14, weight: .medium)
        tips1.textColor = UIColor(hex: Colors.Text)
        tips1.numberOfLines = 0
        tips1.textAlignment = .center
        tips1.text = "当前无人演唱"
        let tips2 = UILabel()
        tips2.font = UIFont.systemFont(ofSize: 14, weight: .medium)
        tips2.textColor = UIColor(hex: Colors.Text)
        tips2.numberOfLines = 0
        tips2.textAlignment = .center
        tips2.text = "点击“点歌”一展歌喉"

        view.addSubview(icon)
        view.addSubview(tips1)
        view.addSubview(tips2)

        icon.width(constant: 117)
            .height(constant: 117)
            .marginTop(anchor: view.topAnchor, constant: 50)
            .centerX(anchor: view.centerXAnchor)
            .active()
        tips1.marginTop(anchor: icon.bottomAnchor, constant: 16)
            .marginLeading(anchor: view.leadingAnchor, constant: 15)
            .centerX(anchor: view.centerXAnchor)
            .active()
        tips2.marginTop(anchor: tips1.bottomAnchor, constant: 8)
            .marginLeading(anchor: view.leadingAnchor, constant: 15)
            .centerX(anchor: view.centerXAnchor)
            .active()

        return view
    }()

    func onChange() {
        switch status {
        case .stop, .none:
            settingsView.superview?.isHidden = true
            mv.isHidden = true
            if stopView.superview == nil {
                player.superview!.addSubview(stopView)
                stopView.fill(view: player.superview!)
                    .active()
            }
            stopView.isHidden = false
        case .play:
            stopView.isHidden = true
            if let member = member {
                switch member.toLiveKtvRoomRole() {
                case .listener:
                    settingsView.superview?.isHidden = true
                default:
                    settingsView.superview?.isHidden = false
                }
            } else {
                settingsView.superview?.isHidden = true
            }
        case .pause:
            stopView.isHidden = true
            if let member = member {
                switch member.toLiveKtvRoomRole() {
                case .listener:
                    settingsView.superview?.isHidden = true
                default:
                    settingsView.superview?.isHidden = false
                }
            } else {
                settingsView.superview?.isHidden = true
            }
        }
    }

    func subcribeUIEvent() {
        settingsView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(onTapSettingsView)))
    }

    @objc func onTapSettingsView() {
        let param = PlayerSettingDialogParam(ear: false, volume0: 50, volume1: 50)
        PlayerSettingDialog().show(with: param, delegate: delegate)
    }

    deinit {
        Logger.log(self, message: "deinit", level: .info)
    }
}
