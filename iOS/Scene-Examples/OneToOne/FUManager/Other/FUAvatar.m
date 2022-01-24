//
//  FUAvatar.m
//  P2A
//
//  Created by L on 2018/12/15.
//  Copyright © 2018年 L. All rights reserved.
//
#include "objc/runtime.h"
#import "FUP2AColor.h"
@interface FUAvatar ()
@property (nonatomic, strong) dispatch_semaphore_t signal ;
@end

@implementation FUAvatar
-(void)setEyeBrow:(FUItemModel *)eyeBrow{
   _eyeBrow = eyeBrow;
}
- (instancetype)init
{
    self = [super init];
    if (self)
    {
        self.signal = [FUManager shareInstance].signal;
    }
    return self ;
}

#pragma mark ------ SET/GET ------
// 图片路径
-(NSString *)imagePath
{
    if (!_imagePath)
    {
        _imagePath = [[self filePath] stringByAppendingPathComponent:@"image.png"];
    }
    return _imagePath ;
}

/**
 avatar 模型保存的根目录
 
 @return  avatar 模型保存的根目录
 */
- (NSString *)filePath
{
    NSString *filePath ;
    if (self.defaultModel)
    {
        filePath = [[[NSBundle mainBundle].resourcePath stringByAppendingPathComponent:@"Resource"] stringByAppendingPathComponent:self.name];
    }
    else
    {
        filePath = [documentPath stringByAppendingPathComponent:self.name];
    }
    return filePath ;
}

/**
 获取 controller 所在句柄
 
 @return 返回 controller 所在句柄
 */
- (int)getControllerHandle
{
    return  items[FUItemTypeController];
}

/**
 销毁此模型
 -- 包括 controller, head, body, hair, clothes, glasses, beard, hat, animatiom, arfilter.
 */
- (void)destroyAvatar {
    
    // 先销毁普通道具
//    for (int i = 1 ; i < sizeof(items)/sizeof(int); i ++) {
//        if (items[i] != 0) {
//
//            // 先解绑
//            fuUnbindItems(items[FUItemTypeController], &items[i], 1) ;
//            // 再销毁
//            [FURenderer destroyItem:items[i]];
//            items[i] = 0 ;
//        }
//    }
//    // 再销毁 controller
//    [FURenderer destroyItem:items[FUItemTypeController]];
//    items[FUItemTypeController] = 0 ;
}
/**
 销毁此模型,只包括avatar资源
 -- 包括 , head, body, hair, clothes, glasses, beard, hat, animatiom, arfilter.
 */
- (void)destroyAvatarResouce {
    
}


/**
 更新Cam道具

 @param camPath 辅助道具路径
 */
- (void)reloadCamItemWithPath:(NSString * __nullable)camPath {
    [self loadItemWithtype:FUItemTypeCamera filePath:camPath];
}
/**
 更换动画
 
 @param animationPath 新动画所在路径
 */
- (void)reloadAnimationWithPath_NoSignal:(NSString *)animationPath
{
    [self loadItemWithtype:FUItemTypeAnimation filePath:animationPath];
}
/**
 更换动画
 
 @param animationPath 新动画所在路径
 */
- (void)reloadAnimationWithPath:(NSString *)animationPath
{
    dispatch_semaphore_wait(self.signal, DISPATCH_TIME_FOREVER) ;
    [self loadItemWithtype:FUItemTypeAnimation filePath:animationPath];
    dispatch_semaphore_signal(self.signal) ;
}

/**
 更新辅助道具
 
 @param tmpPath 辅助道具路径
 */
- (void)reloadTmpItemWithPath:(NSString *)tmpPath
{
    [self loadItemWithtype:FUItemTypeTmp filePath:tmpPath];
}

// 加载普通道具
- (void)loadItemWithtype:(FUItemType)itemType filePath:(NSString *)path {
    
    BOOL isDirectory;
    BOOL isExist = [[NSFileManager defaultManager] fileExistsAtPath:path
                                                        isDirectory:&isDirectory];
    
    if (path == nil || !isExist || isDirectory) {
        
        NSString *tips = [NSString stringWithFormat:@"没有这个资源 %@", path];
        NSLog(@"%@", tips);
        return ;
    }
    
    [_avatarEngine enableAvatarGeneratorItem:YES
                                        type:(int)itemType
                                      bundle:path];
    
}


/// 添加临时道具，不销毁老的同类道具
/// @param handle 记录当前的句柄
/// @param path 动画文件路径
- (void)addItemWithHandle:(int*)handle filePath:(NSString *)path {
    
        
}

/// 获取当前动画句柄
-(int)getCurrentAnimationHandle{
    return items[FUItemTypeAnimation];
}
/// 获取当前动画播放进度
- (float)getAnimateProgress{
    return 0.0;
}

// 销毁某个道具
- (void)destroyItemWithType:(FUItemType)itemType {
    
}

/// 获取 tmpItems 第一个空闲的位置
-(int)getTmpItemsNullIndex{
    for (int i = 0; i < tmpItemsCount; i++) {
        if (tmpItems[i] == 0) {
            return i;
        }
    }
    return tmpItemsCount - 1;
}
// 添加临时道具
- (void)addTmpItemFilePath:(NSString *)path{
    NSLog(@"addTmpItemFilePath no imple ");
}
/// 销毁所有的临时句柄
-(void)destoryAllTmpItems{
    
}
// 销毁某个道具
- (void)destroyItemAnimationItemType{
    
}






