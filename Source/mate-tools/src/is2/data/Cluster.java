package is2.data;

import is2.parser.Parser;
import is2.util.DB;
import java.io.*;

/**
 * @author Dr. Bernd Bohnet, 28.10.2010
 *
 *
 */
final public class Cluster {

    public static final String LPATH = "LP";
    public static final String SPATH = "SP";
    // [word][p]  p = [0:long-path | 1:short-path]  
    final private short[][] word2path;

    public Cluster() {
        word2path = new short[0][0];
    }

    /**
     * @param clusterFile
     * @param mf
     *
     */
    public Cluster(String clusterFile, IEncoderPlus mf, int ls) {

        final String REGEX = "\t";

        // register words
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            int cnt = 0;
            String line;
            while ((line = inputReader.readLine()) != null) {

                cnt++;
                try {
                    String[] split = line.split(REGEX);
                    mf.register(SPATH, split[0].length() < ls ? split[0] : split[0].substring(0, ls));
                    mf.register(LPATH, split[0]);
                    mf.register(PipeGen.WORD, split[1]);
                } catch (Exception e) {
                    Parser.out.println("Error in cluster line " + cnt + " error: " + e.getMessage());
                }
            }
            Parser.out.println("read number of clusters " + cnt);
            inputReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        word2path = new short[mf.getFeatureCounter().get(PipeGen.WORD)][2];


        // insert words
        try {
            String line;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(clusterFile), "UTF-8"), 32768);

            while ((line = inputReader.readLine()) != null) {

                String[] split = line.split(REGEX);
                int wd = mf.getValue(PipeGen.WORD, split[1]);
                word2path[wd][0] = (short) mf.getValue(SPATH, split[0].length() < ls ? split[0] : split[0].substring(0, ls));
                word2path[wd][1] = (short) mf.getValue(LPATH, split[0]);
            }
            inputReader.close();
            int fill = 0;
            for (int l = 0; l < word2path.length; l++) {
                if (word2path[l][0] != 0) {
                    fill++;
                }
            }
            /*
             * for(int l = 0; l<word2path.length; l++ ){ if (word2path[l][1]!=0)
             * fillL++; if (word2path[l][1]<-1) Parser.out.println("lower
             * "+word2path[l][1]); }
             */
            Parser.out.println("filled " + fill + " of " + word2path.length);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the cluster
     *
     * @param dos
     * @throws IOException
     */
    public Cluster(DataInputStream dis) throws IOException {

        word2path = new short[dis.readInt()][2];
        for (int i = 0; i < word2path.length; i++) {
            word2path[i][0] = dis.readShort();
            word2path[i][1] = dis.readShort();
        }
        DB.println("Read cluster with " + word2path.length + " words ");
    }

    /**
     * Write the cluster
     *
     * @param dos
     * @throws IOException
     */
    public void write(DataOutputStream dos) throws IOException {

        dos.writeInt(word2path.length);
        for (short[] i : word2path) {
            dos.writeShort(i[0]);
            dos.writeShort(i[1]);
        }
    }

    /**
     * @param form the id of a word form
     * @return the short path to the word form in the cluster
     *
     * final public int getSP(int form) { if (word2path.length<form) return -1;
     * return word2path[form][0]; }
     */
    /**
     * get the long path to a word form in the cluster
     *
     * @param form the id of a word form
     * @return the long path to the word
     */
    final public int getLP(int form) {
        if (word2path.length <= form || word2path[form].length <= 0) {
            return -1;
        }
        return word2path[form][0] == 0 ? -1 : word2path[form][0];
    }

    final public int getLP(int form, int l) {
        if (word2path.length < form) {
            return -1;
        }
        return word2path[form][l] == 0 ? -1 : word2path[form][l];
    }

    final public int size() {
        return word2path.length;
    }
}