package be.kuleuven.cs.flexsim.domain.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory instance for creating resource instances.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public final class ResourceFactory {

    private ResourceFactory() {
    }

    /**
     * Creates a list of IResource elements according to the specs specified.
     * 
     * @param amount
     *            the amount of instances to return in the list.
     * @param processingNeeded
     *            a list of needed processing steps per processor, seperated by
     *            comma's.
     * @return A list of IResource instances.
     */
    public static List<Resource> createBulkMPResource(int amount,
            int... processingNeeded) {
        List<Resource> pool = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            pool.add(createResource(processingNeeded));
        }
        return pool;
    }

    /**
     * Creates a resource with the steps needed at each processor as params.
     * 
     * @param neededProcessing
     *            a list of needed processing steps per processor, seperated by
     *            comma's.
     * @return the IResource instance.
     */
    public static Resource createResource(int... neededProcessing) {
        if (neededProcessing.length == 0) {
            return new SimpleResource(0);
        } else if (neededProcessing.length == 1) {
            return new SimpleResource(neededProcessing[0]);
        } else {
            return new MultiProcessResource(neededProcessing[0], Arrays
                    .copyOfRange(neededProcessing, 1, neededProcessing.length));
        }
    }
}
