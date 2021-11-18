//
//  MainVM+Invoke.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/25.
//

import Foundation


extension MainVM {
    func invokeMainVM(_ vm: MainVM, didJoinRoom info: RoomInfo) {
        delegate?.mainVM(vm, didJoinRoom: info)
    }
    
    func invokeMainVM(_ vm: MainVM, shouldShow tips: String) {
        delegate?.mainVM(vm, shouldShow: tips)
    }
}
