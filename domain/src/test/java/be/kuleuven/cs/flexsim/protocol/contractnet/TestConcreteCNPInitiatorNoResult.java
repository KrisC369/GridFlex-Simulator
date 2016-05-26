package be.kuleuven.cs.flexsim.protocol.contractnet;

import java.util.List;
import java.util.Optional;

import be.kuleuven.cs.flexsim.protocol.Proposal;

public class TestConcreteCNPInitiatorNoResult extends TestConcreteCNPInitiator {
    private boolean noSolutionFoundTriggered = false;

    public TestConcreteCNPInitiatorNoResult() {
        super();
    }

    @Override
    public Optional<Proposal> findBestProposal(List<Proposal> props,
            Proposal description) {
        return Optional.empty();
    }

    @Override
    protected void signalNoSolutionFound() {
        this.noSolutionFoundTriggered = true;
    }

    public boolean isNoSolutionFoundTriggered() {
        return this.noSolutionFoundTriggered;
    }
}
