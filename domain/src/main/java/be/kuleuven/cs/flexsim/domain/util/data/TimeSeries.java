package be.kuleuven.cs.flexsim.domain.util.data;

import it.unimi.dsi.fastutil.doubles.DoubleList;

/**
 * A standard time series handles a set of time and value pairs where each time
 * occurs at most once. Basic operations ont hese time series are also provided.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public interface TimeSeries {

    /**
     * Calculates the mean of the values in these series.
     * 
     * @return the mean.
     */
    double mean();

    /**
     * Find the median of the values in these series.
     * 
     * @return the median.
     */
    double median();

    /**
     * Find the standard deviation of the values in these series.
     * 
     * @return the standard deviation.
     */
    double std();

    /**
     * Returns the values of these time series as an array.
     * 
     * @return the values.
     */
    DoubleList values();

    /**
     * Returns the value at a certain index in the time series.
     * 
     * @param index
     *            an index between [0..timeseries.length()-1];
     * @return the corresponding value.
     */
    double value(int index);

    /**
     * @return Returns the length of this time series.
     */
    int length();

    /**
     * @return Returns the max value of this series.
     */
    double max();

    /**
     * @return Returns the sum of the values in this series.
     */
    double sum();
}