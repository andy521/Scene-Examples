//
//  VideoFrameHandler.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/7.
//

#import "VideoFrameHandler.h"
#import "FUManager.h"

@implementation VideoFrameHandler

- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)srcFrame dstFrame:(AgoraOutputVideoFrame * _Nullable __autoreleasing *)dstFrame {
    CVPixelBufferRef pixelBuffer = srcFrame.pixelBuffer;
    
//    /// 这里会new一个 CVPixelBufferRef
//    CVPixelBufferRef mirrored_pixel = [[FUManager shareInstance] dealTheFrontCameraPixelBuffer:pixelBuffer returnNewBuffer:YES];
//    [[FUManager shareInstance] renderARFilterItemWithBuffer:mirrored_pixel];
    
    const int landmarks_cnt = 314;
    float landmarks[landmarks_cnt] ;
    CVPixelBufferRef mirrored_pixel = [[FUManager shareInstance] dealTheFrontCameraPixelBuffer:pixelBuffer returnNewBuffer:false];
    [[FUManager shareInstance] renderBodyTrackWithBuffer:mirrored_pixel
                                                     ptr:nil
                                              RenderMode:FURenderPreviewMode
                                               Landmarks:landmarks
                                         LandmarksLength:landmarks_cnt];
    srcFrame.pixelBuffer = mirrored_pixel;
    *dstFrame = srcFrame;
    return YES;
}

- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode {
    return AgoraVideoFrameProcessModeReadWrite;
}
@end
