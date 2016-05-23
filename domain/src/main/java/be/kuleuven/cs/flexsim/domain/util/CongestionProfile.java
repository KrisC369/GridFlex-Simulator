package be.kuleuven.cs.flexsim.domain.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;

/**
 * A time series representation of a power congestion profile.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CongestionProfile implements TimeSeries {

    DoubleList dValues;

    @Nullable
    private Double maxcache = null;
    @Nullable
    private Double sumcache = null;

    CongestionProfile() {
        dValues = new DoubleArrayList();
    }

    CongestionProfile(DoubleList values) {
        dValues = new DoubleArrayList(values);
    }

    CongestionProfile(double[] values) {
        dValues = new DoubleArrayList(values);
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#mean()
     */
    @Override
    public double mean() {
        return applyStatistic(new Mean());
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#median()
     */
    @Override
    public double median() {
        return applyStatistic(new Median());
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#std()
     */
    @Override
    public double std() {
        return applyStatistic(new StandardDeviation());
    }

    /**
     * Load and parse time series from file.
     * 
     * @param filename
     *            the name of the file to parse and load.
     * @param column
     *            the label of the column to parse and use as time series.
     * @throws IOException
     *             When loading is not possible for whatever reason.
     */
    public void load(String filename, String column) throws IOException {
        List<Double> dataRead = Lists.newArrayList();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(
                new FileInputStream(file), Charset.defaultCharset())).build();
        String[] nextLine = reader.readNext();
        int key = -1;
        for (int i = 0; i < nextLine.length; i++) {
            if (nextLine[i].equalsIgnoreCase(column)) {
                key = i;
                break;
            }
        }

        while ((nextLine = reader.readNext()) != null) {
            dataRead.add(Double.parseDouble(nextLine[key]));
        }
        dValues = new DoubleArrayList();
        dValues.addAll(dataRead);
        resetCache();
    }

    /**
     * Factory method for building a time series from a csv file.
     * 
     * @param filename
     *            The filename.
     * @param column
     *            The column label to use as data.
     * @return the time series.
     * @throws IOException
     *             If reading from the file is not possible.
     * @throws FileNotFoundException
     *             If the file with that name cannot be found.
     */
    public static TimeSeries createFromCSV(String filename, String column)
            throws IOException {
        CongestionProfile cp = new CongestionProfile();
        cp.load(filename, column);
        return cp;
    }

    /**
     * Factory method for building time series from other time series.
     * 
     * @param series
     *            The series to copy from.
     * @return the time series.
     */
    public static CongestionProfile createFromTimeSeries(TimeSeries series) {
        return new CongestionProfile(series.values());
    }

    @Override
    public DoubleList values() {
        return DoubleLists.unmodifiable(dValues);
    }

    @Override
    public double value(int index) {
        checkArgument(index >= 0 && index < length());
        return dValues.getDouble(index);
    }

    @Override
    public int length() {
        return this.dValues.size();
    }

    /**
     * Change the value of a certain element in the time series.
     * 
     * @param index
     *            the index of the value to change.
     * @param value
     *            the new value.
     */
    public void changeValue(int index, double value) {
        checkArgument(index >= 0 && index < length(),
                "Index(" + index + ") should be within range of time series.");
        this.dValues.set(index, value);
        resetCache();
    }

    private void resetCache() {
        this.sumcache = null;
        this.maxcache = null;
    }

    @Override
    public double max() {
        if (this.maxcache != null) {
            return maxcache;
        }
        maxcache = applyStatistic(new Max());
        return maxcache;
    }

    @Override
    public double sum() {
        if (this.sumcache != null) {
            return sumcache;
        }
        sumcache = applyStatistic(new Sum());
        return sumcache;
    }

    private double applyStatistic(AbstractUnivariateStatistic stat) {
        stat.setData(dValues.toDoubleArray());
        return stat.evaluate();
    }
}
