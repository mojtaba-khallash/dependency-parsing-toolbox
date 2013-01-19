package is2.util;

/**
 * @author Dr. Bernd Bohnet, 01.06.2011
 *
 *
 */
final public class IntStack {

    public int[] stack;
    public int position = -1;

    public IntStack(int size) {
        //DB.println("int stack "+size);
        //new Exception().printStackTrace();
        stack = new int[size];
    }

    public int peek() {
        return position == -1 ? -1 : stack[position];
    }

    public void push(int i) {
        //	if (i ==2)new Exception().printStackTrace();
        stack[++position] = i;
    }

    public int pop() {
        return position == -1 ? -1 : stack[position--];
    }

    public int size() {
        return position + 1;
    }

    public boolean isEmpty() {
        return position == -1 ? true : false;
    }

    public int get(int p) {
        return stack[p];
    }

    public void clear() {
        position = -1;
    }

    /**
     * @param b
     */
    public void addAll(IntStack b) {

        position = b.position;
        if (position < 0) {
            return;
        }

        for (int k = 0; k <= position; k++) {
            stack[k] = b.stack[k];
        }
    }

    public boolean contains(int s) {;

        for (int k = 0; k <= position; k++) {
            if (stack[k] == s) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int k = position; k >= 0; k--) {
            s.append(k).append(":").append(this.stack[k]).append(" ");
        }
        return s.toString();
    }
}