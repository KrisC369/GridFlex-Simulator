#!/usr/bin/env ruby
#
#Script for running the consolidator. Give 1 argument: the output filename.
#
if(ARGV.size < 1)
  puts "You must give minimum two arguments."
  exit 
end
 
pathOut = "persistence/"
pathIn = "persistence/write/"
target = "tosg.utils.DBConsolidatorRunner"
@files = Dir.entries(pathIn).select {|f| !File.directory? f}
outfile = ARGV.first()

#@files = ARGV.drop(1)

args = "#{pathOut}#{outfile}"
@files.each do |f|
  args << " #{pathIn}#{f}"
end

execCmd = "mvn exec:java -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.#{target} -Dexec.classpathScope=runtime -Dexec.args=\"#{args}\""

puts "running consolidator on all files found in #{pathIn} and writing to #{pathOut}#{outfile}"
exec execCmd
