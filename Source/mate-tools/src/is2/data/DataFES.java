package is2.data;

final public class DataFES {

    final public short typesLen;
    final public int len;
    // first order features
    final public float[][] pl;
    // remove !!!!
//		final public float[][] highestLab;
    //final public FV[][][] label;
    final public float[][][] lab;
    public FV fv;
    final public float[][][][] sib;
    final public float[][][][] gra;

    public DataFES(int length, short types) {
        typesLen = types;
        len = length;

        pl = new float[length][length];
        lab = new float[length][length][types];

        sib = new float[length][length][length][];
        gra = new float[length][length][length][];
    }
}