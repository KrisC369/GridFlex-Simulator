package be.kuleuven.cs.gridflex.experimentation.runners;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Concrete implementation for the experiment task with a callback mechanism.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 */
public abstract class ExperimentAtomImpl implements ExperimentAtom {

    final List<ExperimentCallback> callbacks = Lists.newArrayList();

    @Override
    public Object call() {
        doRun();
        return null;
    }

    protected final void doRun() {
        execute();
        for (final ExperimentCallback c : callbacks) {
            c.callback(this);
        }
    }

    /**
     * Similar to the run-method from runnable. This method executes the task.
     */
    protected abstract void execute();

    @Override
    public void registerCallbackOnFinish(final ExperimentCallback c) {
        this.callbacks.add(c);
    }
}
