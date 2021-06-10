//
//  Target.swift
//  Scene-Examples
//
//  Created by XC on 2021/5/20.
//

import BlindDate
import Core
import Foundation
import InteractivePodcast
import LiveKtv
import UIKit

enum SceneApp {
    case InteractivePodcast
    case BlindDate
    case LiveKtv
}

protocol IAppTarget {
    func initTarget() -> Void
    func getAppMainViewController(app: SceneApp) -> UIViewController
}

#if LEANCLOUD
    class LeanCloudAppTarget: IAppTarget {
        func initTarget() {
            Database.initConfig()
            _ = InjectionService.shared
                .register(ISyncManager.self, instance: LeanCloudSyncManager())
                .register(IUserManager.self, instance: LeanCloudUserManager())
                .register(IPodcastModelManager.self, instance: LeanCloudPodcastModelManager())
                .register(IBlindDateModelManager.self, instance: LeanCloudBlindDateModelManager())
        }

        func getAppMainViewController(app: SceneApp) -> UIViewController {
            switch app {
            case .InteractivePodcast:
                return InteractivePodcast.HomeController.instance()
            case .BlindDate:
                return BlindDate.BlindDateHomeController.instance()
            case .LiveKtv:
                return LiveKtv.LiveKtvHomeController.instance()
            }
        }
    }

#elseif FIREBASE
    class FirebaseAppTarget: IAppTarget {
        func initTarget() {
            Database.initConfig()
            _ = InjectionService.shared
                .register(ISyncManager.self, instance: FirebaseSyncManager())
                .register(IUserManager.self, instance: FirebaseUserManager())
                .register(IPodcastModelManager.self, instance: FirebasePodcastModelManager())
                .register(IBlindDateModelManager.self, instance: FirebaseBlindDateModelManager())
        }

        func getAppMainViewController(app: SceneApp) -> UIViewController {
            switch app {
            case .InteractivePodcast:
                return InteractivePodcast.HomeController.instance()
            case .BlindDate:
                return BlindDate.BlindDateHomeController.instance()
            case .LiveKtv:
                return LiveKtv.LiveKtvHomeController.instance()
            }
        }
    }
#endif

enum AppTargets {
    #if LEANCLOUD
        private static let target: IAppTarget = LeanCloudAppTarget()
    #elseif FIREBASE
        private static let target: IAppTarget = FirebaseAppTarget()
    #endif

    static func initTarget() {
        target.initTarget()
    }

    static func getAppMainViewController(app: SceneApp) -> UIViewController {
        target.getAppMainViewController(app: app)
    }
}