#pragma mark --- 以下身体追踪模式

/**
 进入身体追踪模式
 */
- (void)enterTrackBodyMode {
//    _renderer itemSetParam:items[FUItemTypeController] withName:@"enable_human_processor" value:@(1)];
}

/**
 退出身体追踪模式
 */
- (void)quitTrackBodyMode {
//    _renderer itemSetParam:items[FUItemTypeController] withName:@"enable_human_processor" value:@(0)];
}
/**
 进入身体跟随模式
 */
- (void)enterFollowBodyMode {
//    _renderer itemSetParam:items[FUItemTypeController] withName:@"human_3d_track_is_follow" value:@(1)];
}

/**
 退出身体跟随模式
 */
- (void)quitFollowBodyMode {
//    _renderer itemSetParam:items[FUItemTypeController] withName:@"human_3d_track_is_follow" value:@(0)];
}
/**
 设置在身体动画和身体追踪数据之间过渡的时间，默认值为0.5（秒）
 */
- (void)setHuman3dAnimTransitionTime:(float)time{
    
}

/**
 进入DDE追踪模式
 */
- (void)enterDDEMode {

}

/**
 退出DDE追踪模式
 */
- (void)quitDDEMode {

}
/**
 去掉脖子
 */
- (void)removeNeck {
    
}
/**
 重新加上脖子
 */
- (void)reAddNeck {
    
}

/**
 打开Blendshape 混合
 */
- (void)enableBlendshape {

}
/**
 关闭Blendshape 混合
 */
- (void)disableBlendshape {

}
/**
 设置用户输入的bs系数数组
 */
- (void)setBlend_expression:(double*)blend_expression {

}
/**
 设置blend_expression的权重
 */
- (void)setExpression_wieght0:(double*)expression_wieght0 {
    
}
/**
 设置blend_expression的权重
 */
- (void)setExpression_wieght1:(double*)expression_wieght1 {

}
/// 向nama声明当前avatar时第几个avatar，在多个avatar同时存在时使用
/// @param index 声明当前avatar序号
-(void)setCurrentAvatarIndex:(int) index{
    self.currentInstanceId = index;
    [[FUManager shareInstance] setInstanceId:index];
}
#pragma mark ---- AR 滤镜模式

/**
 在 AR 滤镜模式下加载 avatar
 -- 默认加载头部装饰，包括：头、头发、胡子、眼镜、帽子
 -- 加载完毕之后会设置其相应颜色
 
 @return 返回 controller 句柄
 */
- (int)loadAvatarWithARMode
{
    return 1;
}

- (void)loadHalfAvatar {}
- (void)loadFullAvatar {}
- (void)human3dSetYOffset:(float)y_offset {}
- (void)enterARMode {}
- (void)quitARMode {}

#pragma mark --- 捏脸模式

/**
 进入捏脸模式
 */
- (void)enterFacepupMode {
    double enterFaceUpvalue = 1;
    [_renderer itemSetWithName:@"enter_facepup_mode" value:enterFaceUpvalue];
    
    double animStateValue = 2;
    [_renderer itemSetWithName:@"animState" value:animStateValue];
}

/**
 退出捏脸模式
 */
- (void)quitFacepupMode {
    double enterFaceUpvalue = 1;
    [_renderer itemSetWithName:@"quit_facepup_mode" value:enterFaceUpvalue];
    
    double animStateValue = 1;
    [_renderer itemSetWithName:@"animState" value:animStateValue];
}

/**
 获取 mesh 顶点的坐标
 
 @param index   顶点序号
 @return        顶点坐标
 */
- (CGPoint)getMeshPointOfIndex:(NSInteger)index {
    [_renderer itemSetWithName:@"query_vert" value:index];
    
    
    
    double x = [_renderer fuItemGetParamd:@"query_vert_x"];
    
    double y = [_renderer fuItemGetParamd:@"query_vert_y"];
    
    CGSize size = [AppManager getSuitablePixelBufferSizeForCurrentDevice];
    
    return CGPointMake((1.0 - x/size.width) * [UIScreen mainScreen].bounds.size.width,(1.0 - y/size.height) * [UIScreen mainScreen].bounds.size.height) ;
}


/**
 获取 mesh 顶点的坐标
 
 @param index   顶点序号
 @return        顶点坐标
 */
- (CGPoint)getMeshPointOfIndex:(NSInteger)index
                  PixelBufferW:(int)pixelBufferW
                  PixelBufferH:(int)pixelBufferH {
    [_renderer itemSetWithName:@"query_vert" value:index];
    
    double x = [_renderer getDoubleWithName:@"query_vert_x"];
    double y = [_renderer getDoubleWithName:@"query_vert_y"];
    
    y = pixelBufferH - y;
    CGSize size = [UIScreen mainScreen].bounds.size;
    double realScreenWidth  = size.width;
    double realScreenHeight = size.height;
    double xR = realScreenWidth / pixelBufferW;
    double yR = realScreenHeight / pixelBufferH;
    
    if (xR < yR){
        x = x * xR;
        y = y*xR;// - (pixelBufferH*xR - realScreenHeight)/2;
    } else {
        x = x* yR;// - (pixelBufferW * yR - realScreenWidth) / 2;
        y = y * yR;
    }
    return CGPointMake( x , y);
}

/**
 获取当前身体追踪状态，0.no_body,1.half_body,2.half_more_body,3.full_body
 */
