package is2.lemmatizer;

import is2.parser.Parser;
import java.util.ArrayList;

public class StringEdit {

    public static void main(String args[]) {

        String s = new StringBuffer(args[0]).reverse().toString();
        String t = new StringBuffer(args[1]).reverse().toString();

        int d[][] = LD(s, t);



        StringBuffer opersations = new StringBuffer();
        searchPath(s, t, d, opersations, false);
        Parser.out.println("resuylt " + " " + opersations);
    }

    //****************************
    // Get minimum of three values
    //****************************
    static private int Minimum(int a, int b, int c) {
        int mi;

        mi = a;
        if (b < mi) {
            mi = b;
        }
        if (c < mi) {
            mi = c;
        }

        return mi;
    }

    //*****************************
    // Compute Levenshtein distance
    //*****************************
    static public int[][] LD(String s, String t) {

        int n = s.length();
        int m = t.length(); // length of t
        //	char s_i; // ith character of s
        //	char t_j; // jth character of t
        int cost; // cost

        // Step 1
        int[][] d = new int[n + 1][m + 1];

        if (n == 0) {
            return d;
        }
        if (m == 0) {
            return d;
        }

        // Step 2
        for (int i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            d[0][j] = j;
        }


        // Step 3
        for (int i = 1; i <= n; i++) {

            int s_i = s.charAt(i - 1);

            // Step 4
            for (int j = 1; j <= m; j++) {

//				t_j = t.charAt (j - 1);

                // Step 5
                if (s_i == t.charAt(j - 1)) {
                    cost = 0;
                } else {
                    cost = 1;
                }


                // Step 6
                d[i][j] = Minimum(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + cost);
            }
        }

        // Step 7
        return d;
    }

    static String searchPath(String s, String t, int[][] d, StringBuffer operations, boolean debug) {

        StringBuilder result = new StringBuilder(s);

        int n = d.length;
        int m = d[0].length;

        int x = n - 1;
        int y = m - 1;
        boolean changed = false;
        while (true) {
            if (debug && changed) {
                Parser.out.println("result " + new StringBuffer(result).reverse());
            }

            if (d[x][y] == 0) {
                break;
            }
            if (y > 0 && x > 0 && d[x - 1][y - 1] < d[x][y]) {
                if (debug) {
                    Parser.out.println("min d[x-1][y-1] " + d[x - 1][y - 1] + " d[x][y] " + d[x][y] + " rep " + s.charAt(x - 1) + " with " + t.charAt(y - 1) + " at " + (x - 1));
                }

                operations.append('R').append(Character.toString((char) ((int) x - 1))).append(s.charAt(x - 1)).append(t.charAt(y - 1));
                if (debug) {
                    result.setCharAt(x - 1, t.charAt(y - 1));
                }
                y--;
                x--;
                changed = true;
                continue;
            }
            if (y > 0 && d[x][y - 1] < d[x][y]) {
                if (debug) {
                    Parser.out.println("min d[x][y-1] " + d[x][y - 1] + "  d[x][y] " + d[x][y] + " ins " + t.charAt(y - 1) + " at " + (x));
                }
                operations.append('I').append(Character.toString((char) ((int) x))).append(t.charAt(y - 1));
                if (debug) {
                    result.insert(x, t.charAt(y - 1));
                }
                y--;
                changed = true;
                continue;
            }
            if (x > 0 && d[x - 1][y] < d[x][y]) {
                if (debug) {
                    Parser.out.println("min d[x-1][y] " + d[x - 1][y] + " d[x][y] " + d[x][y] + " del " + s.charAt(x - 1) + " at " + (x - 1));
                }
                operations.append('D').append(Character.toString((char) ((int) x - 1))).append(s.charAt(x - 1));
                if (debug) {
                    result.deleteCharAt(x - 1);
                }
                x--;
                changed = true;
                continue;
            }
            changed = false;
            if (x > 0 && y > 0 && d[x - 1][y - 1] == d[x][y]) {
                x--;
                y--;
                continue;
            }
            if (x > 0 && d[x - 1][y] == d[x][y]) {
                x--;
                continue;
            }
            if (y > 0 && d[x][y - 1] == d[x][y]) {
                y--;
                continue;
            }

        }
        if (debug) {
            return result.reverse().toString();
        } else {
            return null;
        }
    }

    public static String change(String s, String operations) {

        StringBuffer result = new StringBuffer(s).reverse();

        int pc = 0;
        while (true) {
            if (operations.length() <= pc) {
                break;
            }
            char nextOperation = operations.charAt(pc);
            pc++;
            if (nextOperation == 'R') {
                //pc++;
                int xm1 = (char) operations.charAt(pc);
                pc++;
                char replace = operations.charAt(pc);
                pc++;
                char with = operations.charAt(pc);
                //operations.append('R').append((char)x-1).append(s.charAt(x-1)).append(t.charAt(y-1));
                //	Parser.out.println(""+result+" xm1 "+xm1+" op "+operations);


                if (result.length() <= xm1) {
                    return s;
                }

                if (result.charAt(xm1) == replace) {
                    result.setCharAt(xm1, with);
                }
                //if (debug) result.setCharAt(x-1, t.charAt(y-1));
                pc++;

            } else if (nextOperation == 'I') {
                //	if (debug) Parser.out.println("min d[x][y-1] "+d[x][y-1]+"  d[x][y] "+d[x][y]+" ins "+t.charAt(y-1)+" at "+(x)); 
                //operations.append('I').append((char)x).append(t.charAt(y-1));

                //if (debug)result.insert(x, t.charAt(y-1));
                //y--;
                //changed =true;
                //pc++;
                int x = operations.charAt(pc);
                pc++;
                char in = operations.charAt(pc);

                if (result.length() < x) {
                    return s;
                }

                result.insert(x, in);
                pc++;
            } else if (nextOperation == 'D') {
                //pc++;
                int xm1 = operations.charAt(pc);


                if (result.length() <= xm1) {
                    return s;
                }

                result.deleteCharAt(xm1);
                pc++;
                // delete with
                pc++;
                //		operations.append('D').append((char)x-1).append(s.charAt(x-1));
                //	if (debug)result.deleteCharAt(x-1);
            }

        }
        return result.reverse().toString();
        //else return null;
    }

    /**
     * @param opers
     * @param postion
     * @return
     */
    public static String get(ArrayList<String> opers, int position) {
        for (String s : opers) {
            int p = (int) s.charAt(1);
            if (p == position) {
                return s;
            }
        }
        return "0";
    }

    /**
     * @param form
     * @param string
     * @param c
     * @return
     */
    public static String changeSimple(String form, String operation, int c) {

        if (operation.equals("0")) {
            return form;
        }

        if (operation.charAt(0) == 'I') {
            StringBuilder f = new StringBuilder(form);
            if (f.length() <= c) {
                //	DB.println("fail insert ");
                return form;
            }
            f.insert(c + 1, operation.charAt(1));
            return f.toString();
        }
        if (operation.charAt(0) == 'R') {
            StringBuilder f = new StringBuilder(form);
            //		if (f.length()<=c) f.append(' ');
            if (f.length() <= c) {
                //	DB.println("fail replace ");
                return form;
            }
            f.setCharAt(c, operation.charAt(2));
            return f.toString();
        }

        if (operation.charAt(0) == 'D') {
            StringBuilder f = new StringBuilder(form);
            f.delete(c, c + 1);//.append(' ');
            return f.toString();
        }
        return form;
    }

    /**
     * @param string
     * @return
     */
    public static String simple(String o) {
        StringBuilder s = new StringBuilder(o);
        s.delete(1, 2);
        return s.toString();
    }
}