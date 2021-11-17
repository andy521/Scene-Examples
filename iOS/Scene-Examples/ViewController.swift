//
//  ViewController.swift
//  Scene-Examples
//
//  Created by XC on 2021/4/16.
//

import UIKit
import RxCocoa
import RxSwift
import Core
import LivePKCore
import LiveMutiCore
import SuperAppCore

class ViewController: CustomTabBarController {
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    override func setupView() {
        let livePKEntryVC = SuperAppCore.EntryVC(appId: BuildConfig.AppId)
        let LiveMutiEntryVC = LiveMutiCore.EntryVC()
        LiveMutiEntryVC.appId = BuildConfig.AppId
        viewControllers = [
            livePKEntryVC,
            UIViewController(),
            UIViewController(),
            SettingController.instance(),
            LiveMutiEntryVC
        ]
        setTabBar(items: [
            CustomTabBarItem(icon: UIImage.strokedCheckmark, title: "All".localized),
            CustomTabBarItem(icon: UIImage(systemName: "music.mic")!, title: "Podcast".localized) {
                AppTargets.getAppMainViewController(app: .InteractivePodcast)
            },
            CustomTabBarItem(icon: UIImage(systemName: "video")!, title: "Dating".localized) {
                AppTargets.getAppMainViewController(app: .BlindDate)
            },
            CustomTabBarItem(icon: UIImage.actions, title: "Settings".localized),
            CustomTabBarItem(icon: UIImage.actions, title: "MutiHost".localized)
        ])
    }
}