- (int)getCurrentBodyTrackState{
    return 0;
}
/**
 设置捏脸参数
 
 @param key     参数名
 @param level   参数
 */
- (void)facepupModeSetParam:(NSString *)key level:(double)level {
    key  = [NSString stringWithFormat:@"{\"name\":\"facepup\",\"param\":\"%@\"}", key];
    [_renderer itemSetWithName:key value:level];
}
/**
 获取捏脸参数
 
 @param key    参数名
 @return       参数
 */
- (double)getFacepupModeParamWith:(NSString *)key {
    key  = [NSString stringWithFormat:@"{\"name\":\"facepup\",\"param\":\"%@\"}", key];
    double result = [_renderer getDoubleWithName:key];
    return result;
}

/**
 捏脸模型下设置颜色
 -- key 具体参数如下：
 肤色：     skin_color
 唇色：     lip_color
 瞳色：     iris_color
 发色：     hair_color
 镜框颜色：  glass_color
 镜片颜色：  glass_frame_color
 胡子颜色：  beard_color
 帽子颜色：  hat_color
 
 
 @param color   颜色
 @param key     参数名
 */
- (void)facepupModeSetColor:(FUP2AColor *)color key:(NSString *)key {
    
    if ([key isEqualToString:@"lip_color"])
    {
        
        NSString * paramDicStr = [NSString stringWithFormat:@"fmt#{\"name\":\"global\",\"type\":\"face_detail\",\"param\":\"blend_color\",\"UUID\":{#type#%ld#}}",FUItemTypeLipGloss];
        
        if (color == nil) {
            color = [FUP2AColor color:UIColor.redColor];
        }
        [_renderer itemSetParam:0
                       withName:paramDicStr
                   fucolorValue:color
                         sub255:YES];
        
        
        return;
    }
    else if ([key isEqualToString:@"eyelash_color"])
    {
        
        NSString * paramDicStr = [NSString stringWithFormat:@"fmt#{\"name\":\"global\",\"type\":\"face_detail\",\"param\":\"blend_color\",\"UUID\":{#type#%ld#}}",FUItemTypeEyeLash];
        if (color != nil) {
            [_renderer itemSetParam:0
                           withName:paramDicStr
                       fucolorValue:color
                             sub255:YES];
        }
        
        return;
    }
    else if ([key isEqualToString:@"eyeshadow_color"])
    {
        NSString * paramDicStr = [NSString stringWithFormat:@"fmt#{\"name\":\"global\",\"type\":\"face_detail\",\"param\":\"blend_color\",\"UUID\":{#type#%ld#}}",FUItemTypeEyeShadow];
        
        if (color != nil) {
            [_renderer itemSetParam:0
                           withName:paramDicStr
                       fucolorValue:color
                             sub255:YES];
        }
        
                                     
        return;
    }
    
    [_renderer itemSetParam:0
                   withName:key
               fucolorValue:color
                     sub255:NO];
    
    
    if ([key isEqualToString:@"hair_color"]) {
        double intensity = color.intensity;
        [_renderer itemSetWithName:@"hair_color_intensity" value:intensity];
    }
}

- (void)facepupModeSetEyebrowColor:(FUP2AColor *)color
{
    NSString * paramDicStr = [NSString stringWithFormat:@"fmt#{\"name\":\"global\",\"type\":\"face_detail\",\"param\":\"blend_color\",\"UUID\":{#type#%ld#}}",(long)FUItemTypeEyeBrow];
    
    if (color != nil) {
        [_renderer itemSetParam:0
                       withName:paramDicStr
                   fucolorValue:color
                         sub255:YES];
    }
}

- (void)setBackGroundColor:(UIColor *)color {
    [_renderer fuItemSetParamd:@"enable_background_color"
                         value:1];
    [_renderer fuItemSetParamd:@"set_background_color"
                    colorValue:color
                        sub255:NO];
}

#pragma mark ----- 以下动画相关

/**
 获取动画总帧数
 
 @return 动画总帧数
 */
- (int)getAnimationFrameCount {
    double result = [_renderer fuItemGetParamd:@"frameNum"];
    return result;
}

/**
 获取当前帧动画播放的位置
 
 @return    当前动画播放的位置
 */
- (int)getCurrentAnimationFrameIndex {
    double result = [_renderer fuItemGetParamd:@"animFrameId"];
    return result;
}


/**
 重新开始播放动画
 */
- (void)restartAnimation {
    
}
/**
 播放动画
 */
- (void)startAnimation {
    
}
/**
 播放一次动画
 */
- (void)playOnceAnimation {
    
}
/**
 暂停动画
 */
- (void)pauseAnimation {}
/**
 结束动画
 */
- (void)stopAnimation {}
/**
 启用相机动画
 */
- (void)enableCameraAnimation {}
/**
 停止相机动画
 */
- (void)stopCameraAnimation {}
/**
 循环相机动画
 */
- (void)loopCameraAnimation {}
/**
 停止循环相机动画
 */
- (void)stopLoopCameraAnimation {}

-(NSString *)description
{
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList([self class], &outCount);
    NSMutableString * descriptionString = [NSMutableString string];
    for(i = 0; i < outCount; i++)
    {
        objc_property_t property = properties[i];
        const char *propName = property_getName(property);
        if(propName)
        {
            NSString *propertyName = [NSString stringWithCString:propName
                                                        encoding:[NSString defaultCStringEncoding]];
            id value = [self valueForKey:propertyName];
            [descriptionString appendFormat:@"%@", [NSString stringWithFormat:@"%@:%@\n",propertyName,value]];
        }
    }
    free(properties);
    return descriptionString;
}
#pragma mark ----- 获取配置

