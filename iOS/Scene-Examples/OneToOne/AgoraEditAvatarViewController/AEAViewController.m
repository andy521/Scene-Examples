//
//  AEAViewController.m
//  Scene-Examples
//
//  Created by ZYP on 2022/1/15.
//

#import "AEAViewController.h"
#import "AEAView.h"
#import "AEABottomInfo.h"

@interface AEAViewController ()

@property (nonatomic, strong)AEAView *mainView;

@end

@implementation AEAViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self commonInit];
}

- (void)setupUI {
    
    NSMutableArray *infos = [NSMutableArray new];
    
    
    
    for (NSInteger i = 0; i<12; i++) {
        AEABottomInfo *info = [AEABottomInfo new];
        info.title = [NSString stringWithFormat:@"选%ld", i];
        
        if (i == 1 || i == 2 || i == 3 || i == 5) {
            info.colors = @[UIColor.redColor,
                            UIColor.purpleColor,
                            UIColor.blueColor,
                            UIColor.greenColor,
                            UIColor.blueColor];
        }
        else {
            info.colors = @[];
        }
        
        info.itemSizeType = i == 0 ? AEABottomItemSizeTypeBig : AEABottomItemSizeTypeSmall;
        
        NSMutableArray *items = @[].mutableCopy;
        for (NSInteger j = 0; j < 30; j++) {
            AEABottomInfoItem *item = [AEABottomInfoItem new];
            item.imageName = @"kuzi_changku_1";
            [items addObject:item];
        }
        
        info.items = items.copy;
        info.selectedItemIndex = 0;
        [infos addObject:info];
    }
    
    _mainView = [[AEAView alloc] initWithInfos:infos.copy];
    _mainView.translatesAutoresizingMaskIntoConstraints = false;
    [self.view addSubview:_mainView];
    
    [[_mainView.leftAnchor constraintEqualToAnchor:self.view.leftAnchor] setActive:true];
    [[_mainView.rightAnchor constraintEqualToAnchor:self.view.rightAnchor] setActive:true];
    [[_mainView.topAnchor constraintEqualToAnchor:self.view.topAnchor] setActive:true];
    [[_mainView.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor] setActive:true];
}

- (void)commonInit {}

@end
