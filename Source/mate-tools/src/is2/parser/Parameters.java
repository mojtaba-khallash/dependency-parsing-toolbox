package is2.parser;

import is2.data.FV;
import is2.data.IFV;
import is2.data.Instances;
import is2.data.Parse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Bernd Bohnet, 31.08.2009
 *
 *
 */
public abstract class Parameters {

    public abstract void average(double avVal);

    public abstract void update(FV act, FV pred, Instances isd, int instc, Parse d, double upd, double e);

    public abstract void write(DataOutputStream dos) throws IOException;

    public abstract void read(DataInputStream dis) throws IOException;

    public abstract int size();

    /**
     * @return
     */
    public abstract IFV getFV();
}