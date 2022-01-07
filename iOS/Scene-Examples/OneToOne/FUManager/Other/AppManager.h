//
//  AppManager.h
//  FUP2A
//
//  Created by LEE on 6/18/19.
//  Copyright © 2019 L. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Photos/Photos.h>

NS_ASSUME_NONNULL_BEGIN

@interface AppManager : NSObject
// 以iphone11 作为参照物，来降低高分辨率，解决高分辨率手机卡顿问题
+(CGSize)getSuitablePixelBufferSizeForCurrentDevice;
@end

NS_ASSUME_NONNULL_END
