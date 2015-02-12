package be.kuleuven.cs.gametheory.experimentation.runners;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Concrete implementation for the experiment task with a callback mechanism.
 *
 * @author Kristof Coninx (kristof.coninx AT cs.kuleuven.be)
 *
 */
public abstract class ExperimentAtomImpl implements ExperimentAtom {

    List<ExperimentCallback> callbacks = Lists.newArrayList();

    @Override
    public void run() {
        execute();
        for (ExperimentCallback c : callbacks) {
            c.callback(this);
        }
    }

    /**
     * Similar to the run-method from runnable. This method executes the task.
     */
    protected abstract void execute();

    @Override
    public void registerCallbackOnFinish(ExperimentCallback c) {
        this.callbacks.add(c);
    }
}
