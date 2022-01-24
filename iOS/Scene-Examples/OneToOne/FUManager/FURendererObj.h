//
//  FURendererObj.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/23.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcKit.h>
@class FUP2AColor;
NS_ASSUME_NONNULL_BEGIN

@interface FURendererObj : NSObject

@property (nonatomic, strong)id<AvatarEngineProtocol> _Nonnull avatarEngine;

- (void)itemSetWithName:(NSString *)name
                  value:(double)value;
- (void)itemSetParamdv:(NSString *)name
                 value:(double [3]) value;
- (double)getDoubleWithName:(NSString *)name;
- (void)itemSetParam:(int)pa
            withName:(NSString *)name
               value:(double)value;
- (void)itemSetParam:(int)pa
            withName:(NSString *)name
          colorValue:(UIColor *)color
              sub255:(BOOL)sub255;
- (void)itemSetParam:(int)pa
            withName:(NSString *)name
        fucolorValue:(FUP2AColor *)color
              sub255:(BOOL)sub255;
/// 使用透明度
- (void)fuItemSetParamdUseAlph:(NSString *)name
                    colorValue:(UIColor *)color
                        sub255:(BOOL)sub255;

- (void)fuItemSetParamd:(NSString *)name value:(double)value;

- (void)fuItemSetParamd:(NSString *)name
             colorValue:(UIColor *)color
                 sub255:(BOOL)sub255;

- (double)fuItemGetParamd:(NSString *)name;
- (int)fuItemGetParamdInt:(NSString *)name;

@end

NS_ASSUME_NONNULL_END


