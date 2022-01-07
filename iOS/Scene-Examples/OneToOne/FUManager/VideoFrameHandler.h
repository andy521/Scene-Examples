//
//  VideoFrameHandler.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/7.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoFrameHandler : NSObject<AgoraVideoFrameDelegate>

- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)srcFrame dstFrame:(AgoraOutputVideoFrame * _Nullable __autoreleasing *)dstFrame;
- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode;

@end

NS_ASSUME_NONNULL_END
