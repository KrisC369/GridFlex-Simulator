package be.kuleuven.cs.flexsim.domain.util.data;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.DoubleStream;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class CableCurrentProfile extends AbstractTimeSeriesImplementation<CableCurrentProfile> {

    CableCurrentProfile() {
        super();
    }

    CableCurrentProfile(final DoubleList values) {
        super(values);
    }

    CableCurrentProfile(final double[] values) {
        super(values);
    }

    /**
     * Apply a double to double function to the elements in this profile and return a new profile.
     *
     * @param function The function transformation to apply.
     * @return A new Congestion profile instance.
     */
    CableCurrentProfile transform(DoubleToDoubleFunction function) {
        return new CableCurrentProfile(
                DoubleStream.of(values().toDoubleArray()).map(y -> function.apply(y)).toArray());
    }

    /**
     * Factory method for building a time series from a csv file.
     *
     * @param filename The filename.
     * @param column   The column label to use as data.
     * @return the time series.
     * @throws IOException           If reading from the file is not possible.
     * @throws FileNotFoundException If the file with that name cannot be found.
     */
    public static CableCurrentProfile createFromCSV(final String filename,
            final String column)
            throws IOException {
        final CableCurrentProfile cp = new CableCurrentProfile();
        cp.load(filename, column);
        return cp;
    }

    /**
     * Factory method for building time series from other time series.
     *
     * @param series The series to copy from.
     * @return the time series.
     */
    public static CableCurrentProfile createFromTimeSeries(final TimeSeries series) {
        return new CableCurrentProfile(series.values());
    }

    /**
     * Create a new empty congestion profile.
     *
     * @return
     */
    public static CableCurrentProfile empty() {
        return new CableCurrentProfile();
    }
}
