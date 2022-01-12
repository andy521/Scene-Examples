//
//  FUPoseTrackView.m
//  FUP2A
//
//  Created by L on 2018/8/10.
//  Copyright © 2018年 L. All rights reserved.
//

#import "FUPoseTrackView.h"
#import "FUPoseTrackCell.h"

typedef enum : NSInteger {
    FUPoseTrackCollectionTypeModel,
    FUPoseTrackCollectionTypeInput,
} FUPoseTrackCollectionType;

@interface FUPoseTrackView ()<UICollectionViewDelegate, UICollectionViewDataSource>
{
    NSInteger modelIndex;
}

@property (nonatomic, assign) FUPoseTrackCollectionType collectionType;
@property (nonatomic,assign)NSInteger inputIndex;

@property (strong, nonatomic) UICollectionView *collectionView;
@property (nonatomic, strong) NSArray *modelsArray;
@property (nonatomic, strong) NSArray *inputArray;

@end

@implementation FUPoseTrackView

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.backgroundColor = [UIColor whiteColor];
        UICollectionViewFlowLayout *layout = [UICollectionViewFlowLayout new];
        self.collectionView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:layout];
        [self.collectionView registerClass:[FUPoseTrackCell class] forCellWithReuseIdentifier:@"FUPoseTrackCell"];
        
        [self addSubview:self.collectionView];
        self.collectionView.translatesAutoresizingMaskIntoConstraints = false;
        
        [NSLayoutConstraint activateConstraints:@[[self.collectionView.leftAnchor constraintEqualToAnchor:self.safeAreaLayoutGuide.leftAnchor constant:5],
                                                  [self.collectionView.rightAnchor constraintEqualToAnchor:self.safeAreaLayoutGuide.rightAnchor constant:5],
                                                  [self.collectionView.heightAnchor constraintEqualToConstant:69],
                                                  [self.collectionView.topAnchor constraintEqualToAnchor:self.topAnchor constant:5]]];
        
        
        self.collectionView.delegate = self;
        self.collectionView.dataSource = self;
        self.collectionType = FUPoseTrackCollectionTypeModel;
        modelIndex = 0;
        self.inputIndex = 0;
        self.collectionView.backgroundColor = [UIColor whiteColor];
    }
    return self;
}

- (void)setupData {
    [self reloadData];
    FUAvatar *avatar = FUManager.shareInstance.currentAvatars.firstObject;
    [self selectedModeWith:avatar];
    
    [self setCollectionType:FUPoseTrackCollectionTypeModel];
    [self setInputIndex:1];
    [self.collectionView reloadData];
}

- (void)freshInputIndex:(int)index{
   self.inputIndex = index;
   [self reloadCollection];
}
- (void)selectedModeWith:(FUAvatar *)avatar {
    
    [self reloadData];
    
    modelIndex = 0;
    if ([[FUManager shareInstance].avatarList containsObject:avatar]) {
        modelIndex = [[FUManager shareInstance].avatarList indexOfObject:avatar];
    }
    
    [self.collectionView reloadData];
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [self.collectionView scrollToItemAtIndexPath:[NSIndexPath indexPathForRow:self->modelIndex inSection:0] atScrollPosition:UICollectionViewScrollPositionCenteredHorizontally animated:NO];
    });
}

- (void)reloadData {
    
    self.modelsArray = [FUManager shareInstance].avatarList;
    self.inputArray = @[@"live",@"album",@"input_1"];
}

- (UIImage *)getImage:(NSString *)videoURL{
    
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:[NSURL fileURLWithPath:videoURL] options:nil];
    
    AVAssetImageGenerator *gen = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    
    gen.appliesPreferredTrackTransform = YES;
    
    CMTime time = CMTimeMakeWithSeconds(0.0, 600);
    
    NSError *error = nil;
    
    CMTime actualTime;
    
    CGImageRef image = [gen copyCGImageAtTime:time actualTime:&actualTime error:&error];
    
    UIImage *thumb = [[UIImage alloc] initWithCGImage:image];
    
    CGImageRelease(image);
    
    return thumb;
}

