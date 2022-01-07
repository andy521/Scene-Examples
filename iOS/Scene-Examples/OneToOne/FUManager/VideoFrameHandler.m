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
    /// 这里会new一个 CVPixelBufferRef
    CVPixelBufferRef mirrored_pixel = [[FUManager shareInstance] dealTheFrontCameraPixelBuffer:pixelBuffer returnNewBuffer:YES];
    [[FUManager shareInstance] renderARFilterItemWithBuffer:mirrored_pixel];
    srcFrame.pixelBuffer = mirrored_pixel;
    *dstFrame = srcFrame;
    return YES;
}

- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode {
    return AgoraVideoFrameProcessModeReadWrite;
}
@end
