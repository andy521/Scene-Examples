//
//  SpeakerToolbar.swift
//  LiveKtv
//
//  Created by XC on 2021/6/7.
//

import Core
import Foundation
import UIKit

class SpeakerToolbar {
    weak var delegate: RoomController!
    weak var root: UIView!
    weak var micView: UIButton!
    weak var switchMVView: UIButton!
    weak var orderMusicView: UIButton!

    var member: LiveKtvMember? {
        didSet {
            if let member = member {
                switch member.toLiveKtvRoomRole() {
                case .listener:
                    root.isHidden = true
                default:
                    root.isHidden = false
                }
            } else {
                root.isHidden = true
            }
        }
    }

    func subcribeUIEvent() {
        onMuted(mute: delegate.viewModel.muted)
        micView.addTarget(self, action: #selector(onTapMicView), for: .touchUpInside)
        switchMVView.addTarget(self, action: #selector(onTapSwitchMV), for: .touchUpInside)
    }

    @objc func onTapMicView() {
        delegate.viewModel.selfMute(mute: !delegate.viewModel.member.isSelfMuted)
    }

    @objc func onTapSwitchMV() {
        SelectMVDialog().show(delegate: delegate)
    }

    func onMuted(mute: Bool) {
        if !root.isHidden {
            micView.setImage(UIImage(named: mute ? "iconMuted" : "iconVoice", in: Utils.bundle, with: nil), for: .normal)
        }
    }

    deinit {
        Logger.log(self, message: "deinit", level: .info)
    }
}
