//
//  OSCPacket.h
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

#include <stdio.h>
#import "OSCBundle.h"
#import "OSCMessage.h"

/*
	this class requires a bundle or message on create/init.  the buffer/msg
	is NOT retained by this class in any way- it's used to immediately create
	the buffer which will be sent.
*/

@interface OSCPacket : NSObject {
	int					bufferLength;
	unsigned char		*payload;
}

+ (void) parseRawBuffer:(unsigned char *)b ofMaxLength:(int)l toInPort:(id)p;
+ (id) createWithContent:(id)c;
- (id) initWithContent:(id)c;

- (int) bufferLength;
- (unsigned char *) payload;

@end
