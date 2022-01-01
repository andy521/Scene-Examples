//
//  ViewController.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2021/11/10.
//

import UIKit

class MainViewController: BaseViewController {
    private lazy var tableView: BaseTableViewLayout = {
        let view = BaseTableViewLayout()
        view.estimatedRowHeight = 100
        view.delegate = self
        view.register(MainTableViewCell.self,
                      forCellWithReuseIdentifier: MainTableViewCell.description())
        view.dataArray = MainModel.mainDatas()
        return view
    }()
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "首页"
        setupUI()
    }
    
    private func setupUI() {
        view.addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        tableView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
}

extension MainViewController: BaseTableViewLayoutDelegate {
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: MainTableViewCell.description(),
                                                 for: indexPath) as! MainTableViewCell
        cell.setupData(model: MainModel.mainDatas()[indexPath.row])
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let sceneType = MainModel.mainDatas()[indexPath.row].sceneType
        
        switch sceneType {
        case .singleLive:
            SyncUtil.initSyncManager(sceneId: sceneType.rawValue)
            let roomListVC = LiveRoomListController(sceneType: sceneType)
            roomListVC.title = MainModel.mainDatas()[indexPath.row].title
            navigationController?.pushViewController(roomListVC, animated: true)
            break
        case .superApp:
            let vc = SuperAppRoomListViewController(appId: KeyCenter.AppId)
            vc.title = MainModel.mainDatas()[indexPath.row].title
            navigationController?.pushViewController(vc, animated: true)
            break
        }
        
        
    }
    
    func pullToRefreshHandler() {
        
    }
}