-(id)copyWithZone:(NSZone *)zone
{
    FUAvatar * copyAvatar = [[FUAvatar alloc]init];
    copyAvatar.avatarEngine = self.avatarEngine;
    copyAvatar.renderer = self.renderer;
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList([self class], &outCount);
    for(i = 0; i < outCount; i++)
    {
        objc_property_t property = properties[i];
        const char *propName = property_getName(property);
        if(propName)
        {
            NSString *propertyName = [NSString stringWithCString:propName encoding:[NSString defaultCStringEncoding]];
            
            id value = [self valueForKey:propertyName];
            
            [copyAvatar setValue:value forKey:propertyName];
        }
    }
    // 复制的对象  重新设置一些属性
    //    copyAvatar.defaultModel = NO;
    //    copyAvatar.imagePath = nil;
    free(properties);
    return copyAvatar;
}


- (void)openHairAnimation
{
    [_renderer fuItemSetParamd:@"modelmat_to_bone" value:1];
}

- (void)closeHairAnimation
{
    [_renderer fuItemSetParamd:@"modelmat_to_bone" value:0];
}



#pragma mark  ------ 捏脸 ------
- (NSArray *)getFacepupModeParamsWithLength:(int)length
{
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

/// 设置捏脸参数
/// @param dict 捏脸参数字典
- (void)configFacepupParamWithDict:(NSDictionary *)dict
{
    [dict enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop)
     {
        key  = [NSString stringWithFormat:@"{\"name\":\"facepup\",\"param\":\"%@\"}", key];
        [_renderer itemSetWithName:key value:[obj doubleValue]];
    }];
}

/**
 加载ani_mg动画
 */
- (void)load_ani_mg_Animation
{
    NSString *animationPath = [[NSBundle mainBundle] pathForResource:@"ani_mg.bundle" ofType:nil];
    [self reloadAnimationWithPath:animationPath];
}

/**
 去除动画
 */
- (void)removeAnimation
{
    [self reloadAnimationWithPath:nil];
}



/**
 换装后回到首页动画
 */
- (void)loadAfterEditAnimation
{
    if (!self.isQType) return;
    
    NSString *animationPath = [[NSBundle mainBundle] pathForResource:@"ani_ok_mid.bundle" ofType:nil];
    
    [self reloadAnimationWithPath:animationPath];
    [self playOnceAnimation];
}

//换装界面动画
- (void)loadChangeItemAnimation
{
    if (!self.isQType) return;
    
    NSString *animationPath = [[NSBundle mainBundle] pathForResource:@"ani_change_01.bundle" ofType:nil];
    [self reloadAnimationWithPath:animationPath];
}

/**
 加载待机动画
 */
- (void)loadStandbyAnimation
{
    NSString *animationPath ;
    if (self.isQType)
    {
        animationPath = [[NSBundle mainBundle] pathForResource:@"ani_huxi_hi.bundle" ofType:nil];
    }
    else
    {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_animation" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_animation" ofType:@"bundle"] ;
    }
    [self reloadAnimationWithPath:animationPath];
}
/**
 人脸追踪时加载 Pose 不带信号量
 */
- (void)loadTrackFaceModePose_NoSignal
{
    NSString *animationPath;
    if (self.isQType)
    {
        animationPath = [[NSBundle mainBundle] pathForResource:@"ani_pose.bundle" ofType:nil];
    }
    else
    {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_pose" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_pose" ofType:@"bundle"] ;
    }
    [self reloadAnimationWithPath_NoSignal:animationPath];
}
/**
 人脸追踪时加载 Pose
 */
- (void)loadTrackFaceModePose
{
    NSString *animationPath;
    if (self.isQType)
    {
        animationPath = [[NSBundle mainBundle] pathForResource:@"ani_pose.bundle" ofType:nil];
    }
    else
    {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_pose" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_pose" ofType:@"bundle"] ;
    }
    [self reloadAnimationWithPath:animationPath];
}
/**
呼吸动画,不带信号量
*/
- (void)loadIdleModePose_NoSignal
{
    NSString *animationPath;
    if (self.isQType)
    {
        animationPath = [[NSBundle mainBundle] pathForResource:@"ani_idle.bundle" ofType:nil];
    }else
    {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_pose" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_pose" ofType:@"bundle"] ;
    }
    [self reloadAnimationWithPath_NoSignal:animationPath];
}
/**
 呼吸动画
 */
- (void)loadIdleModePose
{
    NSString *animationPath;
    if (self.isQType)
    {
        animationPath = [[NSBundle mainBundle] pathForResource:@"ani_idle.bundle" ofType:nil];
    }else
    {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_pose" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_pose" ofType:@"bundle"] ;
    }
    [self reloadAnimationWithPath:animationPath];
}

/**
 身体追踪时加载 Pose
 */
- (void)loadTrackBodyModePose {
    NSString *animationPath;
    if (self.isQType) {
        animationPath = [[NSBundle mainBundle] pathForResource:@"anim_one1.bundle" ofType:nil];
    }else {
        animationPath = self.gender == FUGenderMale ? [[NSBundle mainBundle] pathForResource:@"male_pose" ofType:@"bundle"] : [[NSBundle mainBundle] pathForResource:@"female_pose" ofType:@"bundle"] ;
    }
    [self loadItemWithtype:FUItemTypeAnimation filePath:animationPath];
}

