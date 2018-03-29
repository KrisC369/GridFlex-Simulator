package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

import org.jppf.node.protocol.AbstractTask;

import java.util.concurrent.Callable;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public abstract class GenericTask<T> extends AbstractTask<T> implements Callable<Object> {
    private WgmfGameParams params;

    protected GenericTask(WgmfGameParams params) {
        this.params = params;
    }

    protected GenericTask() {
    }

    protected final WgmfGameParams getParams() {
        return params;
    }

    protected final void setParams(WgmfGameParams params) {
        this.params = params;
    }
}
