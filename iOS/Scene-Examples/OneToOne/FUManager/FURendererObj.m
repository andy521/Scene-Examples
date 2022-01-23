//
//  FURendererObj.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/23.
//

#import "FURendererObj.h"
#import "FUP2AColor.h"

@implementation FURendererObj

- (void)itemSetWithName:(NSString *)name
                  value:(double)value {
    
    double *bytes = &value;
    AgoraAvatarValueType type = AgoraAvatarValueTypeDouble;
    AgoraAvatarOptionValue *optionValue = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                               num:1
                                                                             bytes:bytes];
    NSLog(@"setGeneratorOptions key %@", name);
    [_avatarEngine setGeneratorOptions:name value:optionValue];
}

- (void)itemSetParamdv:(NSString *)name
                 value:(double [3])value {

    AgoraAvatarValueType type = AgoraAvatarValueTypeDoubleArray;
    AgoraAvatarOptionValue *optionValue = [[AgoraAvatarOptionValue alloc] initWith:type
                                                                               num:3
                                                                             bytes:value];
    NSLog(@"setGeneratorOptions key %@", name);
    [_avatarEngine setGeneratorOptions:name value:optionValue];
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
    NSLog(@"setGeneratorOptions key %@", name);
    [_avatarEngine setGeneratorOptions:name value:optionValue];
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
                                                                       bytes:&c];
    NSLog(@"setGeneratorOptions key %@", name);
    [_avatarEngine setGeneratorOptions:name
                                 value:value];
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
                                                                       bytes:&c];
    NSLog(@"setGeneratorOptions key %@", name);
    [_avatarEngine setGeneratorOptions:name
                                 value:value];
}

- (double)fuItemGetParamd:(NSString *)name {
    return [self getDoubleWithName:name];
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

@end

// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// hair_color
// hair_color_intensity
// key skin_color
// iris_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// beard_color
// hat_color
// eyebrow_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// glass_frame_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// modelmat_to_bone
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// hair_color
// hair_color_intensity
// key skin_color
// key iris_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// beard_color
// hat_color
// eyebrow_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// glass_color
// glass_frame_color
// fmt#{"name":"global","type":"face_detail","param":"blend_color","UUID":{#type#0#}}
// modelmat_to_bone
// target_position
// target_angle
// reset_all
// target_position
// target_angle
// reset_all
// enable_background_color
// set_background_color