#pragma mark --- 以下缩放位移
/**
 设置缩放参数
 
 @param delta 缩放增量
 */
- (void)resetScaleDelta:(float)delta
{
    
}

/**
 设置旋转参数
 
 @param delta 旋转增量
 */
- (void)resetRotDelta:(float)delta
{
    NSString *keyName = @"rot_delta";
    double value = (double)delta;
    [_renderer itemSetWithName:keyName value:value];
}

/**
 设置垂直位移
 
 @param delta 垂直位移增量
 */
- (void)resetTranslateDelta:(float)delta {
    NSString *keyName = @"translate_delta";
    double value = (double)delta;
    [_renderer itemSetWithName:keyName value:value];
}

/**
 缩放至面部正面
 */
- (void)resetScaleToFace {
    [_renderer itemSetParam:0
                   withName:@"target_scale"
                      value:-175];
    [_renderer itemSetParam:0
                   withName:@"target_trans"
                      value:-7];
    [_renderer itemSetParam:0
                   withName:@"target_angle"
                      value:0.0];
    [_renderer itemSetParam:0
                   withName:@"reset_all"
                      value:6];
}

/**
 缩放至截图
 */
- (void)resetScaleToScreenShot
{
    [_renderer itemSetWithName:@"target_scale" value:4];   // 调整模型大小，值越小，模型越大
    [_renderer itemSetWithName:@"target_trans" value:-10];  // 调整模型的上下位置，值越小，越靠下
    [_renderer itemSetWithName:@"target_angle" value:0.0];  // 调整模型的旋转角度
    [_renderer itemSetWithName:@"reset_all" value:1];       // 调用生效
}

/**
 捏脸模式缩放至面部正面
 */
- (void)resetScaleToShapeFaceFront
{
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:3];
}

/**
 捏脸模式缩放至面部侧面
 */
- (void)resetScaleToShapeFaceSide
{
    [_renderer itemSetWithName:@"target_angle" value:0.125];
    [_renderer itemSetWithName:@"reset_all" value:3];
}

/**
 缩放至全身
 */
- (void)resetScaleToBody
{
    [_renderer itemSetWithName:@"target_scale" value:-150];
    [_renderer itemSetWithName:@"target_trans" value:2];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}


/**
 缩放至小比例的全身
 */
- (void)resetScaleToSmallBody
{
    [_renderer itemSetWithName:@"target_scale" value:-507];
    [_renderer itemSetWithName:@"target_trans" value:60];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}



- (void)resetPosition
{
    double position[3] = {0,0,0};
    [_renderer itemSetParamdv:@"target_position" value:position];
    
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:0];
}

- (void)resetPositionToShowHalf {
    double position[3] = {0,-20,0};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:0];
}

