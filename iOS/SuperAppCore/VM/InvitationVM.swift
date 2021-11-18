//
//  InvitationVM.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/26.
//

import SyncManager

class InvitationVM: NSObject {
    typealias Info = InvitationCell.Info
    var sceneRef: SceneReference!
    
    init(sceneRef: SceneReference) {
        self.sceneRef = sceneRef
    }
    
    func start() {
        sceneRef.collection(className: "member")
            .get { objs in
                let strs = objs.compactMap({ $0.toJson() })
                print("")
            } fail: { error in
                
            }
    }
    
}
