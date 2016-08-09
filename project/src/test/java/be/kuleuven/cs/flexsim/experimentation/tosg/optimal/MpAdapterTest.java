package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpVariable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MpAdapterTest {

    private static int interAct = 20;
    private static int actDuration = 8;
    private static int maxAct = 4;
    private static int profileSize = 500;

    private FlexConstraints target;
    private MpAdapter adapt;

    @Before
    public void setUp() throws Exception {
        this.target = FlexConstraints.create(interAct, actDuration, maxAct);
        this.adapt = new MpAdapter(target, 500);
    }

    @Test
    public void getDVarTest() {
        List<MpVariable> vars = adapt.getDVars();
        assertEquals(maxAct, vars.size(), 0);
    }

    @Test
    public void getConstraintsTest() {
        List<MpConstraint> ctrs = adapt.getConstraints();
        int diffNbConstraints = 1;
        assertEquals(diffNbConstraints * (maxAct - 1), ctrs.size(), 0);
    }

}