/// 缩小至全身并在屏幕左边显示
- (void)resetScaleSmallBodyToLeft
{
    [_renderer itemSetWithName:@"target_trans" value:60];
    double position[3] = {-100,0,-1000};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/// 缩小至全身并在屏幕左边显示
- (void)resetScaleSmallBodyToRight
{
    [_renderer itemSetWithName:@"target_trans" value:60];
    double position[3] = {100,0,-1000};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/// 缩小至全身并在屏幕上边显示
- (void)resetScaleSmallBodyToUp
{
    [_renderer itemSetWithName:@"target_scale" value:-1000];
    [_renderer itemSetWithName:@"target_trans" value:120];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/// 缩小至全身并在屏幕下面显示
- (void)resetScaleSmallBodyToDown
{
    [_renderer itemSetWithName:@"target_scale" value:-1000];
    [_renderer itemSetWithName:@"target_trans" value:0];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/**
 缩放至显示 Q 版的鞋子
 */
- (void)resetScaleToShowShoes
{
    [_renderer itemSetWithName:@"target_scale" value:-800];
    [_renderer itemSetWithName:@"target_trans" value:100];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/**
 缩放至小比例的身体跟随
 */
- (void)resetScaleToFollowBody
{
    [_renderer itemSetWithName:@"target_scale" value:-5000];
    [_renderer itemSetWithName:@"target_trans" value:240];
    [_renderer itemSetWithName:@"target_angle" value:0];
    [_renderer itemSetWithName:@"reset_all" value:6];
}


/**
 将Avatar的位置设置为初始状态
 */
- (void)resetScaleToOriginal
{
    double position[3] = {0,0,0};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:1];
}


/**
 使用相机bundle缩放至脸部特写
 */
- (void)resetScaleToFace_UseCam
{
    [self resetPosition];
    
    // 获取当前相机动画bundle路径
    NSString *camPath = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/page_cam/cam_texie.bundle"];
    // 将相机动画绑定到controller上
    [[FUManager shareInstance] reloadCamItemWithPath:camPath];
}
/**
 使用相机bundle缩放至脸部特写,不使用信号量，防止造成死锁
 */
- (void)resetScaleToFace_UseCamNoSignal
{
    [self resetPosition];
    
    // 获取当前相机动画bundle路径
    NSString *camPath = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/page_cam/cam_texie.bundle"];
    // 将相机动画绑定到controller上
    [[FUManager shareInstance] reloadCamItemNoSignalWithPath:camPath];
}

/**
 使用相机bundle缩放至小比例的全身
 */
- (void)resetScaleToSmallBody_UseCam
{
    [self resetPosition];
    
    // 获取当前相机动画bundle路径
    NSString *camPath = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/page_cam/cam_02.bundle"];
    // 将相机动画绑定到controller上
    [[FUManager shareInstance] reloadCamItemWithPath:camPath];
}

/**
 使用相机bundle缩放至全身
 */
- (void)resetScaleToBody_UseCam
{
    [self resetPosition];
    
    // 获取当前相机动画bundle路径
    NSString *camPath = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/page_cam/cam_35mm_full_80mm_jinjing.bundle"];
    // 将相机动画绑定到controller上
    [[FUManager shareInstance] reloadCamItemWithPath:camPath];
}

/**
 替换服饰时使用的cam
 */
- (void)resetScaleChange_UseCam
{
    [self resetPosition];
    
    // 获取当前相机动画bundle路径
    NSString *camPath = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/page_cam/cam_quanshen.bundle"];
    // 将相机动画绑定到controller上
    [[FUManager shareInstance] reloadCamItemWithPath:camPath];
}


/**
 缩放至全身追踪,驱动页未收起模型选择栏等工具栏的情况

 */
- (void)resetScaleToTrackBodyWithToolBar
{
    double position[3] = {0,75,-700};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/**
 缩放至全身追踪,驱动页收起模型选择栏等工具栏的情况

 */
- (void)resetScaleToTrackBodyWithoutToolBar
{
    double position[3] = {0,55,-520};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:6];
}


/**
缩放至全身追踪
使用场景：
1.导入视频后生成的画面
*/
- (void)resetScaleToImportTrackBody
{
    double position[3] = {0,75,-700};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/**
 缩放至半身
 */
- (void)resetScaleToHalfBodyWithToolBar
{
    double position[3] = {0,15,-300};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:6];
}

/**
 缩放至半身
 */
- (void)resetScaleToHalfBodyInput
{
    double position[3] = {0,0,-700};
    [_renderer itemSetParamdv:@"target_position" value:position];
    [_renderer itemSetWithName:@"reset_all" value:6];
}


/// 根据传入的形象模型重设形象的信息
/// @param avatar 形象模型
- (void)resetValueFromBeforeEditAvatar:(FUAvatar *)avatar
{
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList([self class], &outCount);
    for(i = 0; i < outCount; i++)
    {
        objc_property_t property = properties[i];
        const char *propName = property_getName(property);
        if(propName)
        {
            NSString *propertyName = [NSString stringWithCString:propName encoding:[NSString defaultCStringEncoding]];
            
            id value = [avatar valueForKey:propertyName];
            [self setValue:value forKey:propertyName];
        }
    }
    free(properties);
}


- (NSString *)getBodyFilePathWithModel:(FUItemModel *)model
{
    NSString *bodyFilepath = @"midBody";
    bodyFilepath = [bodyFilepath stringByAppendingFormat:@"_%@",[model.gender integerValue]>0?@"female":@"male"];
    bodyFilepath = [bodyFilepath stringByAppendingFormat:@"%zi.bundle",[model.body_match_level integerValue]];
    bodyFilepath = [[NSBundle mainBundle]pathForResource:bodyFilepath ofType:nil];
    
    return bodyFilepath;
}



#pragma mark ------ 形象加载 ------
/**
 加载 avatar 模型
 --  会加载 头、头发、身体、衣服、默认动作 四个道具。
 --  如果有 胡子、帽子、眼镜也会加载，没有则不加载。
 --  会设置 肤色、唇色、瞳色、发色(光头不设)。
 --  如果有 胡子、帽子、眼镜也会设置其对应颜色。
 
 @return 返回 controller 所在句柄
 */
- (int)loadAvatarToController
{
  return [self loadAvatarToControllerWith:YES];
}

#pragma mark ------ 形象加载 ------
/**
 加载 avatar 模型
 --  会加载 头、头发、身体、衣服、默认动作 四个道具。
 --  如果有 胡子、帽子、眼镜也会加载，没有则不加载。
 --  会设置 肤色、唇色、瞳色、发色(光头不设)。
 --  如果有 胡子、帽子、眼镜也会设置其对应颜色。
 
 @return 返回 controller 所在句柄
 @param isBg 是否渲染模型自身的背景 bundle
 */
- (int)loadAvatarToControllerWith:(BOOL)isBg
{
    // load controller
    if (items[FUItemTypeController] == 0)
    {
        items[FUItemTypeController] = [FUManager shareInstance].defalutQController;
    }
    
    // load Head
    NSString *headPath = [self.filePath stringByAppendingPathComponent:FU_HEAD_BUNDLE];
    [self bindItemWithType:FUItemTypeHead filePath:headPath];

    if (self.skinColorProgress == -1)
    {
        NSString * paramDicStr = [NSString stringWithFormat:@"skin_color_index"];
        int index = [_renderer fuItemGetParamd:paramDicStr];
        self.skinColorProgress = index/10.0;
    }
    // load Body
    NSString *bodyPath;
    if (self.clothType == FUAvataClothTypeSuit)
    {
        bodyPath = [self getBodyFilePathWithModel:self.clothes];
    }
    else
    {
        bodyPath = [self getBodyFilePathWithModel:self.upper];
    }
    [self bindItemWithType:FUItemTypeBody filePath:bodyPath];
    
    if (self.hairType == FUAvataHairTypeHair)
    {
        // load hair
        NSString *hairPath = [self.filePath stringByAppendingPathComponent:self.hair.name];
        [self destroyItemWithType:FUItemTypeHairHat];
        [self bindItemWithType:FUItemTypeHair filePath:hairPath];
    }
    else
    {
        [self bindHairHatWithItemModel:self.hairHat];
    }
    
    
    if (self.isQType)
    {
        // load clothes
        if (self.clothType == FUAvataClothTypeSuit)
        {
            [self bindClothWithItemModel:self.clothes];
        }
        else
        {
            [self bindUpperWithItemModel:self.upper];
            [self bindLowerWithItemModel:self.lower];
        }
    }

    [self bindShoesWithItemModel:self.shoes];
    // 配饰 大类  多选
    [self bindDecorationShouWithItemModel:self.decoration_shou];
    [self bindDecorationJiaoWithItemModel:self.decoration_jiao];
    [self bindDecorationXianglianWithItemModel:self.decoration_xianglian];
    [self bindDecorationErhuanWithItemModel:self.decoration_erhuan];
    [self bindDecorationToushiWithItemModel:self.decoration_toushi];
      
    [self bindHatWithItemModel:self.hat];
    [self bindEyeLashWithItemModel:self.eyeLash];
    [self bindEyebrowWithItemModel:self.eyeBrow];
    [self bindBeardWithItemModel:self.beard];
    [self bindEyeShadowWithItemModel:self.eyeShadow];
    [self bindEyeLinerWithItemModel:self.eyeLiner];
    [self bindPupilWithItemModel:self.pupil];
    [self bindFaceMakeupWithItemModel:self.faceMakeup];
    [self bindLipGlossWithItemModel:self.lipGloss];
    [self bindGlassesWithItemModel:self.glasses];
    if(isBg)
    [self bindBackgroundWithItemModel:self.dress_2d];
    [self loadAvatarColor];
    return items[FUItemTypeController] ;
}


/// 加载形象颜色
- (void)loadAvatarColor
{
    for (int i = 0; i < FUFigureColorTypeEnd; i++)
    {
        NSString *key = [[FUManager shareInstance]getColorKeyWithType:(FUFigureColorType)i];
        
        NSString *indexProKey = [key stringByReplacingOccurrencesOfString:@"_c" withString:@"C"];
        indexProKey = [indexProKey stringByReplacingOccurrencesOfString:@"_f" withString:@"F"];
        indexProKey = [indexProKey stringByAppendingString:@"Index"];
        
        NSInteger index = [[self valueForKey:indexProKey] integerValue] -1;
        
        FUP2AColor *color = [[FUManager shareInstance].colorDict[key] objectAtIndex:index];
        
        if (i == FUFigureColorTypeSkinColor)
        {
            color = [[FUManager shareInstance]getSkinColorWithProgress:self.skinColorProgress];
        }
        
        [self facepupModeSetColor:color key:key];
    }
}

- (void)loadAvatarSkinColor
{
    NSString *key = @"skinColorIndex";
    NSInteger index = [[self valueForKey:key] integerValue] -1;
    FUP2AColor *color = [[FUManager shareInstance].colorDict[key] objectAtIndex:index];
    color = [[FUManager shareInstance]getSkinColorWithProgress:self.skinColorProgress];
    [self facepupModeSetColor:color key:@"skin_color"];
}

#pragma mark ------ 绑定道具 ------
- (void)bindClothWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
    NSString *bodyFilepath = [self getBodyFilePathWithModel:model];
    
    [self bindItemWithType:FUItemTypeBody filePath:bodyFilepath];
    [self destroyItemWithType:FUItemTypeUpper];
    [self destroyItemWithType:FUItemTypeLower];
    [self bindItemWithType:FUItemTypeClothes filePath:filepath];
    self.clothType = FUAvataClothTypeSuit;
}

/// 加载发型
/// @param model 发型数据
- (void)bindHairWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [NSString stringWithFormat:@"%@/%@",[self filePath],model.name];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:filepath])
    {
        
        [[NSNotificationCenter defaultCenter] postNotificationName:FUCreatingHairBundleNot object:nil userInfo:@{@"show":@(1)}];
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [[FUManager shareInstance]createAndCopyHairBundlesWithAvatar:self withHairModel:model];
            [[NSNotificationCenter defaultCenter] postNotificationName:FUCreatingHairBundleNot object:nil userInfo:@{@"show":@(0)}];
            
            [self destroyItemWithType:FUItemTypeHairHat];
            [self bindItemWithType:FUItemTypeHair filePath:filepath];
        });
        
    }
    else
    {
        [self destroyItemWithType:FUItemTypeHairHat];
        [self bindItemWithType:FUItemTypeHair filePath:filepath];
    }
}

