package be.kuleuven.cs.flexsim.domain.util.data;

import java.io.File;

/**
 * A standard time series handles a set of time and value pairs where each time
 * occurs at most once. Basic operations ont hese time series are also provided.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public interface TimeSeries {

    /**
     * Calculates the mean of the values in these series.
     * 
     * @return the mean.
     */
    long mean();

    /**
     * Find the median of the values in these series.
     * 
     * @return the median.
     */
    long median();

    /**
     * Find the standard deviation of the values in these series.
     * 
     * @return the standard deviation.
     */
    long std();

    /**
     * Load and parse time series from file.
     * 
     * @param file
     *            the file to parse and load.
     */
    void load(File file);
}
