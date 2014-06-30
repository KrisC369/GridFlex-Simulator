package be.kuleuven.cs.flexsim.domain.workstation;

interface ConfigurableWorkstation extends Workstation {

    void setProcessor(Processor proc);

    void decreaseFixedECons(int shift);

    void increaseFixedECons(int shift);

    void decreaseRatedMaxVarECons(int shift);

    void increaseRatedMaxVarECons(int shift);

}
