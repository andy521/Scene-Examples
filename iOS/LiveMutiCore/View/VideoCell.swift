//
//  VideoCell.swift
//  LivePK
//
//  Created by ZYP on 2021/9/23.
//

import UIKit


class VideoCell: UICollectionViewCell {
    let videoView = UIView()
    private let button = UIButton(type: .contactAdd)
    private var style: Style = .tap
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        set(style: .tap)
        setup()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setup() {
        button.isUserInteractionEnabled = false
        backgroundColor = .gray
        contentView.addSubview(videoView)
        contentView.addSubview(button)
        button.translatesAutoresizingMaskIntoConstraints = false
        videoView.translatesAutoresizingMaskIntoConstraints = false
        
        videoView.leftAnchor.constraint(equalTo: contentView.leftAnchor).isActive = true
        videoView.rightAnchor.constraint(equalTo: contentView.rightAnchor).isActive = true
        videoView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        videoView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        button.leftAnchor.constraint(equalTo: contentView.leftAnchor).isActive = true
        button.rightAnchor.constraint(equalTo: contentView.rightAnchor).isActive = true
        button.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        button.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
    }
    
    func set(style: Style) {
        self.style = style
        
        switch style {
        case .tap:
            videoView.isHidden = true
            button.isHidden = false
            break
        case .video:
            videoView.isHidden = false
            button.isHidden = true
            break
        }
    }
}

extension VideoCell {
    enum Style {
        case video
        case tap
    }
}
