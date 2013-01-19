package is2.mtag;

import is2.parser.Parser;
import java.io.*;
import java.util.ArrayList;

/**
 * @author Dr. Bernd Bohnet, 20.01.2010
 *
 *
 */
public class Convert {

    public static void main(String[] args) throws IOException {

        Options options = new Options(args);

        split(options.trainfile);
    }

    /**
     * @param trainfile
     * @throws IOException
     */
    private static void split(String trainfile) throws IOException {

        String dir = "split";
        boolean success = (new File("split")).mkdir();
        if (success) {
            Parser.out.println("Directory: " + dir + " created");
        }


        ArrayList<String> corpus = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(trainfile), "UTF-8"), 32768);
        String l;
        int sentences = 0;
        try {
            while ((l = reader.readLine()) != null) {

                corpus.add(l);
                if (l.length() < 8) {
                    sentences++;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parser.out.println("Corpus has " + sentences + " sentences.");

        int partSize = sentences / 20;
        Parser.out.println("Prepare corpus for cross annotations with 20 parts with part size " + partSize + " number of lines " + corpus.size());



        for (int k = 0; k < 20; k++) {
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("split/p-" + k), "UTF-8"));
            BufferedWriter rest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("split/r-" + k), "UTF-8"));
            int skip = k * partSize;

            int countSentences = 0;
            int countSentencesWrote = 0;
            Parser.out.println("skip from " + skip + " to " + (skip + partSize - 1));
            for (String x : corpus) {
                if (countSentences >= skip && (countSentences < (skip + partSize) || k == 19)) {
                    rest.write(x);
                    rest.newLine();
                    if (x.length() < 8) {
                        countSentencesWrote++;
                    }
                } else {
                    br.write(x);
                    br.newLine();
                }

                if (x.length() < 8) {
                    countSentences++;
                }
            }
            Parser.out.println("wrote for this part " + countSentencesWrote);
            br.flush();
            br.close();
            rest.flush();
            rest.close();
        }
    }
}