-(void)setCollectionType:(FUPoseTrackCollectionType)collectionType {
    _collectionType = collectionType;
    
    [self.collectionView reloadData];
}

- (void)showCollection:(BOOL)show {
    if (show) {
        self.collectionView.hidden = NO;
        [UIView animateWithDuration:0.35 animations:^{
            self.collectionView.transform = CGAffineTransformIdentity;
        }];
    }else {
        [UIView animateWithDuration:0.35 animations:^{
            self.collectionView.transform = CGAffineTransformMakeTranslation(0, self.collectionView.frame.size.height);
        }completion:^(BOOL finished) {
            self.collectionView.hidden = YES;
        }];
    }
}
- (void)reloadCollection{
    [self.collectionView reloadData];
}
- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    
    switch (_collectionType) {
        case FUPoseTrackCollectionTypeModel:
            return self.modelsArray.count;
            break;
        case FUPoseTrackCollectionTypeInput:
            return self.inputArray.count;
            break;
    }
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    FUPoseTrackCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"FUPoseTrackCell" forIndexPath:indexPath];
    
    switch (_collectionType) {
        case FUPoseTrackCollectionTypeModel:{
            
            FUAvatar *avatar = self.modelsArray[indexPath.row];
            cell.imageView.image = [UIImage imageWithContentsOfFile:avatar.imagePath];
            
            cell.layer.borderColor = modelIndex == indexPath.row ? [UIColor colorWithRed:54/255.0 green:178/255.0 blue:1.0 alpha:1.0].CGColor : [UIColor clearColor].CGColor;
            cell.layer.borderWidth = modelIndex == indexPath.row ? 2.0 : 0.0;
        }
            break;
        case FUPoseTrackCollectionTypeInput:{
            
            if (indexPath.row == 0)
            {
                cell.imageView.image =  [UIImage imageNamed:@"icon_live_55"];
            }
            else if (indexPath.row == 1)
            {
                cell.imageView.image =  [UIImage imageNamed:@"icon_album_55"];
            }
            else if (indexPath.row > 1)
            {
                NSString *path = [[NSBundle mainBundle].resourcePath stringByAppendingFormat:@"/Resource/input_video/%@.mp4",self.inputArray[indexPath.row]];
                
                cell.imageView.image =  [self getImage:path];
            }
            
            cell.layer.borderColor = self.inputIndex == indexPath.row?[UIColor colorWithRed:54/255.0 green:178/255.0 blue:1.0 alpha:1.0].CGColor:[UIColor clearColor].CGColor;
            cell.layer.borderWidth = self.inputIndex == indexPath.row?2.0:0.0;
            
        }
            break;
    }
    
    return cell;
}

-(void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    
    switch (_collectionType) {
        case FUPoseTrackCollectionTypeModel:{
            
            if (modelIndex == indexPath.row) {
                return;
            }
            modelIndex = indexPath.row;
            FUAvatar *avatar = self.modelsArray[indexPath.row];
            [self.collectionView reloadData];
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                if (self.delegate && [self.delegate respondsToSelector:@selector(PoseTrackViewDidSelectedAvatar:)]) {
                    [self.delegate PoseTrackViewDidSelectedAvatar:avatar];
                }
            });
        }
            break;
        case FUPoseTrackCollectionTypeInput:{
            
            if (self.inputIndex != indexPath.row || (self.inputIndex == 1 && self.inputIndex == indexPath.row))  // 可以重新选择相册里面的视频
            {
                NSString *inputName = self.inputArray[indexPath.row];
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    if (self.delegate && [self.delegate respondsToSelector:@selector(PoseTrackViewDidSelectedInput:)]) {
                        [self.delegate PoseTrackViewDidSelectedInput:inputName];
                    }
                });
            }
        }
            break;
    }
}

@end

