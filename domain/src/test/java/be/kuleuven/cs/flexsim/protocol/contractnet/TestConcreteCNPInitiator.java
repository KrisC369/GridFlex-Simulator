package be.kuleuven.cs.flexsim.protocol.contractnet;

import java.util.List;

import com.google.common.base.Optional;

import be.kuleuven.cs.flexsim.protocol.Proposal;

public class TestConcreteCNPInitiator extends CNPInitiator<Proposal> {
    public TestConcreteCNPInitiator() {
        super();
    }

    @Override
    public Optional<Proposal> findBestProposal(List<Proposal> props,
            Proposal description) {
        return Optional.fromNullable(props.get(0));
    }

    @Override
    protected void signalNoSolutionFound() {
    }

    @Override
    public Optional<Proposal> getWorkUnitDescription() {
        Proposal p = new Proposal() {
        };
        return Optional.fromNullable(p);
    }

    @Override
    public void notifyWorkDone(Proposal prop) {
    }

    @Override
    public Proposal updateWorkDescription(Proposal best) {
        return best;
    }
}
