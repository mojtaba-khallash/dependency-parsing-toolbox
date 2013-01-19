package is2.data;

final public class F2S extends IFV {

    private double[] parameters;

    public F2S() {
    }
    public double score;

    /**
     * @param parameters2
     */
    public F2S(double[] parameters2) {
        parameters = parameters2;
    }

    @Override
    public void add(int i) {
        if (i > 0) {
            score += parameters[i];
        }
    }

    public void setParameters(double[] p) {
        parameters = p;
    }

    @Override
    public void clear() {
        score = 0;
    }

    /*
     * (non-Javadoc) @see is2.IFV#getScore()
     */
    @Override
    public double getScore() {
        return score;
    }

    /*
     * (non-Javadoc) @see is2.IFV#clone()
     */
    @Override
    public IFV clone() {
        return new F2S(parameters);
    }
}