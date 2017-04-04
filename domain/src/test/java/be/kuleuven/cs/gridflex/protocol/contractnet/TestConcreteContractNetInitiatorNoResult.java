package be.kuleuven.cs.gridflex.protocol.contractnet;

import be.kuleuven.cs.gridflex.protocol.Proposal;

import java.util.List;
import java.util.Optional;

public class TestConcreteContractNetInitiatorNoResult extends TestConcreteContractNetInitiator {
    private boolean noSolutionFoundTriggered = false;

    public TestConcreteContractNetInitiatorNoResult() {
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
