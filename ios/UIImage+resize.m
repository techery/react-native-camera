//
//  UIImage+resize.m
//  RCTCamera
//
//  Created by Andrew Petrus on 07.04.16.
//
//

#import "UIImage+resize.h"

@implementation UIImage (resize)

+ (UIImage *)resizeImage:(UIImage *)image withMaxSize:(CGSize)maxSize compressForMaxAmountOfBytes:(NSUInteger)maxAmountOfBytes {
    if (image.size.width > maxSize.width || image.size.height > maxSize.height) {
        CGSize newSize;
        if (image.size.width > image.size.height) {
            newSize = CGSizeMake(maxSize.width, (image.size.height * maxSize.width)/image.size.width);
        } else {
            newSize = CGSizeMake((image.size.width * maxSize.height) / image.size.height, maxSize.height);
        }
        UIGraphicsBeginImageContext(newSize);
        [image drawInRect:CGRectMake(0,0,newSize.width,newSize.height)];
        image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
    }
    CGFloat compression = 1.0f;
    NSData *imageData = UIImageJPEGRepresentation(image, compression);
    while (imageData.length > maxAmountOfBytes) {
        compression = compression - 0.1f;
        imageData = UIImageJPEGRepresentation(image, compression);
    }
    image = [UIImage imageWithData:imageData];
    return image;
}


@end