/// 加载上衣
/// @param model 上衣数据
- (void)bindUpperWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
    NSString *bodyFilepath = [self getBodyFilePathWithModel:model];
    
    [self destroyItemWithType:FUItemTypeClothes];
    [self bindItemWithType:FUItemTypeBody filePath:bodyFilepath];
    [self bindItemWithType:FUItemTypeUpper filePath:filepath];
    self.clothType = FUAvataClothTypeUpperAndLower;
    }

/// 加载下衣
/// @param model 下衣数据
- (void)bindLowerWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        
    [self destroyItemWithType:FUItemTypeClothes];
    [self bindItemWithType:FUItemTypeLower filePath:filepath];
    self.clothType = FUAvataClothTypeUpperAndLower;
    }

/// 加载鞋子
/// @param model 鞋子数据
- (void)bindShoesWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeShoes filePath:filepath];
    }

/// 加载帽子
/// @param model 帽子数据
- (void)bindHatWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeHat filePath:filepath];
    }

/// 加载睫毛
/// @param model 睫毛数据
- (void)bindEyeLashWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
    [self bindItemWithType:FUItemTypeEyeLash filePath:filepath];
    [self facepupModeSetColor:[[FUManager shareInstance] getSelectedColorWithType:FUFigureColorTypeEyelashColor] key:@"eyelash_color"];
    }

