package be.kuleuven.cs.flexsim.domain.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.cs.flexsim.domain.process.ProductionLine;
import be.kuleuven.cs.flexsim.domain.util.data.FlexTuple;

public class SiteTest {

    Site s = new SiteImpl();

    @Before
    public void setUp() throws Exception {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
    }

    @Test
    public void testCreationAndAdd() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        assertTrue(s.containsLine(line1));
        assertTrue(s.containsLine(line2));

    }

    @Test
    public void testNoFlex() {
        s = new SiteImpl();
        assertTrue(s.getFlexTuples().isEmpty());
    }

    @Test
    public void testAggregatedFlex() {
        ProductionLine line1 = new ProductionLine.ProductionLineBuilder()
                .addShifted(7).addMultiCapExponentialConsuming(2, 15)
                .addShifted(7).build();
        ProductionLine line2 = new ProductionLine.ProductionLineBuilder()
                .addShifted(3).addMultiCapExponentialConsuming(1, 15)
                .addShifted(4).build();
        s = new SiteImpl(line1, line2);
        List<FlexTuple> flex1 = line1.getCurrentFlexbility();
        List<FlexTuple> flex2 = line2.getCurrentFlexbility();
        List<FlexTuple> siteflex = s.getFlexTuples();
        assertTrue(siteflex.containsAll(flex1));
        assertTrue(siteflex.containsAll(flex2));
        assertEquals(siteflex.size(), flex1.size() + flex2.size(), 0);
    }
}
