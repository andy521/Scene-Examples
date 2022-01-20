//
//  AEATopView.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/15.
//

#import "AEATopView.h"

@interface AEATopView ()

@property (nonatomic, strong)UIButton *saveButton;

@end

@implementation AEATopView

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        _saveButton = [UIButton new];
        [self setupUI];
    }
    return self;
}

- (void)setupUI {
    _saveButton.backgroundColor = [UIColor colorWithHexColorString:@"6F57EB"];
    [_saveButton setTitle:@"保存" forState:UIControlStateNormal];
    [_saveButton setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    
    [self addSubview:_saveButton];
    _saveButton.translatesAutoresizingMaskIntoConstraints = NO;
    
    [[_saveButton.rightAnchor constraintEqualToAnchor:self.rightAnchor constant:-20] setActive:YES];
    [[_saveButton.widthAnchor constraintEqualToConstant:60] setActive:YES];
    [[_saveButton.heightAnchor constraintEqualToConstant:40] setActive:YES];
    [[_saveButton.bottomAnchor constraintEqualToAnchor:self.bottomAnchor constant:-15] setActive:YES];
    
    _saveButton.layer.cornerRadius = 19.5;
}

@end
