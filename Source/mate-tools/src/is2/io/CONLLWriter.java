package is2.io;

import is2.data.SentenceData09;
import java.io.BufferedWriter;
import java.io.IOException;

public abstract class CONLLWriter extends IOGenerals {
    public static final String DASH = "_";
    protected BufferedWriter writer;
    
    public abstract void write(SentenceData09 inst) throws IOException;
    
    public void finishWriting() throws IOException {
        writer.flush();
        writer.close();
    }
}
