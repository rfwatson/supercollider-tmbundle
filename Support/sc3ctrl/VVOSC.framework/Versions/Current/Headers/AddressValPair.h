//
//  AddressValPair.h
//  VVOSC
//
//  Created by bagheera on 12/11/08.
//  Copyright 2008 __MyCompanyName__. All rights reserved.
//

#if IPHONE
#import <UIKit/UIKit.h>
#else
#import <Cocoa/Cocoa.h>
#endif



@interface AddressValPair : NSObject {
	NSString		*address;
	id				val;
}

+ (id) createWithAddress:(NSString *)a val:(id)v;
- (id) initWithAddress:(NSString *)a val:(id)v;

- (NSString *) address;
- (id) val;

@end
