package is2.util;

import java.io.*;
import java.util.StringTokenizer;

/**
 * @author Dr. Bernd Bohnet, 17.01.2010
 *
 * This class removes all information from a conll 2009 file except of columns 1
 * and 2 that contain the word id and the word form.
 */
public class ConvertTiger2CoNLL {

    public static void main(String[] args) throws IOException {


        OptionsSuper options = new OptionsSuper(args, null);

        if (options.trainfile != null) {
            System.err.println("included sentences " + clean(options.trainfile, options.outfile, options.start, options.count));
        } else {
            System.err.println("Please proivde the file name -train <file-name>");
        }
    }

    /**
     * @param trainfile
     * @throws IOException
     */
    private static int clean(String file, String outFile, int start, int numberOfSentences) throws IOException {

        System.err.println("writting to " + outFile);
        System.err.println("start " + start + " to " + (start + numberOfSentences));
        int state = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 32768);
        BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(outFile), "UTF-8"), 32768);
        String l;
        try {

            int id = 1, snt = 0, cnt = 0;

            while ((l = reader.readLine()) != null) {


                if (l.startsWith("#BOS")) {
                    state = 1; //BOS
                    id = 1;
                    snt++;
                    continue;
                }
                if (l.startsWith("#EOS") && state == 1) {
                    state = 2; //BOS
                    cnt++;

                    writer.newLine();
                }

                if (start > snt || (start + numberOfSentences) <= snt) {
                    state = 3;
                }

                if (l.startsWith("#5") || l.startsWith("#6") || l.startsWith("#7")) {
                    continue;
                }
                if ((start + numberOfSentences) <= snt) {
                    break;
                }

                if (state == 3) {
                    continue;
                }

                if (state == 1) {

                    l = l.replace("\t\t", "\t");
                    l = l.replace("\t\t", "\t");

                    StringTokenizer t = new StringTokenizer(l, "\t");
                    int count = 0;

                    writer.write("" + id + "\t");

                    while (t.hasMoreTokens()) {
                        if (count == 0) {
                            writer.write(t.nextToken() + "\t");
                        } else if (count == 1) {
                            writer.write(t.nextToken() + "\t_\t");
                        } else if (count == 2) {
                            writer.write(t.nextToken() + "\t_\t");
                        } else if (count == 3) {
                            writer.write(t.nextToken().replace(".", "|") + "\t_\t");
                        } else {
                            t.nextToken();
                        }
                        count++;
                    }
                    writer.write("_\t_\t_\t_\t_\t_\t_\t_\t_");
                    writer.newLine();
                }
                id++;
            }
            writer.flush();
            writer.close();
            reader.close();

            return cnt;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}