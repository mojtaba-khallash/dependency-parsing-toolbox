package is2.data;

/**
 * @author Dr. Bernd Bohnet, 17.01.2011
 *
 * Dynamic phrase structure tree.
 */
public class DPSTree {

    private int size = 0;
    public int[] heads;
    public int[] labels;

    public DPSTree() {
        this(30);
    }

    public DPSTree(int initialCapacity) {
        heads = new int[initialCapacity];
        labels = new int[initialCapacity];
    }

    /**
     * Increases the capacity of this <tt>Graph</tt> instance, if necessary, to
     * ensure that it can hold at least the number of nodes specified by the
     * minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity.
     */
    private void ensureCapacity(int minCapacity) {


        if (minCapacity > heads.length) {

            int newCapacity = minCapacity + 1;

            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            int oldIndex[] = heads;
            heads = new int[newCapacity];
            System.arraycopy(oldIndex, 0, heads, 0, oldIndex.length);

            oldIndex = labels;
            labels = new int[newCapacity];
            System.arraycopy(oldIndex, 0, labels, 0, oldIndex.length);

        }
    }

    final public int size() {
        return size;
    }

    final public boolean isEmpty() {
        return size == 0;
    }

    final public void clear() {
        size = 0;
    }

    final public void createTerminals(int terminals) {
        ensureCapacity(terminals + 1);
        size = terminals + 1;
    }

    final public int create(int phrase) {

        ensureCapacity(size + 1);
        labels[size] = phrase;
        size++;
        return size - 1;
    }

    public int create(int phrase, int nodeId) {

        if (nodeId < 0) {
            return this.create(phrase);
        }
// 		DB.println("create phrase "+nodeId+"  label "+phrase);
        ensureCapacity(nodeId + 1);
        labels[nodeId] = phrase;
        if (size < nodeId) {
            size = nodeId + 1;
        }
        return nodeId;
    }

    public void createEdge(int i, int j) {
        heads[i] = j;
//		DB.println("create edge "+i+"\t "+j);
    }

    @Override
    public DPSTree clone() {
        DPSTree ps = new DPSTree(this.size + 1);

        for (int k = 0; k < size; k++) {
            ps.heads[k] = heads[k];
            ps.labels[k] = labels[k];
        }
        ps.size = size;
        return ps;
    }
}