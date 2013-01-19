package is2.parser;

import is2.data.F2SF;
import is2.data.FV;
import is2.data.Instances;
import is2.data.Parse;
import is2.util.DB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

final public class ParametersFloat extends Parameters {

    public float[] parameters;
    public float[] total;

    public ParametersFloat(int size) {
        parameters = new float[size];
        total = new float[size];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = 0F;
            total[i] = 0F;
        }
    }

    /**
     * @param parameters2
     */
    public ParametersFloat(float[] p) {
        parameters = p;
    }

    @Override
    public void average(double avVal) {
        for (int j = 0; j < total.length; j++) {
            parameters[j] = total[j] / ((float) avVal);
        }
        total = null;
    }

    public ParametersFloat average2(double avVal) {
        float[] px = new float[this.parameters.length];
        for (int j = 0; j < total.length; j++) {
            px[j] = total[j] / ((float) avVal);
        }
        ParametersFloat pf = new ParametersFloat(px);
        return pf;
    }

    @Override
    public void update(FV act, FV pred, Instances isd, int instc, Parse d, double upd, double e) {

        e++;

        float lam_dist = getScore(act) - getScore(pred);

        float b = (float) e - lam_dist;

        FV dist = act.getDistVector(pred);

        dist.update(parameters, total, hildreth(dist, b), upd, false);
    }

    protected double hildreth(FV a, double b) {

        double A = a.dotProduct(a);
        if (A <= 0.0000000000000000001) {
            return 0.0;
        }
        return b / A;
    }

    public float getScore(FV fv) {
        if (fv == null) {
            return 0.0F;
        }
        return fv.getScore(parameters, false);

    }

    @Override
    final public void write(DataOutputStream dos) throws IOException {

        dos.writeInt(parameters.length);
        for (float d : parameters) {
            dos.writeFloat(d);
        }
    }

    @Override
    public void read(DataInputStream dis) throws IOException {

        parameters = new float[dis.readInt()];
        int notZero = 0;
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = dis.readFloat();
            if (parameters[i] != 0.0F) {
                notZero++;
            }
        }

        DB.println("read parameters " + parameters.length + " not zero " + notZero);
    }

    public int countNZ() {

        int notZero = 0;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] != 0.0F) {
                notZero++;
            }
        }
        return notZero;

        //	DB.println("read parameters "+parameters.length+" not zero "+notZero);
    }

    /*
     * (non-Javadoc) @see is2.sp09k99995.Parameters#getFV()
     */
    @Override
    public F2SF getFV() {
        return new F2SF(parameters);
    }

    /*
     * (non-Javadoc) @see is2.sp09k99999.Parameters#size()
     */
    @Override
    public int size() {
        return parameters.length;
    }
}