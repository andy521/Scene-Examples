//
//  StrorageManager.swift
//  SuperAppCore
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

class StorageManager {
    static var userName: String {
        set {
            UserDefaults.standard.setValue(newValue,
                                           forKey: "userName")
        }
        get {
            return (UserDefaults.standard.value(forKey: "userName") as? String) ?? uuid
        }
    }
    
    static var roomName: String {
        set {
            UserDefaults.standard.setValue(newValue,
                                           forKey: "roomName")
        }
        get {
            return (UserDefaults.standard.value(forKey: "roomName") as? String) ?? ""
        }
    }
    
    static var uuid: String {
        set {
            UserDefaults.standard.setValue(newValue,
                                           forKey: "uuid")
        }
        get {
            var value = (UserDefaults.standard.value(forKey: "uuid") as? String)
            if value == nil {
                value = UUID().uuidString.replacingOccurrences(of: "-", with: "").lowercased()
                value = value?.subString(to: 16)
                UserDefaults.standard.setValue(value,
                                               forKey: "uuid")
            }
            return value!
        }
    }
}


