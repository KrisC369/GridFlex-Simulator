package be.kuleuven.cs.flexsim.domain.process;

/**
 * Data transfer object for returning pairs of results concerning flexibility
 * calculation.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
interface FlexDTO<T1, T2> {
    /**
     * Return first result.
     * 
     * @return first result.
     */
    T1 getFirst();

    /**
     * Return second result.
     * 
     * @return second result.
     */
    T2 getSecond();

}
