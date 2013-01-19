package is2.tools;

import is2.data.Instances;
import java.io.DataOutputStream;

/**
 * @author Dr. Bernd Bohnet, 25.12.2010
 *
 *
 */
public interface IPipe {

    public abstract Instances createInstances(String file) throws Exception;

    public abstract void initValues();

    /**
     * Initialize the features types.
     */
    public abstract void initFeatures();

    public abstract void write(DataOutputStream dos);
}