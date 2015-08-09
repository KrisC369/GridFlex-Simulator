package be.kuleuven.cs.flexsim.domain.util;

import java.io.File;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;

/**
 * A time series representation of a power congestion profile.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CongestionProfile implements TimeSeries {

    /*
     * (non-Javadoc)
     * 
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#mean()
     */
    @Override
    public long mean() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#median()
     */
    @Override
    public long median() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#std()
     */
    @Override
    public long std() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#load(java.io.File)
     */
    @Override
    public void load(File file) {
        // TODO Auto-generated method stub

    }

    /**
     * Factory method for building a time series from a csv file.
     * 
     * @param filename
     *            The filename.
     * @return the time series.
     */
    public static TimeSeries createFromCSV(String filename) {
        CongestionProfile cp = new CongestionProfile();
        cp.load(new File(filename));
        return cp;
    }
}
