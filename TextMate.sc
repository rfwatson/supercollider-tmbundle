TextMate {
  classvar menu, <openClassInTextMate, <openReferencesInTextMate;
  
  *initClass {
    var opt;
        
    menu = CocoaMenuItem(nil, 7, "TextMate", true);

    opt = CocoaMenuItem(menu, 0, "TextMate to front", false) {
      "osascript << END
      tell application \"TextMate\" to activate
      END
      ".unixCmd(postOutput: false)
    };
    opt.setShortCut("T");

    openClassInTextMate = CocoaMenuItem(menu, 1, "Open class files in TextMate", false) { |item|
      item.state = item.state.not;
    };
    openClassInTextMate.state = true;
    openClassInTextMate.setShortCut("j", true, true);
    
    openReferencesInTextMate = CocoaMenuItem(menu, 2, "Open references in TextMate", false) { |item|
      item.state = item.state.not;
    };
    openReferencesInTextMate.state = true;
    openReferencesInTextMate.setShortCut("r", true, true);    
  }
}

// http://github.com/rfwatson/sc3ctrl
SC3Controller {
  classvar nodes;

  *initClass {
    nodes = List[];
        
    Platform.case(\osx) {      
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
      node = OSCresponderNode(nil, '/sc3ctrl/cmd') { |t, r, msg|
        {
          thisThread.clock = SystemClock;
          msg[1].asString.interpretPrint;
          postToFront.();
        }.defer
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(nil, '/sc3ctrl/help') { |t, r, msg|
        { 
          msg[1].asString.openHelpFile;
        }.defer
      }.add;
      nodes.add(node);
   
      node = OSCresponderNode(nil, '/sc3ctrl/class') { |t, r, msg|
        // TM version only
        var klass = msg[1].asString;
        var allClasses = Class.allClasses.collect(_.asString);
        
        { 
          if(TextMate.openClassInTextMate.state) {
            if(allClasses.detect{ |str| str == klass }.notNil) { // .includes doesn't work?
              var fname = klass.interpret.filenameSymbol;
              var cmd = "grep -nh \"^" ++ klass ++ "\" \"" ++ fname ++ "\" > /tmp/grepout.tmp";
              cmd.unixCmd(postOutput: false, action: { 
                File.use("/tmp/grepout.tmp", "r") { |f|
                  var content = f.readAllString;
                  var split = content.split($:);
                  if("^[0-9]+$".matchRegexp(split.first.asString)) {
                   ("mate -l" ++ split.first + "\"" ++ fname ++ "\"").postln.unixCmd(postOutput: false);
                  } {
                   ("mate" + fname).unixCmd(postOutput: false);
                  }
                };
              });
            }
          } { // open in SC.app
            klass.interpret.openCodeFile;
          };
        }.defer
      }.add;
      nodes.add(node);

      node = OSCresponderNode(nil, '/sc3ctrl/implementations') { |t, r, msg|
        if(TextMate.openReferencesInTextMate.state) {
          { SC3Controller.methodTemplates(msg[1], true) }.defer
        } { // open in SC.app
          { SC3Controller.methodTemplates(msg[1], false) }.defer
        }      
      }.add;
      nodes.add(node);       
    
      node = OSCresponderNode(nil, '/sc3ctrl/references') { |t, r, msg|
        if(TextMate.openReferencesInTextMate.state) {
          { SC3Controller.methodReferences(msg[1], true) }.defer          
        } { // open in SC.app
          { SC3Controller.methodReferences(msg[1], false) }.defer
        }
      }.add;
      nodes.add(node);

      node = OSCresponderNode(nil, '/sc3ctrl/stop') { |t, r, msg|
        thisProcess.stop; nil;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(nil, '/sc3ctrl/clear') { |t, r, msg|
        { 
          Document.listener.string = ""; ""; 
          postToFront.();
        }.defer;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(nil, '/sc3ctrl/postfront') { |t, r, msg|
        { postToFront.() }.defer;
      }.add;
      nodes.add(node);
    
      node = OSCresponderNode(nil, '/sc3ctrl/recompile') { |t, r, msg|
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
		var out, found = 0, namestring;
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
		    var fname = "/tmp/" ++ Date.seed ++ ".sc";
		    File.use(fname, "w") { |f|
		      f << out.collection.asString;
		      ("mate" + fname).unixCmd(postOutput: false);
		    };
		  } {
			  out.collection.newTextWindow(name.asString);
		  };
		};
	}

  // adapted from Kernel.sc
	*methodReferences { |name, openInTextMate|
		var out, references;
		name = name.asSymbol;
		out = CollStream.new;
		references = Class.findAllReferences(name);

		if (references.notNil, {
			out << "References to '" << name << "' :\n";
			references.do({ arg ref; out << "   " << ref.asString << "\n"; });

		  if(openInTextMate) {
		    var fname = "/tmp/" ++ Date.seed ++ ".sc";
		    File.use(fname, "w") { |f|
		      f << out.collection.asString;
		      ("mate" + fname).unixCmd(postOutput: false);
		    };
		  } {
			  out.collection.newTextWindow(name.asString);
		  };
		},{
			Post << "\nNo references to '" << name << "'.\n";
		});
	}
}