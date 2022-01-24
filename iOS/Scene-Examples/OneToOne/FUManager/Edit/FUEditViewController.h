//
//  FUEditViewController.h
//  FUP2A
//
//  Created by L on 2018/8/22.
//  Copyright © 2018年 L. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol FUEditViewControllerDelegate <NSObject>

- (void)editViewControllerDidClose;

@end

@interface FUEditViewController : UIViewController
@property (copy, nonatomic) void(^editAvatarSuccessBlock) (void);
@property (nonatomic, weak)id<FUEditViewControllerDelegate> delegate;
+ (instancetype)instacneFromStoryBoard;
- (UIView *)getVideoView;
- (void)setPixelBufferSize:(CGSize)size;
@end
