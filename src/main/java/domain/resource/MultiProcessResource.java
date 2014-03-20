package domain.resource;

/**
 * A resource that can have multiple different processing runs as requirement.
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MultiProcessResource extends SimpleResource {

    int[] neededProcessing;

    /**
     * Constructor instantiating a multiprocess resource instance.
     * 
     * @param needed
     *            the needed processing steps in first run.
     * @param following
     *            the needed processing steps in following runs.
     */
    protected MultiProcessResource(int needed, int... following) {
        super(needed);
        neededProcessing = new int[following.length + 1];
        neededProcessing[0] = 0;
        int count = 1;
        for (int i : following) {
            neededProcessing[count++] = i;
        }
    }

    private int findNonZeroIdx() {
        int result = -1;
        for (int i = 0; result < 0 && i < neededProcessing.length; i++) {
            if (neededProcessing[i] > 0) {
                result = i;
            }
        }
        return result;
    }

    @Override
    public void notifyOfHasBeenBuffered() {
        int firstNonZeroIdx = findNonZeroIdx();
        if (firstNonZeroIdx > 0) {
            setNeededTime(neededProcessing[firstNonZeroIdx]);
            neededProcessing[firstNonZeroIdx] = 0;
        }
    }

}
