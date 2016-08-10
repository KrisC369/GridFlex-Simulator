package be.kuleuven.cs.flexsim.experimentation.tosg.optimal;

import be.kuleuven.cs.flexsim.experimentation.tosg.optimal.dso.MpDsoAdapter;
import com.google.common.collect.Lists;
import net.sf.jmpi.main.MpConstraint;
import net.sf.jmpi.main.MpVariable;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class MpDsoAdapterTest {

    private static int interAct = 20;
    private static int actDuration = 8;
    private static int maxAct = 4;
    private static int profileSize = 500;

    private FlexConstraints target;
    private MpDsoAdapter adapt;

    @Before
    public void setUp() throws Exception {
        this.target = FlexConstraints.builder().interActivationTime(interAct)
                .interActivationTime(actDuration)
                .maximumActivations(maxAct).build();
        List<String> id = Lists.newArrayList();
        for (int i = 0; i < profileSize; i++) {
            id.add(String.valueOf(i));
        }
        this.adapt = new MpDsoAdapter(target, id);
    }

    @Test
    public void getDVarTest() {
        List<MpVariable> vars = adapt.getDVars();
        assertEquals(0, vars.size(), 0);
    }

    @Test
    public void getConstraintsTest() {
        List<MpConstraint> ctrs = adapt.getConstraints();
        int diffNbConstraints = 500;
        assertEquals(diffNbConstraints * (maxAct - 1), ctrs.size(), 20);
    }

}