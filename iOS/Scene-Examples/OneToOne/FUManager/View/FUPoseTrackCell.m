//
//  FUPoseTrackCell.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/8.
//

#import "FUPoseTrackCell.h"

@implementation FUPoseTrackCell


- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self.layer.masksToBounds = YES;
        self.layer.cornerRadius = 8.0;
        _imageView = [UIImageView new];
        [self.contentView addSubview:_imageView];
        
        _imageView.translatesAutoresizingMaskIntoConstraints = false;
        [NSLayoutConstraint activateConstraints:@[[_imageView.leftAnchor constraintEqualToAnchor:self.contentView.leftAnchor],
                                                  [_imageView.rightAnchor constraintEqualToAnchor:self.contentView.rightAnchor],
                                                  [_imageView.topAnchor constraintEqualToAnchor:self.contentView.topAnchor],
                                                  [_imageView.bottomAnchor constraintEqualToAnchor:self.contentView.bottomAnchor]]];
    }
    return self;
}



@end
