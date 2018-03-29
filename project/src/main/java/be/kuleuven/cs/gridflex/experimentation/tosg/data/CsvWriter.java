package be.kuleuven.cs.gridflex.experimentation.tosg.data;

import com.google.common.base.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Abstract csv writer class.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
final class CsvWriter {

    private static final Logger logger = LoggerFactory.getLogger(EgtCsvResultWriter.class);
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final char DELIMITER = ';';

    private CsvWriter() {
    }

    public static void writeCsvFile(String fileName,
            List<? extends Printable> results,
            boolean append, Object[] fileHeader) {
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR)
                .withDelimiter(DELIMITER);
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(fileName, append), Charsets.UTF_8)) {
            CSVPrinter csvPrinter = new CSVPrinter(fileWriter, csvFileFormat);
            if (!append) {
                csvPrinter.printRecord(fileHeader);
            }
            for (Printable res : results) {
                csvPrinter.printRecord(res.getValues());
            }

            fileWriter.flush();
            csvPrinter.close();

            if (logger.isInfoEnabled()) {
                logger.info("Output written and resources released.");
            }
        } catch (IOException e) {
            logger.error("Error writing to csv file.", e);
        }
    }
}
