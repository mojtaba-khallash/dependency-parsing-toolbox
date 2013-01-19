package optimizer;

import java.io.Writer;

/**
 * @author miguel
 *
 */
public class PathNotFoundException extends Exception {

    public PathNotFoundException() {
        this(null);
    }
    
    public PathNotFoundException(Writer writer) {
        String text = "The Training set path used in Phase 1 is not the same as you are trying to use in Phase 2";
        Optimizer.out.println(text);
        if (writer != null) {
            try {
                writer.write(text + "\n");
            }
            catch (Exception ex) {}
        }
    }
}