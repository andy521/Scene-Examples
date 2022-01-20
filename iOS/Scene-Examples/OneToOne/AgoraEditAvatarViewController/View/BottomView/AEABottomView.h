//
//  AgoraEditBottomView.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/15.
//

#import <UIKit/UIKit.h>
@class AEATitleView;
@class AEAColorSelectedView;
@class AEABottomInfo;

NS_ASSUME_NONNULL_BEGIN

@interface AEABottomView : UIView

@property (nonatomic, strong)AEATitleView *titleView;
@property (nonatomic, strong)AEAColorSelectedView *colorSelectedView;
- (instancetype)initWithTitleInfos:(NSArray<AEABottomInfo *> *)infos;
@end

NS_ASSUME_NONNULL_END
