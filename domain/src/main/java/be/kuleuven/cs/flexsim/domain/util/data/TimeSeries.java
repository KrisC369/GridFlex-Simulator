package be.kuleuven.cs.flexsim.domain.util.data;

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
     * Load and parse time series from file.
     * 
     * @param filename
     *            the name of the file to parse and load.
     * @param column
     *            the label of the column to parse and use as time series.
     * @throws Exception
     *             When loading is not possible for whatever reason.
     */
    void load(String filename, String column) throws Exception;

    /**
     * Returns the values of these timeseries as an array.
     * 
     * @return the values.
     */
    double[] values();

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
     * @return
     */
    double max();
}