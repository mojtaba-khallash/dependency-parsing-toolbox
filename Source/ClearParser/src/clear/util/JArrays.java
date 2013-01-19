package clear.util;

import com.carrotsearch.hppc.IntArrayList;
import java.util.ArrayList;

public class JArrays {

    /**
     * @return delim.join(list) as in Python
     */
    static public String join(ArrayList<String> list, String delim) {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                buff.append(delim);
            }
            buff.append(list.get(i));
        }

        return buff.toString();
    }

    static public String join(IntArrayList list, String delim) {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                buff.append(delim);
            }
            buff.append(list.get(i));
        }

        return buff.toString();
    }

    /**
     * @return delim.join(list) as in Python
     */
    static public String join(String[] arr, String delim) {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                buff.append(delim);
            }
            buff.append(arr[i]);
        }

        return buff.toString();
    }

    static public String join(int[] arr, String delim) {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                buff.append(delim);
            }
            buff.append(arr[i]);
        }

        return buff.toString();
    }

    static public String join(float[] arr, String delim) {
        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                buff.append(delim);
            }
            buff.append(JString.getNormalizedForm(arr[i]));
        }

        return buff.toString();
    }
}