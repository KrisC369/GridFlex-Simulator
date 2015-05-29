package be.kuleuven.cs.flexsim.domain.workstation;

/**
 * This workstation is configurable in terms of Energy consumption parameters.
 * 
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 * 
 */
interface ConfigurableWorkstation extends Workstation {

    /**
     * Change the resource processor to use.
     * 
     * @param proc
     *            the resource processor
     */
    void setProcessor(Processor proc);

    /**
     * Decrease the fixed energy consumption param.
     * 
     * @param shift
     *            the amount to shift.
     */
    void decreaseFixedECons(int shift);

    /**
     * Increase the fixed energy consumption param.
     * 
     * @param shift
     *            the amount to shift.
     */
    void increaseFixedECons(int shift);

    /**
     * Decrease the maximum rated variable energy consumption param.
     * 
     * @param shift
     *            the amount to shift.
     */
    void decreaseRatedMaxVarECons(int shift);

    /**
     * Increase the maximum rated variable energy consumption param.
     * 
     * @param shift
     *            the amount to shift.
     */
    void increaseRatedMaxVarECons(int shift);

}
