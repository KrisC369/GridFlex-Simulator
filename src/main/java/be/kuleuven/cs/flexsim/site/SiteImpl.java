package be.kuleuven.cs.flexsim.site;

import java.util.Collections;
import java.util.List;

/**
 * An implementation for the Site interface
 * 
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 *
 */
public class SiteImpl implements Site {

    @Override
    public boolean checkConfig() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setControlSchedules(List<ControlSchedule> schedule) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<FlexTuple> getFlexTuples() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

}
