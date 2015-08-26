package be.kuleuven.cs.flexsim.protocol.contractnet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import be.kuleuven.cs.flexsim.protocol.AnswerAnticipator;
import be.kuleuven.cs.flexsim.protocol.Proposal;

@RunWith(MockitoJUnitRunner.class)
public class CNPInitiatorTest {

    @Mock
    private CNPResponder mockResp1;
    @Mock
    private CNPResponder mockResp2;
    private CNPInitiator subj;
    @Mock
    private Proposal mockProposal;
    @Captor
    private ArgumentCaptor<AnswerAnticipator<Proposal>> answerCaptor1;
    @Captor
    private ArgumentCaptor<AnswerAnticipator<Proposal>> answerCaptor2;
    @Captor
    private ArgumentCaptor<Proposal> proposalCaptor;

    @Before
    public void setUp() throws Exception {
        subj = new ConcreteCNPInitiator();
        subj.registerResponder(mockResp1);
        subj.registerResponder(mockResp2);
    }

    @Test
    public void testRegister() {
        subj = new ConcreteCNPInitiator();
        subj.registerResponder(mockResp1);
        assertEquals(1, subj.getResponders().size());
        mockResp1 = mock(CNPResponder.class);
        subj.registerResponder(mockResp2);
        assertEquals(2, subj.getResponders().size());
    }

    @Test
    public void testDispatchCNP() {
        subj.sollicitWork(mockProposal);
        verify(mockResp1, times(1)).callForProposal(any(AnswerAnticipator.class), any(Proposal.class));
        verify(mockResp2, times(1)).callForProposal(any(AnswerAnticipator.class), any(Proposal.class));
    }

    @Test
    public void testChooseProposal() {
        boolean flag = false;
        subj.sollicitWork(mockProposal);
        AnswerAnticipator<Proposal> reply1 = mock(AnswerAnticipator.class);
        verify(mockResp1, times(1)).callForProposal(answerCaptor1.capture(), proposalCaptor.capture());
        answerCaptor1.getValue().affirmative(mock(Proposal.class), reply1);

        verify(mockResp2, times(1)).callForProposal(answerCaptor2.capture(), proposalCaptor.capture());
        AnswerAnticipator<Proposal> reply = mock(AnswerAnticipator.class);
        answerCaptor2.getValue().affirmative(mock(Proposal.class), reply);

        verify(reply1, times(1)).affirmative(any(Proposal.class), any(AnswerAnticipator.class));
        verify(reply, times(1)).reject();
    }

    private class ConcreteCNPInitiator extends CNPInitiator {
        public ConcreteCNPInitiator() {
            super();
        }

        @Override
        public Proposal findBestProposal(List<Proposal> props, Proposal description) {
            return props.get(0);
        }
    }
}
