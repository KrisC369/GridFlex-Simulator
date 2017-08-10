package be.kuleuven.cs.gridflex.experimentation.tosg.data;

import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EgtCsvResultWriterTest {

    @Test
    public void writeCsvFile() throws Exception {
        double[] fixedPoints = new double[] { 0.2, 0.3 };
        double pp = 43.2;
        String file = "testOut.csv";
        EgtCsvResultWriter.WgmfDynamicsResults wgmfDynamicsResults = EgtCsvResultWriter
                .WgmfDynamicsResults
                .create(2, 4, "TestData", pp, fixedPoints, fixedPoints, fixedPoints, fixedPoints,
                        fixedPoints, fixedPoints, 0.99, new ConfidenceInterval(0, 1, 0.95), 1);
        List<EgtCsvResultWriter.WgmfDynamicsResults> results = Lists
                .newArrayList(wgmfDynamicsResults, wgmfDynamicsResults);
        EgtCsvResultWriter.writeCsvFile(file, results, false);

        File varTmpDir = new File(file);
        boolean exists = varTmpDir.exists();
        assertTrue(exists);
        varTmpDir.delete();
    }

}