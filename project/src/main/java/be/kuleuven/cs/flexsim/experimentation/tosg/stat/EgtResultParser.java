package be.kuleuven.cs.flexsim.experimentation.tosg.stat;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Result parser that can produce fixed point of equations using Matlab and evolutionary game
 * theory principles.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EgtResultParser implements AutoCloseable {
    private static final Object LOCK = new Object();
    private static final boolean HIDDEN = true;
    private static final boolean REUSE_PREVIOUS = true;
    private final AtomicReference<MatlabProxy> proxy = new AtomicReference<>();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(EgtResultParser.class);
    private static final String EGT_FUNCTION_PATH = "~/gitworkspace/EgtTools/src/";

    /**
     * Default constructor.
     *
     * @param pathToML Path to the matlab executable or null if on classpath.
     * @throws IllegalStateException If the matlab connection proxy cannot be made.
     */
    public EgtResultParser(String pathToML) {
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                .setUsePreviouslyControlledSession(REUSE_PREVIOUS).setMatlabLocation(pathToML)
                .setHidden(HIDDEN).build();
        try {
            synchronized (LOCK) {
                logger.debug(
                        "Attempting to connect matlab proxy to instance using options: {}",
                        options);
                proxy.set(new MatlabProxyFactory(options).getProxy());
                logger.debug("Matlab proxy connected");
            }
        } catch (MatlabConnectionException e) {
            throw new IllegalStateException("Could not instantiate matlab proxy. Quitting!", e);
        }
        try {
            getProxy().eval("addpath('" + EGT_FUNCTION_PATH + "');");
        } catch (MatlabInvocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find the fixed points of the population dynamics using ML code.
     *
     * @param dynParams The parameters for building the differential equations.
     * @return the array of fixed points.
     * @throws IllegalStateException    If an exception occurs when calling the ML code.
     * @throws IllegalArgumentException If dynParams is empty.
     */
    public double[] findFixedPointForDynEquationParams(double[] dynParams) {
        checkArgument(dynParams.length > 0, "Params should not be empty.");
        logger.debug("Evaluating function call for finding fixed points.");
        try {
            getProxy().setVariable("PARAMS", dynParams);
            getProxy().eval("RES = solveN(PARAMS);");
            double[] res = (double[]) getProxy().getVariable("RES");

            if (res.length == 0) {
                double ac1 = dynParams[0];
                double ac2 = dynParams[dynParams.length - 1];
                for (int i = 1; i < dynParams.length - 1; i++) {
                    if (i % 2 != 0) {
                        ac1 += dynParams[i];
                    } else {
                        ac2 += dynParams[i];
                    }
                }
                return new double[] { ac1 < ac2 ? 0 : 1 };
            }
            return res;
        } catch (MatlabInvocationException e) {
            throw new IllegalStateException("Something went wrong with the invoked operation.", e);
        }
    }

    private synchronized MatlabProxy getProxy() {
        return proxy.get();
    }

    @Override
    public void close() throws Exception {
        synchronized (LOCK) {
            logger.debug("Closing matlab instance and disconnecting proxy.");
            this.getProxy().disconnect();
            logger.debug("Matlab instance closed and proxy disconnected.");
        }
    }
}
