//
//  AEABottomInfo.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/18.
//

#import <Foundation/Foundation.h>
@class AEABottomInfoItem;

typedef NS_ENUM(NSUInteger, AEABottomItemSizeType) {
    AEABottomItemSizeTypeSmall,
    AEABottomItemSizeTypeBig
};

NS_ASSUME_NONNULL_BEGIN

@interface AEABottomInfo : NSObject

@property (nonatomic, copy)NSString *title;
@property (nonatomic, copy)NSArray<UIColor *> *colors;
@property (nonatomic, copy)NSArray<AEABottomInfoItem *> *items;
@property (nonatomic, assign)NSInteger selectedItemIndex;
@property (nonatomic, assign)AEABottomItemSizeType itemSizeType;

@end

@interface AEABottomInfoItem : NSObject

@property (nonatomic, copy)NSString *imageName;

@end

NS_ASSUME_NONNULL_END
