package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.Proposal;

import java.util.List;
import java.util.Optional;

public class TestConcreteContractNetInitiator extends ContractNetInitiator<Proposal> {
    public TestConcreteContractNetInitiator() {
        super();
    }

    @Override
    public Optional<Proposal> findBestProposal(List<Proposal> props,
            Proposal description) {
        return Optional.of(props.get(0));
    }

    @Override
    protected void signalNoSolutionFound() {
    }

    @Override
    public Optional<Proposal> getWorkUnitDescription() {
        Proposal p = new Proposal() {
        };
        return Optional.of(p);
    }

    @Override
    public void notifyWorkDone(Proposal prop) {
    }

    @Override
    public Proposal updateWorkDescription(Proposal best) {
        return best;
    }
}
