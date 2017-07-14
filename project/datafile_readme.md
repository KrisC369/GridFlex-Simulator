Multiple data files are used to serve as input for the simulator. 
This readme catalogues the different files.
Power forecast error distribution profiles: 
    -[0]: ERCOT US, day ahead (EDA), normally distributed
    -[1]: EDA distribution * 2, normally distributed
    -[2]: EDA distribution / 2, normally distributed
    -[3]: ERCOT, day ahead 15 min averages, cauchy distributed
    -[4]: Artificial to match EDA[0] with (wrongful) scaling by /3.6 in total volume, cauchy distributed
    -[5]:

Input power production data files:
    -[0]: Antwerp harbor data profile.
    -[2]: Zeebrugge data profile.
