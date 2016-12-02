package be.kuleuven.cs.flexsim.protocol.contractnet;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CNPResponderTest {
    private CNPResponder subjPos;
    private CNPResponder subjNeg;
    private ContractNetInitiator realInit;
    @Mock
    private Proposal mockProposal;
    @Captor
    private ArgumentCaptor<AnswerAnticipator<Proposal>> answerCaptor2;
    @Mock
    private AnswerAnticipator<Proposal> mockAnswerAnticipator;

    @Before
    public void SetUp() throws Exception {
        subjPos = new ConcretePositiveCNPResponder();
        subjNeg = new ConcreteNegativeCNPResponder();
        realInit = new TestConcreteContractNetInitiator();
        realInit.registerResponder(subjPos);
    }

    @Test
    public void testRecieveCFPPositive() {
        subjPos.callForProposal(mockAnswerAnticipator, mockProposal);
        verify(mockAnswerAnticipator, times(1)).affirmative(any(Proposal.class),
                Matchers.<AnswerAnticipator<Proposal>> any());
    }

    @Test
    public void testRecieveCFPNegative() {
        subjNeg.callForProposal(mockAnswerAnticipator, mockProposal);
        verify(mockAnswerAnticipator, times(1)).reject();
    }

    @Test
    public void testInformDone() {
        final AnswerAnticipator<Proposal> reply = mock(AnswerAnticipator.class);
        subjPos.callForProposal(new AnswerAnticipator<Proposal>() {

            @Override
            public void affirmative(Proposal prop,
                    AnswerAnticipator<Proposal> ant) {
                ant.affirmative(mockProposal, reply);
            }

            @Override
            public void reject() {
            }
        }, mockProposal);
        verify(reply, times(1)).affirmative(any(Proposal.class),
                Matchers.<AnswerAnticipator<Proposal>> any());
    }

    @Test
    public void testInformFailed() {
        final AnswerAnticipator<Proposal> reply = mock(AnswerAnticipator.class);
        subjNeg = new ConcreteFailedCNPResponder();
        subjNeg.callForProposal(new AnswerAnticipator<Proposal>() {

            @Override
            public void affirmative(Proposal prop,
                    AnswerAnticipator<Proposal> ant) {
                ant.affirmative(mockProposal, reply);
            }

            @Override
            public void reject() {
            }
        }, mockProposal);
        verify(reply, times(1)).reject();
    }

    private class ConcretePositiveCNPResponder extends CNPResponder {
        public ConcretePositiveCNPResponder() {
            super();
        }

        @Override
        public Proposal makeProposalForCNP(Proposal arg)
                throws CanNotFindProposalException {
            return new Proposal() {
            };
        }

        @Override
        protected boolean performWorkUnitFor(Proposal arg) {
            return true;
        }
    }

    private class ConcreteNegativeCNPResponder extends CNPResponder {
        public ConcreteNegativeCNPResponder() {
            super();
        }

        @Override
        public Proposal makeProposalForCNP(Proposal arg)
                throws CanNotFindProposalException {
            throw new CanNotFindProposalException();
        }

        @Override
        protected boolean performWorkUnitFor(Proposal arg) {
            return false;
        }
    }

    private class ConcreteFailedCNPResponder extends CNPResponder {
        public ConcreteFailedCNPResponder() {
            super();
        }

        @Override
        public Proposal makeProposalForCNP(Proposal arg)
                throws CanNotFindProposalException {
            return new Proposal() {
            };
        }

        @Override
        protected boolean performWorkUnitFor(Proposal arg) {
            return false;
        }
    }
}
