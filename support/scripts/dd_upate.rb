#!/usr/bin/env ruby
#kdo-1101-2
src_folder = ARGV.shift

dd_folders = `find #{src_folder} -type d -name datadictionary`;
dd_folders += `find #{src_folder} -type d -name accesssecurity`;
dd_folders += `find #{src_folder} -type d -name overrides`;

#puts dd_folders.inspect

dd_folders.split("\n").each do |dd_folder| 
  subfolder = dd_folder[dd_folder.rindex("org/kuali/kfs"), dd_folder.length];
  puts "mkdir -p src/test/resources/#{subfolder}";
  `mkdir -p src/test/resources/#{subfolder}`;

  puts "cp #{dd_folder}/*.xml src/test/resources/#{subfolder}";
  `cp #{dd_folder}/*.xml src/test/resources/#{subfolder}`;

end
