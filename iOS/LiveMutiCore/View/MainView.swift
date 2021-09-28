//
//  MainView.swift
//  abseil
//
//  Created by ZYP on 2021/9/28.
//

import UIKit

class MainView: UIView {
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: VideoLayout())
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
        commonInit()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setup() {
        
        collectionView.backgroundColor = .white
        backgroundColor = .white
        addSubview(collectionView)
        collectionView.showsHorizontalScrollIndicator = false
    }
    
    func commonInit() {
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        collectionView.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
}

class VideoLayout: UICollectionViewFlowLayout {
    
    var attrs = [UICollectionViewLayoutAttributes]()
    
    override init() {
        super.init()
        sectionInset = .zero
        minimumLineSpacing = 0.5
        minimumInteritemSpacing = 0.5
        scrollDirection = .vertical
        let width = (UIScreen.main.bounds.size.width - 4*0.5)/3
        let height = width * 4/3
        itemSize = .init(width: width, height: height)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}
