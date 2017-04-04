package be.kuleuven.cs.gridflex.domain.energy.dso.contractnet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class DSMProposalTest {
    private DSMProposal subjectWithConstraints = mock(DSMProposal.class);
    ;
    private DSMProposal subjectWithoutConstraints = mock(DSMProposal.class);
    private DSMProposal subjectWithOneConstraint = mock(DSMProposal.class);

    @Before
    public void setUp() throws Exception {
        subjectWithConstraints = DSMProposal.create("Mock", 0, 0, 4, 4);
        subjectWithoutConstraints = DSMProposal.create("Mock", 0, 0, null,
                null);
        subjectWithOneConstraint = DSMProposal.create("Mock", 0, 0, 4, null);
    }

    @Test
    public void test() {
        assertTrue(subjectWithConstraints.hasTimeConstraints());
        assertFalse(subjectWithoutConstraints.hasTimeConstraints());
        assertTrue(subjectWithOneConstraint.hasTimeConstraints());
    }

}
