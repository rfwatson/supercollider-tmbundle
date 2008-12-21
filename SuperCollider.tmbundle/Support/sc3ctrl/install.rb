#!/usr/bin/env ruby

puts "Ready to install sc3ctrl."
puts "Press ENTER to continue."
gets

puts "Creating executable in /usr/local/bin .."
%x{echo "`pwd`/sc3ctrl \\$1 \\$2" > /usr/local/bin/sc3ctrl}
%x{chmod a+x /usr/local/bin/sc3ctrl}
puts "Done."

puts
puts "Copying SuperCollider class to Extensions folder .."
system 'cp SC3Controller.sc ~/Library/Application\ Support/SuperCollider/Extensions/'
puts "Done."

puts 