/// 加载眉毛
/// @param model 眉毛数据
- (void)bindEyebrowWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
    [self bindItemWithType:FUItemTypeEyeBrow filePath:filepath];
    [self facepupModeSetEyebrowColor:[[FUManager shareInstance] getSelectedColorWithType:FUFigureColorTypeEyebrowColor]];
    }

/// 加载胡子
/// @param model 胡子数据
- (void)bindBeardWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeBeard filePath:filepath];
    }

/// 加载眼镜
/// @param model 眼镜数据
- (void)bindGlassesWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeGlasses filePath:filepath];
    }

/// 加载眼影
/// @param model 眼影数据
- (void)bindEyeShadowWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeEyeShadow filePath:filepath];
    [self facepupModeSetColor:[[FUManager shareInstance] getSelectedColorWithType:FUFigureColorTypeEyeshadowColor] key:@"eyeshadow_color"];
    }

/// 加载眼线
/// @param model 眼线数据
- (void)bindEyeLinerWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeEyeLiner filePath:filepath];
    }

/// 加载美瞳
/// @param model 美瞳数据
- (void)bindPupilWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypePupil filePath:filepath];
    }

/// 加载脸妆
/// @param model 脸妆数据
- (void)bindFaceMakeupWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeMakeFaceup filePath:filepath];
    }

/// 加载唇妆
/// @param model 唇妆数据
- (void)bindLipGlossWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeLipGloss filePath:filepath];
    [self facepupModeSetColor:[[FUManager shareInstance] getSelectedColorWithType:FUFigureColorTypeLipsColor] key:@"lip_color"];
    }

/// 加载饰品
/// @param model 手饰品数据
- (void)bindDecorationShouWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeDecoration_shou filePath:filepath];
}
/// 加载饰品
/// @param model 脚饰品数据
- (void)bindDecorationJiaoWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeDecoration_jiao filePath:filepath];
}
/// 加载饰品
/// @param model 项链饰品数据
- (void)bindDecorationXianglianWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeDecoration_xianglian filePath:filepath];
}
/// 加载饰品
/// @param model 耳环饰品数据
- (void)bindDecorationErhuanWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    
        [self bindItemWithType:FUItemTypeDecoration_erhuan filePath:filepath];
}
/// 加载饰品
/// @param model 头饰饰品数据
- (void)bindDecorationToushiWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    [self bindItemWithType:FUItemTypeDecoration_toushi filePath:filepath];
}


/// 加载发帽
/// @param model 发帽数据
- (void)bindHairHatWithItemModel:(FUItemModel *)model
{
    
    NSString *filepath = [NSString stringWithFormat:@"%@/%@",[self filePath],model.name];
    
    if (![[NSFileManager defaultManager] fileExistsAtPath:filepath])
    {
        
        [[NSNotificationCenter defaultCenter] postNotificationName:FUCreatingHairHatBundleNot object:nil userInfo:@{@"show":@(1)}];
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [[FUManager shareInstance]createAndCopyHairHatBundlesWithAvatar:self withHairHatModel:model];
            [[NSNotificationCenter defaultCenter] postNotificationName:FUCreatingHairHatBundleNot object:nil userInfo:@{@"show":@(0)}];
            
            [self destroyItemWithType:FUItemTypeHairHat];
            [self bindItemWithType:FUItemTypeHair filePath:filepath];
        });
        
    }
    else
    {
        [self destroyItemWithType:FUItemTypeHair];
        [self bindItemWithType:FUItemTypeHairHat filePath:filepath];
    }
}

/// 加载背景 FUItemModel
/// @param model 背景数据
- (void)bindBackgroundWithItemModel:(FUItemModel *)model
{
    NSString *filepath = [model getBundlePath];
    [self destroyItemWithType:FUItemTypeBackground];
    [self bindItemWithType:FUItemTypeBackground filePath:filepath];
    }



#pragma mark ------ 绑定底层方法 ------
- (void)bindItemWithType:(FUItemType)itemType filePath:(NSString *)path
{
    BOOL isDirectory;
    BOOL isExist = [[NSFileManager defaultManager] fileExistsAtPath:path isDirectory:&isDirectory];
    
    if (path == nil || !isExist || isDirectory)
    {
        NSString *tips = [NSString stringWithFormat:@"没有这个资源 %@", path];
        NSLog(@"%@", tips);
        return ;
    }
    
    NSLog(@"enableAvatarGeneratorItem:%@", path);
    
   NSInteger ret = [_avatarEngine enableAvatarGeneratorItem:YES
                                        type:(int)itemType
                                      bundle:path];
    
    NSLog(@"enableAvatarGeneratorItem ret %ld", ret);
}

@end
