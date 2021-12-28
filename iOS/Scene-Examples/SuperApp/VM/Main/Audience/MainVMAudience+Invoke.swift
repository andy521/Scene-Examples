//
//  MainVMAudience+Invoke.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/30.
//

import Foundation

extension MainVMAudience {
    func invokeMainVM(_ vm: MainVMProtocol, didJoinRoom info: RoomInfo) {
        if Thread.isMainThread {
            delegate?.mainVM(vm, didJoinRoom: info)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.mainVM(vm, didJoinRoom: info)
        }
    }
    
    func invokeMainVM(_ vm: MainVMProtocol, shouldShow tips: String) {
        if Thread.isMainThread {
            delegate?.mainVM(vm, shouldShow: tips)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.mainVM(vm, shouldShow: tips)
        }
        
    }
    
    func invokeMainVMShouldStartRenderRemoteView(_ vm: MainVMProtocol) {
        if Thread.isMainThread {
            delegate?.mainVMShouldStartRenderRemoteView(self)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.mainVMShouldStartRenderRemoteView(vm)
        }
    }
    
    func invokeMainVMShouldStoptRenderRemoteView(_ vm: MainVMProtocol) {
        if Thread.isMainThread {
            delegate?.mainVMShouldStopRenderRemoteView(vm)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.mainVMShouldStopRenderRemoteView(vm)
        }
    }
    
    func invokeMainVMShouldCloseRoom(_ vm: MainVMProtocol) {
        if Thread.isMainThread {
            delegate?.mainVMShouldCloseRoom(vm)
            return
        }
        
        DispatchQueue.main.async { [weak self] in
            self?.delegate?.mainVMShouldCloseRoom(vm)
        }
    }
}
