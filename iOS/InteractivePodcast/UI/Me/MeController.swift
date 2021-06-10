//
//  MeController.swift
//  InteractivePodcast
//
//  Created by XUCH on 2021/3/6.
//

import Core
import Foundation
import RxCocoa
import RxSwift
import UIKit

class MeController: BaseViewContoller {
    @IBOutlet var avatarView: UIImageView!
    @IBOutlet var nameView: UILabel!
    @IBOutlet var nickNameView: UILabel!
    @IBOutlet var backButton: UIView!
    @IBOutlet var setNameView: UIView!
    @IBOutlet var aboutView: UIView!
    @IBOutlet var audienceLatencyLevelView: UISwitch!

    private var account: User = RoomManager.shared().account!
    private var setting: LocalSetting = RoomManager.shared().setting

    override func viewDidLoad() {
        super.viewDidLoad()
        nameView.text = account.name
        nickNameView.text = account.name
        avatarView.image = UIImage(named: account.getLocalAvatar(), in: Utils.bundle, with: nil)
        audienceLatencyLevelView.setOn(setting.audienceLatency, animated: false)

        let tapBack = UITapGestureRecognizer()
        backButton.addGestureRecognizer(tapBack)
        tapBack.rx.event
            .subscribe(onNext: { _ in
                self.navigationController?.popViewController(animated: true)
            })
            .disposed(by: disposeBag)

        let tapSetName = UITapGestureRecognizer()
        setNameView.addGestureRecognizer(tapSetName)
        tapSetName.rx.event
            .subscribe(onNext: { _ in
                self.navigationController?.pushViewController(ChangeNameController.instance(), animated: true)
            })
            .disposed(by: disposeBag)

        let tapAbout = UITapGestureRecognizer()
        aboutView.addGestureRecognizer(tapAbout)
        tapAbout.rx.event
            .subscribe(onNext: { _ in
                self.navigationController?.pushViewController(AboutController.instance(), animated: true)
            })
            .disposed(by: disposeBag)

        audienceLatencyLevelView.rx.isOn
            .asObservable()
            .filter { isOn -> Bool in
                isOn != self.setting.audienceLatency
            }
            .flatMap { isOn -> Observable<Result<LocalSetting>> in
                self.setting.audienceLatency = isOn
                return AppDataManager.saveSetting(setting: self.setting)
            }
            .subscribe(onNext: { result in
                if !result.success {
                    self.show(message: result.message ?? "unknown error".localized, type: .error)
                } else {
                    RoomManager.shared().updateSetting()
                }
            })
            .disposed(by: disposeBag)
    }

    override func viewDidAppear(_: Bool) {
        nameView.text = account.name
        nickNameView.text = account.name
    }

    static func instance() -> MeController {
        let storyBoard = UIStoryboard(name: "Main", bundle: Utils.bundle)
        let controller = storyBoard.instantiateViewController(withIdentifier: "MeController") as! MeController
        return controller
    }
}
