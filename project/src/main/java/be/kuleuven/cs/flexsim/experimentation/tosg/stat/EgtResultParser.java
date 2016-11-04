package be.kuleuven.cs.flexsim.experimentation.tosg.stat;

import org.eclipse.jdt.annotation.Nullable;
import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Result parser that can produce fixed point of equations using Matlab and evolutionary game
 * theory principles.
 *
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class EgtResultParser implements AutoCloseable {
    private static final boolean HIDDEN = true;
    private static final boolean REUSE_PREVIOUS = true;
    @Nullable
    private final String ml_location;
    private final AtomicReference<MatlabProxy> proxy = new AtomicReference<>();

    public EgtResultParser(String pathToML) {
        this.ml_location = pathToML;
        Properties p = new Properties();
        MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
                .setUsePreviouslyControlledSession(REUSE_PREVIOUS).setMatlabLocation(ml_location)
                .setHidden(HIDDEN).build();
        try {
            proxy.set(new MatlabProxyFactory(options).getProxy());
        } catch (MatlabConnectionException e) {
            throw new IllegalStateException("Could not instantiate matlab proxy. Quitting!", e);
        }
    }

    public double[] findFixedPointForDynEquationParams(double[] dynParams) {
        try {
            getProxy().setVariable("PARAMS", dynParams);
            getProxy().eval("RES = solveN(PARAMS);");
            return (double[]) getProxy().getVariable("RES");
        } catch (MatlabInvocationException e) {
            throw new IllegalStateException("Something went wrong with the invoked operation.", e);
        }
    }

    private synchronized MatlabProxy getProxy() {
        return proxy.get();
    }

    @Override
    public void close() throws Exception {
        this.getProxy().exit();
        this.getProxy().disconnect();
    }
}
