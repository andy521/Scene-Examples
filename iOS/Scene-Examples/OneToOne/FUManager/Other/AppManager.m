//
//  AppManager.m
//  FUP2A
//
//  Created by LEE on 6/18/19.
//  Copyright © 2019 L. All rights reserved.
//

#import "AppManager.h"
@interface AppManager()
{
float _RStep;
float _GStep;
float _BStep;
}
@end
static AppManager *sharedInstance;
@implementation AppManager

// 以iphone11 作为参照物，来降低高分辨率，解决高分辨率手机卡顿问题
+(CGSize)getSuitablePixelBufferSizeForCurrentDevice{
	int iphone11_w = 750;
	int iphone11_h = 1624;
	CGSize size = [UIScreen mainScreen].currentMode.size;
	CGFloat current_iphone_w = size.width;
	CGFloat current_iphone_h = size.height;
	CGFloat scale = [UIScreen mainScreen].scale;
	if (current_iphone_w * current_iphone_h > iphone11_w * iphone11_h && scale == 3) {   // 以iphone11 作为参照物，来降低高分辨率，解决高分辨率手机卡顿问题
		CGFloat new_current_iphone_w = size.width / 3 * 2;
		CGFloat new_current_iphone_h = size.height / 3 * 2;
		return CGSizeMake(new_current_iphone_w, new_current_iphone_h);
	}
	return size;
}
@end
