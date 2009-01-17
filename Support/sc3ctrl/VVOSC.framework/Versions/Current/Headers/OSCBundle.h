//
//  OSCBundle.h
//  OSC
//
//  Created by bagheera on 9/20/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

#if IPHONE
#import <UIKit/UIKit.h>
#else
#import <Cocoa/Cocoa.h>
#endif

#import "OSCMessage.h"




@interface OSCBundle : NSObject {
	NSMutableArray		*elementArray;	//	array of messages or bundles
}

+ (void) parseRawBuffer:(unsigned char *)b ofMaxLength:(int)l toInPort:(id)p;

+ (id) create;

- (void) addElement:(id)n;
- (void) addElementArray:(NSArray *)a;

- (int) bufferLength;
- (void) writeToBuffer:(unsigned char *)b;

@end
