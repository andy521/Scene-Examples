//
//  VideoFrameHandler.h
//  Scene-Examples
//
//  Created by ZYP on 2022/1/7.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol VideoFrameHandlerDelegate <NSObject>

- (void)videoHandlerDidRecvPixelData:(CVPixelBufferRef)pixelBuffer;

@end

@interface VideoFrameHandler : NSObject<AgoraVideoFrameDelegate>

- (BOOL)onCaptureVideoFrame:(AgoraOutputVideoFrame *)srcFrame
                   dstFrame:(AgoraOutputVideoFrame * _Nullable __autoreleasing *)dstFrame;
- (AgoraVideoFrameProcessMode)getVideoFrameProcessMode;

@property (nonatomic, weak)id<VideoFrameHandlerDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
