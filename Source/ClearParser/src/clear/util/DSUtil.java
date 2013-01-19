/**
 * Copyright (c) 2009, Regents of the University of Colorado All rights
 * reserved.
 * 
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
* Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the University of Colorado at
 * Boulder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package clear.util;

import clear.train.kernel.AbstractKernel;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.util.*;

/**
 * Data structure utilities.
 *
 * @author Jinho D. Choi <b>Last update:</b> 6/30/2010
 */
public class DSUtil {

    /**
     * @return List of integers converted from
     * <code>strArr</code>
     */
    static public ArrayList<Integer> toTIntArrayList(String[] strArr) {
        ArrayList<Integer> list = new ArrayList<>(strArr.length);

        for (String str : strArr) {
            list.add(Integer.parseInt(str));
        }

        return list;
    }

    /**
     * @return HashSet contains strings from
     * <code>strArr</code>
     */
    static public HashSet<String> toHashSet(String[] strArr) {
        HashSet<String> set = new HashSet<>(strArr.length);
        set.addAll(Arrays.asList(strArr));

        return set;
    }

    /**
     * @return HashMap whose keys are strings from
     * <code>list</code> and values are sequential integers starting at
     * <code>beginId</code>
     */
    static public HashMap<String, Integer> toHashMap(ArrayList<String> list, int beginId) {
        HashMap<String, Integer> map = new HashMap<>(list.size());

        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i + beginId);
        }

        return map;
    }

    static public int max(int[] arr) {
        int max = Integer.MIN_VALUE;
        for (int i : arr) {
            max = Math.max(max, i);
        }

        return max;
    }

    static public int max(ArrayList<Integer> arrlist) {
        int max = Integer.MIN_VALUE;
        for (int i : arrlist) {
            max = Math.max(max, i);
        }

        return max;
    }

    static public double max(double[] arr) {
        double max = Double.MIN_VALUE;
        for (double x : arr) {
            max = Math.max(max, x);
        }

        return max;
    }

    static public double min(double[] arr) {
        double max = Double.MAX_VALUE;
        for (double x : arr) {
            max = Math.max(max, x);
        }

        return max;
    }

    static public int[] toIntArray(String[] sArr, int beginIdx) {
        int[] iArr = new int[sArr.length - beginIdx];

        for (int i = beginIdx, j = 0; i < sArr.length; i++, j++) {
            iArr[j] = Integer.parseInt(sArr[i]);
        }

        return iArr;
    }

    static public JIntDoubleTuple[] toJIntDoubleArray(String[] sArr, int beginIdx) {
        JIntDoubleTuple[] iArr = new JIntDoubleTuple[sArr.length - beginIdx];
        String[] tmp;

        for (int i = beginIdx, j = 0; i < sArr.length; i++, j++) {
            tmp = sArr[i].split(AbstractKernel.FTR_DELIM);
            iArr[j] = new JIntDoubleTuple(Integer.parseInt(tmp[0]), Double.parseDouble(tmp[1]));
        }

        return iArr;
    }

    static public IntArrayList toIntArrayList(String[] sArr, int beginIdx) {
        IntArrayList iArr = new IntArrayList(sArr.length - beginIdx);

        for (int i = beginIdx; i < sArr.length; i++) {
            iArr.add(Integer.parseInt(sArr[i]));
        }

        return iArr;
    }

    static public int[] toIntArray(StringTokenizer tok) {
        int[] arr = new int[tok.countTokens()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(tok.nextToken());
        }

        return arr;
    }

    static public double[] toDoubleArray(StringTokenizer tok) {
        double[] arr = new double[tok.countTokens()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = Double.parseDouble(tok.nextToken());
        }

        return arr;
    }

    static public String[] toStringArray(StringTokenizer tok) {
        String[] arr = new String[tok.countTokens()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = tok.nextToken();
        }

        return arr;
    }

    static public int[] toArray(ArrayList<Integer> arrlist) {
        int[] arr = new int[arrlist.size()];

        for (int i = 0; i < arrlist.size(); i++) {
            arr[i] = arrlist.get(i);
        }

        return arr;
    }

    /*
     * static public double[] toArray(ArrayList<Double> arrlist) { double[] arr
     * = new double[arrlist.size()];
     *
     * for (int i=0; i<arrlist.size(); i++) arr[i] = arrlist.get(i);
     *
     * return arr;
	}
     */
    static public ArrayList<Integer> toArrayList(StringTokenizer tok) {
        ArrayList<Integer> arrlist = new ArrayList<>(tok.countTokens());

        while (tok.hasMoreTokens()) {
            arrlist.add(Integer.parseInt(tok.nextToken()));
        }

        return arrlist;
    }

    static public ArrayList<Integer> toArrayList(int[] arr) {
        ArrayList<Integer> arrlist = new ArrayList<>(arr.length);

        for (int value : arr) {
            arrlist.add(value);
        }
        return arrlist;
    }

    static public String toString(ArrayList<Integer> arr, String delim) {
        String str = "";
        for (int i : arr) {
            str += i + delim;
        }

        return str.trim();
    }

    static public String toString(IntArrayList arr, String delim) {
        StringBuilder build = new StringBuilder();
        int i, n = arr.size();

        for (i = 0; i < n; i++) {
            build.append(arr.get(i));
            build.append(delim);
        }

        return build.toString().trim();
    }

    static public String toString(int[] arr, String delim) {
        String str = "";
        for (int i : arr) {
            str += i + delim;
        }

        return str.trim();
    }

    // copy d1 = d2 
    static public void copy(double[] d1, double[] d2) {
        for (int i = 0; i < d1.length && i < d2.length; i++) {
            d1[i] = d2[i];
        }
    }
}