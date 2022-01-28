//
//  AgoraVoiceSoundEffectModel.swift
//  Scene-Examples
//
//  Created by zhaoyongqiang on 2022/1/27.
//

import UIKit
import AgoraRtcKit

enum SoundEffectType: Int, CaseIterable {
    case space = 0
    case voiceChangerEffect = 1
    case styleTransformation = 2
    case pitchCorrection = 3
    case magicTone = 4

    var dataArray: [AgoraVoiceSoundEffectModel] {
        var tempArray = [AgoraVoiceSoundEffectModel]()
        switch self {
        case .space:
            var model = AgoraVoiceSoundEffectModel(imageName: "icon-KTV", title: "KTV", effectPreset: .roomAcousticsKTV)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-演唱会", title: "演唱会", effectPreset: .roomAcousVocalConcer)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-录音棚", title: "录音棚", effectPreset: .roomAcousStudio)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-留声机", title: "留声机", effectPreset: .roomAcousPhonograph)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-虚拟立体声", title: "虚拟立体声", effectPreset: .roomAcousVirtualStereo)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-空旷", title: "空旷", effectPreset: .roomAcousSpatial)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-空灵", title: "空灵", effectPreset: .roomAcousEthereal)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-3D人声", title: "3D人声", effectPreset: .roomAcous3DVoice)
            tempArray.append(model)
            
        case .voiceChangerEffect:
            var model = AgoraVoiceSoundEffectModel(imageName: "icon-大叔磁性", title: "大叔", effectPreset: .voiceChangerEffectUncle)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-老年人", title: "老男人", effectPreset: .voiceChangerEffectOldMan)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-小男孩", title: "小男孩", effectPreset: .voiceChangerEffectBoy)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-小姐姐", title: "小姐姐", effectPreset: .voiceChangerEffectSister)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-小女孩", title: "小女孩", effectPreset: .voiceChangerEffectGirl)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-猪八戒", title: "猪八戒", effectPreset: .voiceChangerEffectPigKin)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-绿巨人", title: "绿巨人", effectPreset: .voiceChangerEffectHulk)
            tempArray.append(model)
            
        case .styleTransformation:
            var model = AgoraVoiceSoundEffectModel(imageName: "icon-R&B", title: "R&B", effectPreset: .styleTransformationRnb)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(imageName: "icon-流行", title: "流行", effectPreset: .styleTransformationPopular)
            tempArray.append(model)

        case .pitchCorrection:
            var model = AgoraVoiceSoundEffectModel(title: "A", effectPreset: .pitchCorrection, pitchCorrectionValue: 1)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "Ab", effectPreset: .pitchCorrection, pitchCorrectionValue: 2)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "B", effectPreset: .pitchCorrection, pitchCorrectionValue: 3)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "C", effectPreset: .pitchCorrection, pitchCorrectionValue: 4)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "Cb", effectPreset: .pitchCorrection, pitchCorrectionValue: 5)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "D", effectPreset: .pitchCorrection, pitchCorrectionValue: 6)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "Db", effectPreset: .pitchCorrection, pitchCorrectionValue: 7)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "E", effectPreset: .pitchCorrection, pitchCorrectionValue: 8)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "F", effectPreset: .pitchCorrection, pitchCorrectionValue: 9)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "Fb", effectPreset: .pitchCorrection, pitchCorrectionValue: 10)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "G", effectPreset: .pitchCorrection, pitchCorrectionValue: 11)
            tempArray.append(model)
            model = AgoraVoiceSoundEffectModel(title: "Gb", effectPreset: .pitchCorrection, pitchCorrectionValue: 12)
            tempArray.append(model)
            
        case .magicTone: break
        }
        return tempArray
    }
    
    var title: String {
        switch self {
        case .space:               return "空间塑造".localized
        case .voiceChangerEffect:  return "变声音效".localized
        case .styleTransformation: return "曲风音效".localized
        case .pitchCorrection:     return "电音音效".localized
        case .magicTone:           return "魔力音阶".localized
        }
    }
    
    var edges: UIEdgeInsets {
        switch self {
        case .voiceChangerEffect: fallthrough
        case .styleTransformation: fallthrough
        case .space: return UIEdgeInsets(top: 0, left: 15.fit, bottom: 0, right: 15.fit)
        case .pitchCorrection: return UIEdgeInsets(top: 0, left: 15.fit, bottom: 0, right: 15.fit)
        case .magicTone: return .zero
        }
    }
    
    var minInteritemSpacing: CGFloat {
        switch self {
        case .voiceChangerEffect: fallthrough
        case .styleTransformation: fallthrough
        case .space: return 20.fit
        case .pitchCorrection: return 15.fit
        case .magicTone: return 0
        }
    }
    
    var minLineSpacing: CGFloat {
        switch self {
        case .voiceChangerEffect: fallthrough
        case .styleTransformation: fallthrough
        case .space: return 20.fit
        case .pitchCorrection: return 20.fit
        case .magicTone: return 0
        }
    }
    
    var layout: CGSize {
        switch self {
        case .voiceChangerEffect: fallthrough
        case .styleTransformation: fallthrough
        case .space:
            let w = (Screen.width - 30.fit * 2 -  20.fit * 3) / 4
            return CGSize(width: w, height: 100)
            
        case .pitchCorrection:
            let w = (Screen.width - 30.fit * 2 - 20.fit * 3) / 4
            return CGSize(width: w, height: 40)
            
        case .magicTone: return .zero
        }
    }
}


struct AgoraVoiceSoundEffectModel {
    var imageName: String = ""
    var title: String = ""
    var effectPreset: AgoraAudioEffectPreset = .roomAcous3DVoice
    var pitchCorrectionValue: Int = 0
}
