//
//  HomeController.swift
//  Scene-Examples
//
//  Created by XC on 2021/4/19.
//

import Foundation
import LiveKtv
import UIKit

class InteractivePodcastCard: HomeCard {
    var title: String = "Interactive Podcast".localized
    var color = UIColor(hex: Colors.Blue)

    func create() -> UIViewController {
        AppTargets.getAppMainViewController(app: .InteractivePodcast)
    }
}

class InteractiveLiveDatingCard: HomeCard {
    var title: String = "Interactive Live Dating".localized
    var color = UIColor(hex: Colors.LightBLue)

    func create() -> UIViewController {
        AppTargets.getAppMainViewController(app: .BlindDate)
    }
}

class LiveKtvCard: HomeCard {
    var title: String = "Live KTV".localized
    var color = UIColor(hex: Colors.Blue)

    func create() -> UIViewController {
        AppTargets.getAppMainViewController(app: .LiveKtv)
    }
}

class HomeController: UITableViewController {
    var listData: [HomeCard] = [
        InteractivePodcastCard(),
        InteractiveLiveDatingCard(),
        LiveKtvCard(),
    ]

    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.backgroundColor = .white
        tableView.rowHeight = 200
        tableView.separatorStyle = .none
        tableView.register(HomeCardView.self, forCellReuseIdentifier: NSStringFromClass(HomeCardView.self))
    }

    static func instance() -> HomeController {
        let storyBoard = UIStoryboard(name: "Main", bundle: nil)
        let controller = storyBoard.instantiateViewController(withIdentifier: "HomeController") as! HomeController
        return controller
    }

    override func tableView(_: UITableView, numberOfRowsInSection _: Int) -> Int {
        return listData.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let identifier = NSStringFromClass(HomeCardView.self)
        let cell = tableView.dequeueReusableCell(withIdentifier: identifier, for: indexPath) as! HomeCardView
        cell.item = listData[indexPath.row]
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = listData[indexPath.row]
        navigationController?.pushViewController(item.create(), animated: true)
    }
}
