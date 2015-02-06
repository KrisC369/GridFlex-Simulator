package be.kuleuven.cs.gametheory.experimentation.io;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Writes the results from experimentation to certain outputs (eg. a logger).
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public class ResultWriter {
    private final Logger logger;
    private final Writable target;
    private final Map<String, String> resultComponents;

    /**
     * Default constructor.
     *
     * @param target
     *            The target to take results from.
     */
    public ResultWriter(Writable target) {
        this(target, "RESULTS");
    }

    /**
     * Constructor creating new logger programmatically.
     *
     * @param target
     *            The target to take results from.
     * @param filename
     *            The filename for the logger.
     */
    public ResultWriter(Writable target, String filename) {
        logger = LoggerFactory.getLogger(filename);
        this.target = target;
        this.resultComponents = Maps.newLinkedHashMap();
    }

    /**
     * Write the outputs of the target.
     */
    public synchronized void write() {
        writeToLogger();
    }

    private void writeToLogger() {
        StringBuilder b = new StringBuilder();
        b.append("Writing results:\n");
        b.append(target.getFormattedResultString());
        b.append("----------------\n");
        if (!this.resultComponents.isEmpty()) {
            b.append("Other result components:\n");
            for (Entry<String, String> entry : resultComponents.entrySet()) {
                b.append(entry.getKey()).append(":").append(entry.getValue())
                        .append("\n");
            }
        }
        b.append("----------------\n");
        logger.info(b.toString());
    }

    /**
     * Add a key/value string component to this result writer.
     *
     * @param key
     *            the string indicating the key.
     * @param value
     *            the value for the representing key.
     */
    public void addResultComponent(String key, String value) {
        this.resultComponents.put(key, value);
    }
}
