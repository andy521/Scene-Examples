//
//  AEAView.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/15.
//

#import <UIKit/UIKit.h>
@class AEABottomInfo;

NS_ASSUME_NONNULL_BEGIN

@interface AEAView : UIView

- (instancetype)initWithInfos:(NSArray<AEABottomInfo *> *)infos;

@end

NS_ASSUME_NONNULL_END
