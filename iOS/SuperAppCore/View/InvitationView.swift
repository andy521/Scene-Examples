//
//  InvitationView.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/18.
//

import UIKit

protocol InvitationViewDelegate: NSObjectProtocol {
    func invitationView(_ view: InvitationView, didSelectedAt index: Int)
}

class InvitationView: UIView {
    let tableView = UITableView(frame: .zero,
                                style: .plain)
    let titleLabel = UILabel()
    private var infos = [InvitationCell.Info]()
    weak var delegate: InvitationViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
        commomInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setup() {
        backgroundColor = .white
        titleLabel.text = "在线用户"
        titleLabel.textColor = .gray
        addSubview(tableView)
        addSubview(titleLabel)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        
        titleLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        titleLabel.topAnchor.constraint(equalTo: topAnchor, constant: 10).isActive = true
        
        tableView.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        tableView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 10).isActive = true
        tableView.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        tableView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
    
    private func commomInit() {
        tableView.register(InvitationCell.self,
                           forCellReuseIdentifier: "InvitationCell")
        tableView.delegate = self
        tableView.dataSource = self
    }
    
    func update(infos: [InvitationCell.Info]) {
        self.infos = infos
    }
    
}

extension InvitationView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return infos.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "InvitationCell", for: indexPath) as! InvitationCell
        cell.delegate = self
        let info = infos[indexPath.row]
        cell.udpate(info: info)
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        return UIView()
    }
}

extension InvitationView: InvitationCellDelegate {
    func cell(_ cell: InvitationCell, on index: Int) {
        delegate?.invitationView(self, didSelectedAt: index)
    }
}
