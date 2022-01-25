//
//  FURendererObj.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/23.
//

#import "FURendererObj.h"
#import "FUP2AColor.h"

@implementation FURendererObj

- (void)setGeneratorOptionsInternal:(NSString*)key value:(AgoraAvatarOptionValue *)value {
    NSLog(@"setGeneratorOptions key %@", key);
    [_avatarEngine setGeneratorOptions:key value:value];
}

- (void)itemSetWithName:(NSString *)name
                  value:(double)value {
    
    double *bytes = &value;
    AgoraAvatarValueType type = AgoraAvatarValueTypeDouble;
    AgoraAvatarOptionValue *optionValue = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                               num:1
                                                                             bytes:bytes];
    [self setGeneratorOptionsInternal:name value:optionValue];
}

- (void)itemSetParamdv:(NSString *)name
                 value:(double [3])value {
    AgoraAvatarValueType type = AgoraAvatarValueTypeDoubleArray;
    AgoraAvatarOptionValue *optionValue = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                               num:3
                                                                             bytes:value];
    [self setGeneratorOptionsInternal:name value:optionValue];
}

- (double)getDoubleWithName:(NSString *)name {
    AgoraAvatarOptionValue *value;
    [_avatarEngine getGeneratorOptions:name
                                  type:AgoraAvatarValueTypeDouble
                                result:&value];
    
    NSAssert(value != nil, @"optionValue not nil");
    double *result = (double *) value.value.bytes;
    return (double) *result;
}

- (void)itemSetParam:(int)pa
            withName:(NSString *)name
               value:(double)value {
    
    double *bytes = &value;
    AgoraAvatarValueType type = AgoraAvatarValueTypeDouble;
    AgoraAvatarOptionValue *optionValue = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                               num:1
                                                                             bytes:bytes];
    
    [self setGeneratorOptionsInternal:name value:optionValue];
}

- (void)itemSetParam:(int)pa
            withName:(NSString *)name
        fucolorValue:(FUP2AColor *)color
              sub255:(BOOL)sub255 {
    double s = sub255 ? 255 : 1;
    double c[3] = {
        color.r / s ,
        color.g / s,
        color.b / s
    };
    AgoraAvatarValueType type = AgoraAvatarValueTypeDoubleArray;
    AgoraAvatarOptionValue *value = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                         num:3
                                                                       bytes:c];
    
    [self setGeneratorOptionsInternal:name value:value];
}

- (void)itemSetParam:(int)pa
            withName:(NSString *)name
          colorValue:(UIColor *)color
              sub255:(BOOL)sub255 {
    FUP2AColor *fucolor = [FUP2AColor color:color];
    [self itemSetParam:pa
              withName:name
          fucolorValue:fucolor
                sub255:sub255];
}

- (void)fuItemSetParamd:(NSString *)name value:(double)value {
    [self itemSetParam:0 withName:name value:value];
}

- (void)fuItemSetParamd:(NSString *)name
             colorValue:(UIColor *)color
                 sub255:(BOOL)sub255 {
    FUP2AColor *fucolor = [FUP2AColor color:color];
    double s = sub255 ? 255 : 1;
    double c[3] = {
        fucolor.r / s,
        fucolor.g / s,
        fucolor.b /s
    };
    
    AgoraAvatarValueType type = AgoraAvatarValueTypeDoubleArray;
    AgoraAvatarOptionValue *value = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                         num:3
                                                                       bytes:c];
    
    [self setGeneratorOptionsInternal:name value:value];
}

- (void)fuItemSetParamdUseAlph:(NSString *)name
             colorValue:(UIColor *)color
                 sub255:(BOOL)sub255 {
    FUP2AColor *fucolor = [FUP2AColor color:color];
    double s = sub255 ? 255 : 1;
    double c[4] = {
        fucolor.r / s,
        fucolor.g / s,
        fucolor.b /s,
        fucolor.intensity /s
    };
    
    AgoraAvatarValueType type = AgoraAvatarValueTypeDoubleArray;
    AgoraAvatarOptionValue *value = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                         num:4
                                                                       bytes:c];
    
    [self setGeneratorOptionsInternal:name value:value];
}

- (double)fuItemGetParamd:(NSString *)name {
    return [self getDoubleWithName:name];
}

- (NSArray <NSNumber *>*)getArrayByKey:(NSString *)key length:(int)length {
    AgoraAvatarOptionValue *optionValue;
    [_avatarEngine getGeneratorOptions:@"facepup_expression"
                                  type:AgoraAvatarValueTypeDoubleArray
                                result:&optionValue];
    
    NSAssert(optionValue != nil, @"optionValue not nil");
    
    double (* arrPtr)[length] = NULL;
    arrPtr = (double (*)[length]) optionValue.value.bytes;
    
    NSMutableArray *params = [[NSMutableArray alloc]init];
    for (int i = 0; i < length; i++)
    {
        [params addObject:[NSNumber numberWithDouble:*(arrPtr[i])]];
    }
    return params;
}

- (int)fuItemGetParamdInt:(NSString *)name {
    AgoraAvatarOptionValue *value;
    [_avatarEngine getGeneratorOptions:name
                                  type:AgoraAvatarValueTypeUInt64
                                result:&value];
    
    NSAssert(value != nil, @"optionValue not nil");
    int *result = (int *) value.value.bytes;
    return (int) *result;
}

- (int)enableAvatarGeneratorItem:(BOOL)enable
                            type:(AgoraAvatarItemType)type
                          bundle:(NSString *  _Nonnull)bundle {
    NSLog(@"enableAvatarGeneratorItem %d %@", type, enable ? @"YES" : @"NO");
    return [_avatarEngine enableAvatarGeneratorItem:enable type:type bundle:bundle];
}

@end

