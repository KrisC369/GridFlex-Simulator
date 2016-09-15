package be.kuleuven.cs.flexsim.domain.util.data;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import static com.google.common.base.Preconditions.checkArgument;

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
    default double mean() {
        return applyStatistic(new Mean());
    }

    /**
     * Find the median of the values in these series.
     *
     * @return the median.
     */
    default double median() {
        return applyStatistic(new Median());
    }

    /**
     * Find the standard deviation of the values in these series.
     *
     * @return the standard deviation.
     */
    default double std() {
        return applyStatistic(new StandardDeviation());
    }

    /**
     * Returns the values of these time series as an array.
     *
     * @return the values.
     */
    DoubleList values();

    /**
     * Returns the value at a certain index in the time series.
     *
     * @param index an index between [0..timeseries.length()-1];
     * @return the corresponding value.
     */
    default double value(int index) {
        checkArgument(index >= 0 && index < length());
        return values().getDouble(index);
    }

    /**
     * @return Returns the length of this time series.
     */
    default int length() {
        return this.values().size();
    }

    /**
     * @return Returns the max value of this series.
     */
    default double max() {
        return applyStatistic(new Max());
    }

    /**
     * @return Returns the sum of the values in this series.
     */
    default double sum() {
        return applyStatistic(new Sum());
    }

    /**
     * Apply statistic to the data.
     * This method is a helper method to concretely implemented statistics.
     * Maybe this can method can become private in java 9 so it does not clutter the interface.
     *
     * @param stat The statistic to apply.
     * @return The result.
     */
    default double applyStatistic(final AbstractUnivariateStatistic stat) {
        stat.setData(values().toDoubleArray());
        return stat.evaluate();
    }
}