//
//  FUPoseTrackView.h
//  FUP2A
//
//  Created by LEE on 9/25/19.
//  Copyright © 2019 L. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol FUPoseTrackViewDelegate <NSObject>

@optional
// 点击模型
- (void)PoseTrackViewDidSelectedAvatar:(FUAvatar *)avatar ;
// 点击滤镜
- (void)PoseTrackViewDidSelectedInput:(NSString *)filterName ;
// 隐藏上半部
- (void)PoseTrackViewDidShowTopView:(BOOL)show ;
@end

@interface FUPoseTrackView : UIView
@property (nonatomic, assign) id<FUPoseTrackViewDelegate>delegate;
- (instancetype)init;
- (void)setupData;
- (void)selectedModeWith:(FUAvatar *)avatar;
- (void)showCollection:(BOOL)show;
- (void)reloadCollection;
- (void)freshInputIndex:(int)index;

@end


NS_ASSUME_NONNULL_END
