package be.kuleuven.cs.gridflex.domain.energy.generation.wind;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class TurbineSpecificationTest {
    private static final String targetfile = "specs_enercon_e101-e1.csv";
    private TurbineSpecification target;

    @Test
    public void loadFromSpecTest() throws Exception {
        target = TurbineSpecification.loadFromResource(targetfile);
        assertEquals(26, target.getPowerValues().size(), 0);
    }

}

