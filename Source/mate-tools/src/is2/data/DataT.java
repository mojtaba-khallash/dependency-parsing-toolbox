package is2.data;

final public class DataT {

    final public short typesLen;
    final public int len;
    //final public FV[][][] label;
    // a b lab op
    final public float[][][][] lab;

    public DataT(int length, short types) {
        typesLen = types;
        len = length;

        lab = new float[length][length][types][4];
    }
}