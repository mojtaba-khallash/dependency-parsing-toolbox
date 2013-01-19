package edu.stanford.nlp.parser.ensemble.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    public static BufferedWriter openForWriting(String filename)
            throws IOException {
        OutputStream stream = new FileOutputStream(filename);

        if (filename.endsWith(".gz")) {
            stream = new GZIPOutputStream(stream);
        }

        return new BufferedWriter(new OutputStreamWriter(stream));
    }

    public static BufferedReader openForReading(String filename)
            throws IOException {
        InputStream stream = new FileInputStream(filename);

        if (filename.endsWith(".gz")) {
            stream = new GZIPInputStream(stream);
        }

        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Represents a basic CoNLL file (just words and parts of speech) along with
     * the corresponding parses by different parsers.
     *
     * @author dmcc
     */
    public static class BaseFilenameAndParses {

        private String baseFilename;
        private String[] parses;

        public BaseFilenameAndParses(String baseFilename, String[] parses) {
            this.baseFilename = baseFilename;
            this.parses = parses;
        }

        public String getBaseFilename() {
            return baseFilename;
        }

        public void setBaseFilename(String baseFilename) {
            this.baseFilename = baseFilename;
        }

        public String[] getParses() {
            return parses;
        }

        public void setParses(String[] parses) {
            this.parses = parses;
        }

        @Override
        public String toString() {
            return "BaseFilenameAndParses [baseFilename=" + baseFilename
                    + ", parses=" + Arrays.toString(parses) + "]";
        }
    }

    public static List<BaseFilenameAndParses> getParseFilesFromDir(String input) {
        List<BaseFilenameAndParses> results = new ArrayList<BaseFilenameAndParses>();

        File inputDirectory = new File(input);

        // first, find all the simple filenames (ones with words and tags only)
        String[] wordsAndTagsFilenames = inputDirectory.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".parse.gz");
            }
        });

        /*
         * given the simple filenames, we find out which filenames start with
         * them since parses using the words and tags filenames use them as a
         * prefix.
         */
        for (final String wordsAndTagsFilename : wordsAndTagsFilenames) {
            String[] allParses = inputDirectory.list(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(wordsAndTagsFilename)
                            && !name.equals(wordsAndTagsFilename);
                }
            });

            BaseFilenameAndParses singleEntry = new BaseFilenameAndParses(
                    input + "/" + wordsAndTagsFilename, allParses);
            results.add(singleEntry);
        }
        return results;
    }

    /**
     * Split the *.parse.gz files in one directory evenly among output
     * directories. We create the output directories (output/[division number])
     * and print commands which create softlinks from the input files to the
     * output files.
     *
     * @param input base directory for input (should contain *.parse.gz files)
     * @param output base directory for output (subdirectories for each division
     * will be made inside this)
     * @param divisions number of divisions
     * @throws IOException
     */
    public static void evenlySplitFilesInDirectory(String input, String output,
            int divisions) throws IOException {
        // make output directories
        File[] outputSubDirs = new File[divisions];
        for (int i = 0; i < divisions; i++) {
            File outputDir = new File(output, Integer.toString(i));
            outputDir.mkdirs();
            outputSubDirs[i] = outputDir;
        }

        int currentOutputSubDir = 0;
        for (BaseFilenameAndParses bfap : getParseFilesFromDir(input)) {
            File inputFile = new File(bfap.baseFilename);
            File outputFile = new File(outputSubDirs[currentOutputSubDir],
                    inputFile.getName());

            System.out.format("ln -s %s %s\n", inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath());

            currentOutputSubDir++;
            currentOutputSubDir %= divisions;
        }
    }

    public static void main(String[] args) throws IOException {
        String baseDir = "/home/mcclosky/data/gigaword-selected/apw_eng/100";
        String outputDir = "/home/mcclosky/data/gigaword-selected/apw_eng/100-split";
        evenlySplitFilesInDirectory(baseDir, outputDir, 6);
    }
}