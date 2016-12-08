package be.kuleuven.cs.flexsim.experimentation.tosg.data;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class CsvResultWriterTest {

    @Test
    public void writeCsvFile() throws Exception {
        double[] fixedPoints = new double[] { 0.2, 0.3 };
        double pp = 43.2;
        String file = "testOut.csv";
        CsvResultWriter.WgmfDynamicsResults wgmfDynamicsResults = CsvResultWriter
                .WgmfDynamicsResults
                .create(2, 4, "TestData", pp, fixedPoints, fixedPoints, fixedPoints, fixedPoints,
                        fixedPoints, fixedPoints, 0.99);
        List<CsvResultWriter.WgmfDynamicsResults> results = Lists
                .newArrayList(wgmfDynamicsResults, wgmfDynamicsResults);
        CsvResultWriter.writeCsvFile(file, results);

        File varTmpDir = new File(file);
        boolean exists = varTmpDir.exists();
        assertTrue(exists);
        varTmpDir.delete();
    }

}