//
//  ViewController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit
import AgoraUIKit_iOS

class MainViewController: BaseViewController, FUPoseTrackViewDelegate {
    private lazy var tableView: AGETableView = {
        let view = AGETableView()
        view.estimatedRowHeight = 100
        view.delegate = self
        view.register(MainTableViewCell.self,
                      forCellWithReuseIdentifier: MainTableViewCell.description())
        view.dataArray = MainModel.mainDatas()
        return view
    }()
    let versionLabel = UILabel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "首页"
        setupUI()
        
        SyncUtil.initSyncManager(sceneId: SceneType.oneToOne.rawValue)
        SyncUtil.fetchAll(success: nil, fail: nil)
    }
    
    private func setupUI() {
        versionLabel.textColor = .gray
        versionLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(tableView)
        view.addSubview(versionLabel)
        
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -40).isActive = true
        
        versionLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        versionLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        versionLabel.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -10).isActive = true
        versionLabel.centerXAnchor.constraint(equalTo: tableView.centerXAnchor).isActive = true
        
        let dict = Bundle.main.infoDictionary
        let version = dict!["CFBundleShortVersionString"] as! String
        let build = dict!["CFBundleVersion"] as! String
        versionLabel.text = "\(version)(\(build))"
        versionLabel.textAlignment = .center
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
    }
}

extension MainViewController: AGETableViewDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: MainTableViewCell.description(),
                                                 for: indexPath) as! MainTableViewCell
        cell.setupData(model: MainModel.mainDatas()[indexPath.row])
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let sceneType = MainModel.mainDatas()[indexPath.row].sceneType
        
        if sceneType == .breakoutRoom {
            let breakoutRoomVC = BORHomeViewController()
            breakoutRoomVC.title = MainModel.mainDatas()[indexPath.row].title
            navigationController?.pushViewController(breakoutRoomVC, animated: true)
        } else {
            
            let roomListVC = LiveRoomListController(sceneType: sceneType)
            roomListVC.title = MainModel.mainDatas()[indexPath.row].title
            navigationController?.pushViewController(roomListVC, animated: true)
        }
    }
    
    func pullToRefreshHandler() {
        
    }
    
}
