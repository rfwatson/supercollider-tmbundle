TextMate {
  classvar menu, <openClassInTextMate, <openReferencesInTextMate;
  
  *saveState {
    (
      classfiles: openClassInTextMate.state, 
      references: openReferencesInTextMate.state
    ).writeArchive("%/.textmate-settings".format(Platform.userAppSupportDir));
  }

  *initClass {
    var opt;
    var settings;
    var setpath = "%/.textmate-settings".format(Platform.userAppSupportDir);

    if(File.exists(setpath)) {
      settings = Object.readArchive(setpath);
    } {
      settings = (
        classfiles: true,
        references: true
      )
    };
    
    try {
      menu = SCMenuGroup(nil, "TextMate", 7);

      opt = SCMenuItem(menu, "TextMate to front")
        .action_{
          "osascript << END
          tell application \"TextMate\" to activate
          END
          ".unixCmd(postOutput: false)
        }
        .setShortCut("T");
      
      SCMenuSeparator(menu);

      openClassInTextMate = SCMenuItem(menu, "Open class files in TextMate")
        .action_{ |item|
          item.state = item.state.not;
          this.saveState;
        }
        .state_(settings.classfiles);
    
      openReferencesInTextMate = SCMenuItem(menu, "Open references in TextMate") 
        .action_{ |item|
          item.state = item.state.not;
          this.saveState;
        }
        .state_(settings.references);
        
      SCMenuSeparator(menu, 4);
      
      SCMenuItem(menu, "About SuperCollider bundle") 
        .action_{ |item|
          "open 'http://github.com/rfwatson/supercollider-tmbundle'".unixCmd(postOutput:false);
        }
    } { 
      "TextMate found a problem installing CocoaMenuItems - you may be running SC 3.2 or older, or booting from command-line.".warn
    }
  }
}

// adapted from http://github.com/rfwatson/sc3ctrl
SC3Controller {
  classvar nodes, <>matePath;

  *initClass {
    nodes = List[];    
        
    Platform.case(\osx) {
      
      var whichMate = "which mate".unixCmdGetStdOut;
      if(whichMate.isEmpty){
        matePath = "/usr/local/bin/mate";
      } {
        matePath = whichMate;
      };
      
      StartUp.add {
        this.addListeners;
      }
    }
  }

  *addListeners {
    var node, postToFront;
    
    postToFront = {
      Document.listener.front;
    };
    
    if(nodes.isEmpty) {
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/cmd') { |t, r, msg|
        {
          thisThread.clock = SystemClock;
          msg[1].asString.interpretPrint;
          postToFront.();
        }.defer
      }.add;
      nodes.add(node);

      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/help') { |t, r, msg|
        { 
          msg[1].asString.openHelpFile;
        }.defer
      }.add;
      nodes.add(node);
   
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/class') { |t, r, msg|
        // TM version only
        var fname, cmd;
        var klass = msg[1].asString;
        var allClasses = Class.allClasses.collect(_.asString);
        
        { 
          if(TextMate.openClassInTextMate.state) {
            if(allClasses.detect{ |str| str == klass }.notNil) { // .includes doesn't work?
              fname = klass.interpret.filenameSymbol;
              cmd = "grep -nh \"^" ++ klass ++ "\" \"" ++ fname ++ "\" > /tmp/grepout.tmp";
              cmd.unixCmd(postOutput: false, action: { 
                File.use("/tmp/grepout.tmp", "r") { |f|
                  var content = f.readAllString;
                  var split = content.split($:);
                  if("^[0-9]+$".matchRegexp(split.first.asString)) {
                   (matePath ++ " -l"++ split.first + "\"" ++ fname ++ "\"").unixCmd(postOutput: false);
                  } {
                   (matePath ++ " -l1 \"" ++ fname ++ "\"").unixCmd(postOutput: false);
                  };
                  f.close;
                };
                
                
              });
            }
          } { // open in SC.app
            klass.interpret.openCodeFile;
          };
        }.defer
      }.add;
      nodes.add(node);

      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/implementations') { |t, r, msg|
        if(TextMate.openReferencesInTextMate.state) {
          { this.methodTemplates(msg[1], true) }.defer
        } { // open in SC.app
          { this.methodTemplates(msg[1], false) }.defer
        }      
      }.add;
      nodes.add(node);       
    
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/references') { |t, r, msg|
        if(TextMate.openReferencesInTextMate.state) {
          { this.methodReferences(msg[1], true) }.defer          
        } { // open in SC.app
          { this.methodReferences(msg[1], false) }.defer
        }
      }.add;
      nodes.add(node);

      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/stop') { |t, r, msg|
        thisProcess.stop; nil;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/clear') { |t, r, msg|
        { 
          Document.listener.string = ""; ""; 
          postToFront.();
        }.defer;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/postfront') { |t, r, msg|
        { postToFront.() }.defer;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(NetAddr("localhost", nil), '/sc3ctrl/recompile') { |t, r, msg|
        { 
          thisProcess.recompile;
          postToFront.();
        }.defer;
      }.add;
      nodes.add(node);
    }
  }

  *removeAllListeners {
    nodes.do(_.remove);
  }

  // adapated from Kernel.sc
	*methodTemplates { |name, openInTextMate=false|
		var out, found = 0, namestring, fname;
		out = CollStream.new;
		out << "Implementations of '" << name << "' :\n";
		Class.allClasses.do({ arg class;
			class.methods.do({ arg method;
				if (method.name == name, {
					found = found + 1;
					namestring = class.name ++ ":" ++ name;
					out << "   " << namestring << " :     ";
					if (method.argNames.isNil or: { method.argNames.size == 1 }, {
						out << "this." << name;
						if (name.isSetter, { out << "(val)"; });
					},{
						out << method.argNames.at(0);
						if (name.asString.at(0).isAlpha, {
							out << "." << name << "(";
							method.argNames.do({ arg argName, i;
								if (i > 0, {
									if (i != 1, { out << ", " });
									out << argName;
								});
							});
							out << ")";
						},{
							out << " " << name << " ";
							out << method.argNames.at(1);
						});
					});
					out.nl;
				});
			});
		});
		if(found == 0)
		{
			Post << "\nNo implementations of '" << name << "'.\n";
		}
		{
		  if(openInTextMate) {
		    fname = "/tmp/" ++ Date.seed ++ ".sc";
		    File.use(fname, "w") { |f|
		      f << out.collection.asString;
		      (matePath + fname).unixCmd(postOutput: false);
		    };
		  } {
			  out.collection.newTextWindow(name.asString);
		  };
		};
	}

  // adapted from Kernel.sc
	*methodReferences { |name, openInTextMate|
		var out, references, fname;
		name = name.asSymbol;
		out = CollStream.new;
		references = Class.findAllReferences(name);

		if (references.notNil, {
			out << "References to '" << name << "' :\n";
			references.do({ arg ref; out << "   " << ref.asString << "\n"; });

		  if(openInTextMate) {
		    fname = "/tmp/" ++ Date.seed ++ ".sc";
		    File.use(fname, "w") { |f|
		      f << out.collection.asString;
		      (matePath + fname).unixCmd(postOutput: false);
		    };
		  } {
			  out.collection.newTextWindow(name.asString);
		  };
		},{
			Post << "\nNo references to '" << name << "'.\n";
		});
	}
}