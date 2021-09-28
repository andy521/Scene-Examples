//
//  MainVC.swift
//  LiveMutiHostCore
//
//  Created by ZYP on 2021/9/28.
//

import UIKit

class MainVC: UIViewController {
    var vm: MainVM!
    let mainView = MainView()
    var roomName: String!
    var renderInfos = [RenderInfo]()
    var closeItem: UIBarButtonItem!
    var pkItem: UIBarButtonItem!
    var exitPkItem: UIBarButtonItem!
    
    init(roomName: String, appId: String) {
        super.init(nibName: nil, bundle: nil)
        self.vm = MainVM(roomName: roomName, appId: appId)
        self.roomName = roomName
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setup() {
        title = roomName
        view.addSubview(mainView)
        mainView.translatesAutoresizingMaskIntoConstraints = false
        mainView.leftAnchor.constraint(equalTo: view.leftAnchor).isActive = true
        mainView.rightAnchor.constraint(equalTo: view.rightAnchor).isActive = true
        mainView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        mainView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
    
    func commonInit() {
        mainView.collectionView.dataSource = self
        mainView.collectionView.delegate = self
        mainView.collectionView.register(VideoCell.self, forCellWithReuseIdentifier: "cell")
        vm.delegate = self
        vm.start()
    }
    
    @objc func closeOnClick() {
        vm.leave()
        dismiss(animated: true, completion: nil)
    }
    
    @objc func quickOnClick() {
        vm.removeMe()
    }
}

extension MainVC: UICollectionViewDataSource, UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return renderInfos.count
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! VideoCell
        let info = renderInfos[indexPath.row]
        if vm.shouldRenderVideo(uid: info.uid) {
            cell.set(style: .video)
            let videoView = cell.videoView
            info.isLocal ? vm.subscribeVideoLocal(view: videoView) : vm.subscribeVideoRemote(view: videoView, uid: info.uid)
        }
        else {
            cell.set(style: .tap)
        }
        return cell
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        vm.addMe(indexPath: indexPath)
    }
}

extension MainVC: MainVMDelegate {
    func mainVMShouldShowTips(tips: String) {
        show(tips)
    }
    
    func mainVMDidUpdateRenderInfos(renders: [RenderInfo]) {
        self.renderInfos = renders
        mainView.collectionView.reloadData()
        
        let closeItem = UIBarButtonItem(title: "Close", style: .plain, target: self, action: #selector(closeOnClick))
        let quictItem = UIBarButtonItem(title: "Quick", style: .plain, target: self, action: #selector(quickOnClick))
        navigationItem.rightBarButtonItems = (vm.currentIndexPath != nil) ? [closeItem, quictItem] : [closeItem]
    }
}
