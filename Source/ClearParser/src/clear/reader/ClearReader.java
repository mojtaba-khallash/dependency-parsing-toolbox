package clear.reader;

public class ClearReader {

    protected ReaderXML reader_xml;
    protected String[] a_sentences;

    public ClearReader(ReaderXML readerXML) {
        reader_xml = readerXML;
    }

    public ClearReader(ReaderXML readerXML, String inputFile) {
        reader_xml = readerXML;
        open(inputFile);
    }

    public void open(String inputFile) {
        /*
         * if (reader_xml.equals(ReaderXML.FORMAT_RAW)) openRaw(inputFile); else
         * if (reader_xml.equals(ReaderXML.FORMAT_LINE)) openLine(inputFile);
         * else if (reader_xml.equals(ReaderXML.FORMAT_COLUMN))
			openColumn(inputFile);
         */
    }

    protected void openRaw(String inputFile) throws Exception {
        //	SentenceDetectorME detector = new SentenceDetectorME(new SentenceModel(new FileInputStream("en-sent.bin")));
    }

    protected void openLine(String inputFile) {
    }

    protected void openColumn(String inputFile) {
    }
}