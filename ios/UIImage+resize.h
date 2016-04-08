//
//  UIImage+resize.h
//  RCTCamera
//
//  Created by Andrew Petrus on 07.04.16.
//
//

#import <UIKit/UIKit.h>

@interface UIImage (resize)

+ (UIImage *)resizeImage:(UIImage *)image withMaxSize:(CGSize)maxSize compressForMaxAmountOfBytes:(NSUInteger)maxAmountOfBytes;

@end
