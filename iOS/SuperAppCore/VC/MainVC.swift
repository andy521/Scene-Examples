//
//  MainVC.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/17.
//

import UIKit

class MainVC: UIViewController {
    let mainView = MainView()
    
    public init(appId: String) {
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    private func setup() {
        view.addSubview(mainView)
        mainView.frame = view.bounds
    }
    
    private func commonInit() {
        
    }
}
