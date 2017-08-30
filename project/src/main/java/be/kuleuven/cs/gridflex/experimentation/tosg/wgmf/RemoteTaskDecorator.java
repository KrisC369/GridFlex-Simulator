package be.kuleuven.cs.gridflex.experimentation.tosg.wgmf;

/**
 * @author Kristof Coninx <kristof.coninx AT cs.kuleuven.be>
 */
public class RemoteTaskDecorator<T> extends GenericTask<T> {

    private static final long serialVersionUID = -1599132847916137208L;
    private GenericTask<T> target;
    private String remoteParamId;

    public RemoteTaskDecorator(String remoteParamId, GenericTask<T> target) {
        super();
        this.remoteParamId = remoteParamId;
        this.target = target;
    }

    @Override
    public void run() {
        if (getDataProvider() != null) {
            setParams(getDataProvider().getParameter(remoteParamId));
        }
        target.run();
    }

    @Override
    public Object call() throws Exception {
        return target.call();
    }

    public GenericTask<T> getTarget() {
        return target;
    }
}
