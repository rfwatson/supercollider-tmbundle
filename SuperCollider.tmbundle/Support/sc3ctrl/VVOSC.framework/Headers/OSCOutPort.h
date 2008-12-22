//
//  OSCOutPort.h
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


#include <arpa/inet.h>

#import "OSCPacket.h"
#import "OSCBundle.h"
#import "OSCMessage.h"




@interface OSCOutPort : NSObject {
	BOOL					deleted;
	int						sock;
	struct sockaddr_in		addr;
	unsigned short          port;
	NSString				*addressString;
	NSString				*portLabel;	//	used it to distinguish between multiple osc outputs
}

+ (id) createWithAddress:(NSString *)a andPort:(unsigned short)p;
+ (id) createWithAddress:(NSString *)a andPort:(unsigned short)p labelled:(NSString *)l;
- (id) initWithAddress:(NSString *)a andPort:(unsigned short)p;
- (id) initWithAddress:(NSString *)a andPort:(unsigned short)p labelled:(NSString *)l;
- (void) prepareToBeDeleted;

- (NSDictionary *) createSnapshot;

- (BOOL) createSocket;

- (void) sendThisBundle:(OSCBundle *)b;
- (void) sendThisMessage:(OSCMessage *)m;
- (void) sendThisPacket:(OSCPacket *)p;

- (void) setAddressString:(NSString *)n;
- (void) setPort:(unsigned short)p;
- (void) setAddressString:(NSString *)n andPort:(unsigned short)p;

- (NSString *) portLabel;
- (void) setPortLabel:(NSString *)n;

- (unsigned short) port;
- (NSString *) addressString;

@end
