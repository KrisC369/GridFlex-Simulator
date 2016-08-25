package be.kuleuven.cs.flexsim.solver.optimal.dso;

import be.kuleuven.cs.flexsim.domain.energy.dso.r3dp.HourlyFlexConstraints;
import be.kuleuven.cs.flexsim.solver.optimal.ConstraintConversion;
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

    private static int interAct = 5;
    private static int actDuration = 2;
    private static int maxAct = 4;
    private static int profileSize = 500;

    private HourlyFlexConstraints target;
    private MpDsoAdapter adapt;

    @Before
    public void setUp() throws Exception {
        this.target = HourlyFlexConstraints.builder().interActivationTime(interAct)
                .interActivationTime(actDuration)
                .maximumActivations(maxAct).build();
        List<String> id = Lists.newArrayList();
        for (int i = 0; i < profileSize; i++) {
            id.add(String.valueOf(i));
        }
        this.adapt = new MpDsoAdapter(ConstraintConversion.fromHourlyToQuarterHourly(target), id);
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
        assertEquals(diffNbConstraints * (maxAct - 1), ctrs.size(), 30);
    }

}