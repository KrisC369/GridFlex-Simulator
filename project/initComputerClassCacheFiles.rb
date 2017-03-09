# require '../../JPPFRunner/computerlist'
#AllComputers = ['aalst','heist','aubel']
path = "/home/u0091633/gitworkspace/JPPFRunner/jppf/current/node/persistence/write/"
filebase = "memo.db_"
target = "tosg.utils.WriteFileCreator"
args = ""
AllComputers.each do |comp|
  args << "#{path}#{filebase}#{comp} "
end
execCmd = "mvn exec:java -Dexec.mainClass=be.kuleuven.cs.flexsim.experimentation.#{target} -Dexec.classpathScope=runtime -Dexec.args=\"#{args}\""
puts "Creating caching files for all hosts found in computers.rb."
puts "Running command #{execCmd}"
exec execCmd
