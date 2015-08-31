package be.kuleuven.cs.flexsim.protocol.contractnet;

import java.util.List;

import be.kuleuven.cs.flexsim.protocol.Proposal;

public class TestConcreteCNPInitiator extends CNPInitiator<Proposal> {
    public TestConcreteCNPInitiator() {
        super();
    }

    @Override
    public Proposal findBestProposal(List<Proposal> props, Proposal description) {
        return props.get(0);
    }

    @Override
    protected void signalNoSolutionFound() {
    }

    @Override
    public Proposal getWorkUnitDescription() {
        return new Proposal() {
        };
    }

    @Override
    public void notifyWorkDone(Proposal prop) {
    }
}
