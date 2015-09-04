package be.kuleuven.cs.flexsim.domain.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import com.google.common.collect.Lists;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import be.kuleuven.cs.flexsim.domain.util.data.TimeSeries;

/**
 * A time series representation of a power congestion profile.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public class CongestionProfile implements TimeSeries {

    double[] dataValues;

    CongestionProfile() {
        dataValues = new double[] {};
    }

    CongestionProfile(double[] values) {
        dataValues = values;
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#mean()
     */
    @Override
    public double mean() {
        Mean mean = new Mean();
        mean.setData(values());
        return mean.evaluate();
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#median()
     */
    @Override
    public double median() {
        Median med = new Median();
        med.setData(values());
        return med.evaluate();
    }

    /*
     * (non-Javadoc)
     * @see be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#std()
     */
    @Override
    public double std() {
        StandardDeviation std = new StandardDeviation();
        std.setData(values());
        return std.evaluate();
    }

    /*
     * (non-Javadoc)
     * @see
     * be.kuleuven.cs.flexsim.domain.util.data.TimeSeries#load(java.io.File)
     */
    @Override
    public void load(String filename, String column)
            throws FileNotFoundException, IOException {
        List<Double> dataRead = Lists.newArrayList();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());
        CSVReader reader = new CSVReaderBuilder(new FileReader(file)).build();
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
        dataValues = new double[dataRead.size()];
        int i = 0;
        for (double d : dataRead) {
            dataValues[i++] = d;
        }

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
            throws FileNotFoundException, IOException {
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
    public double[] values() {
        return Arrays.copyOf(dataValues, dataValues.length);
    }

    @Override
    public double value(int index) {
        checkArgument(index >= 0 && index < dataValues.length);
        return dataValues[index];
    }

    @Override
    public int length() {
        return this.dataValues.length;
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
        this.dataValues[index] = value;
    }
}
