//
//  OSCInPort.h
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


//#import <sys/types.h>
//#import <sys/socket.h>
#import <netinet/in.h>

#import <pthread.h>
#import "AddressValPair.h"
#import "OSCPacket.h"
#import "OSCBundle.h"
#import "OSCMessage.h"


@protocol OSCInPortDelegateProtocol
- (void) oscMessageReceived:(NSDictionary *)d;
- (void) receivedOSCVal:(id)v forAddress:(NSString *)a;
@end

@protocol OSCDelegateProtocol
- (void) oscMessageReceived:(NSDictionary *)d;
- (void) receivedOSCVal:(id)v forAddress:(NSString *)a;
@end


@interface OSCInPort : NSObject {
	BOOL					deleted;	//	whether or not i'm deleted- ensures that socket gets closed
	BOOL					bound;		//	whether or not the socket is bound
	int						sock;		//	socket file descriptor.  remember, everything in unix is files!
	struct sockaddr_in		addr;		//	struct that describes *my* address (this is an in port)
	unsigned short			port;		//	the port number i'm receiving from
	BOOL					running;	//	whether or not i should keep running
	BOOL					busy;
	unsigned char			buf[8192];	//	the socket gets data and dumps it here immediately
	
	pthread_mutex_t			lock;
	NSTimer					*threadTimer;
	int						threadTimerCount;
	NSAutoreleasePool		*threadPool;
	
	NSString				*portLabel;	//	the "name" of the port (added to distinguish multiple osc input ports for bonjour)
	NSNetService			*zeroConfDest;	//	bonjour service for publishing this input's address...only active if there's a portLabel!
	
	NSMutableDictionary		*scratchDict;	//	key of dict is address port; object at key is a mut. array.  coalesced messaging.
	NSMutableArray			*scratchArray;	//	array of AddressValPair objects.  used for serial messaging.
	id						delegate;	//	my delegate gets notified of incoming messages
}

+ (id) createWithPort:(unsigned short)p;
+ (id) createWithPort:(unsigned short)p labelled:(NSString *)n;
- (id) initWithPort:(unsigned short)p;
- (id) initWithPort:(unsigned short)p labelled:(NSString *)n;

- (void) prepareToBeDeleted;

- (NSDictionary *) createSnapshot;

- (BOOL) createSocket;
- (void) start;
- (void) stop;
- (void) launchOSCLoop:(id)o;
- (void) OSCThreadProc:(NSTimer *)t;
- (void) parseRawBuffer:(unsigned char *)b ofMaxLength:(int)l;

//	if the delegate im
- (void) handleParsedScratchDict:(NSDictionary *)d;
- (void) handleScratchArray:(NSArray *)a;

- (void) addValue:(id)val toAddressPath:(NSString *)p;

- (unsigned short) port;
- (void) setPort:(unsigned short)n;
- (NSString *) portLabel;
- (void) setPortLabel:(NSString *)n;
- (NSNetService *) zeroConfDest;
- (BOOL) bound;

- (id) delegate;
- (void) setDelegate:(id)n;

@end
