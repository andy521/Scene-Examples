//
//  AboutController.swift
//  InteractivePodcast
//
//  Created by XC on 2021/4/8.
//

import Core
import Foundation
import RxCocoa
import RxSwift
import UIKit

class AboutController: BaseViewContoller {
    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var itemView0: UIView!
    @IBOutlet var itemView1: UIView!
    @IBOutlet var itemView2: UIView!

    @IBOutlet var publishTimeView: UILabel!
    @IBOutlet var sdkVersionView: UILabel!
    @IBOutlet var appVersionView: UILabel!

    @IBOutlet var backButton: UIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        scrollView.alwaysBounceVertical = true
        let tapItem0 = UITapGestureRecognizer()
        itemView0.addGestureRecognizer(tapItem0)
        tapItem0.rx.event
            .subscribe(onNext: { _ in
                if let url = URL(string: BuildConfig.PrivacyPolicy) {
                    UIApplication.shared.open(url)
                }
            })
            .disposed(by: disposeBag)

        let tapItem1 = UITapGestureRecognizer()
        itemView1.addGestureRecognizer(tapItem1)
        tapItem1.rx.event
            .subscribe(onNext: { _ in
                self.navigationController?.pushViewController(DisclaimerController.instance(), animated: true)
            })
            .disposed(by: disposeBag)

        let tapItem2 = UITapGestureRecognizer()
        itemView2.addGestureRecognizer(tapItem2)
        tapItem2.rx.event
            .subscribe(onNext: { _ in
                if let url = URL(string: BuildConfig.SignupUrl) {
                    UIApplication.shared.open(url)
                }
            })
            .disposed(by: disposeBag)

        publishTimeView.text = BuildConfig.PublishTime
        sdkVersionView.text = BuildConfig.SdkVersion
        appVersionView.text = BuildConfig.AppVersion

        let tapBack = UITapGestureRecognizer()
        backButton.addGestureRecognizer(tapBack)
        tapBack.rx.event
            .subscribe(onNext: { _ in
                self.navigationController?.popViewController(animated: true)
            })
            .disposed(by: disposeBag)
    }

    static func instance() -> AboutController {
        let storyBoard = UIStoryboard(name: "Main", bundle: Utils.bundle)
        let controller = storyBoard.instantiateViewController(withIdentifier: "AboutController") as! AboutController
        return controller
    }
}
