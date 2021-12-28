//
//  MineView.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/12.
//

import UIKit

protocol MineViewDelegate: NSObjectProtocol {
    func mineViewDidTapChangeName (_ view: MineView)
}

class MineView: UIView {
    let topView = TopView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.size.width, height: 220))
    let tableView = UITableView(frame: .zero, style: .grouped)
    var info: Info = .empty
    weak var delegate: MineViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setup() {
        tableView.tableHeaderView = topView
        addSubview(tableView)
        
        tableView.translatesAutoresizingMaskIntoConstraints = false
        
        tableView.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        tableView.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor).isActive = true
    }
    
    private func commonInit() {
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    func update(info: Info) {
        self.info = info
        topView.label.text = info.name
        topView.imageView.image = UIImage(named: info.imageName)
        tableView.reloadData()
    }
}
// MARK: - UITableViewDataSource & UITableViewDelegate
extension MineView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .value1, reuseIdentifier: "UITableViewCell")
        cell.textLabel?.text = "设置昵称"
        cell.detailTextLabel?.text = info.name
        cell.accessoryType = .disclosureIndicator
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return .init()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        delegate?.mineViewDidTapChangeName(self)
    }
}

extension MineView {
    class TopView: UIImageView {
        var imageView: UIImageView
        var label: UILabel
        
        override init(frame: CGRect) {
            let image = UIImageView(frame: CGRect.zero)
            image.layer.masksToBounds = true
            image.contentMode = .scaleAspectFit
            imageView = image
            
            label = UILabel(frame: CGRect.zero)
            label.font = UIFont.systemFont(ofSize: 17, weight: .medium)
            label.textColor = .white
            label.textAlignment = .center
            
            super.init(frame: frame)
            addSubview(label)
            addSubview(image)
            self.image = UIImage(named: "pic-BG")
        }
        
        override func layoutSubviews() {
            super.layoutSubviews()
            
            let imageViewWH: CGFloat = 86.0
            let imageViewY = (self.bounds.height - imageViewWH) * 0.5
            let imageViewX = (self.bounds.width - imageViewWH) * 0.5
            
            imageView.frame = CGRect(x: imageViewX,
                                     y: imageViewY,
                                     width: imageViewWH,
                                     height: imageViewWH)
            imageView.isCycle = true
            imageView.layer.borderWidth = 1
            imageView.layer.borderColor = UIColor.white.cgColor
            
            let labelX: CGFloat = 10.0
            let lableY: CGFloat = imageViewY + imageViewWH + 19.0
            let labelW: CGFloat = self.bounds.width - (labelX * 2)
            let labelH: CGFloat = 24.0
            label.frame = CGRect(x: labelX, y: lableY, width: labelW, height: labelH)
        }
        
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
}

extension MineView {
    struct Info {
        let name: String
        let imageName: String
        
        static var empty: Info {
            return Info(name: "",
                        imageName: "")
        }
    }
}
