package is2.data;

final public class DataF {

    final public short typesLen;
    final public int len;
    // first order features
    final public float[][] pl;
    // remove !!!!
//		final public float[][] highestLab;
    //final public FV[][][] label;
    final public float[][][][] lab;
    public FV fv;
    final public float[][][][][] sib;
    final public float[][][][][] gra;

    public DataF(int length, short types) {
        typesLen = types;
        len = length;

        pl = new float[length][length];
        lab = new float[length][length][types][2];
        //		highestLab = new float[length][length];

        sib = new float[length][length][length][2][];
        gra = new float[length][length][length][2][];
    }
}