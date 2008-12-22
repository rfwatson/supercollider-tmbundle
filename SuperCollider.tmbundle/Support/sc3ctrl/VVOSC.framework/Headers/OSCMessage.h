//
//  OSCMessage.h
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

#import <pthread.h>



@interface OSCMessage : NSObject {
	NSString			*address;
	NSMutableArray		*typeArray;
	NSMutableArray		*argArray;
	pthread_rwlock_t	lock;
}

+ (void) parseRawBuffer:(unsigned char *)b ofMaxLength:(int)l toInPort:(id)p;
+ (id) createMessageToAddress:(NSString *)a;
- (id) initWithAddress:(NSString *)a;

- (void) addInt:(int)n;
- (void) addFloat:(float)n;
#if IPHONE
- (void) addColor:(UIColor *)c;
#else
- (void) addColor:(NSColor *)c;
#endif
- (void) addBOOL:(BOOL)n;
- (void) addString:(NSString *)n;

- (int) bufferLength;
- (void) writeToBuffer:(unsigned char *)b;

@end
