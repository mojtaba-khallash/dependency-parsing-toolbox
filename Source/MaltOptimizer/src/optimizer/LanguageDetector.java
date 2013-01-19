package optimizer;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.util.ArrayList;

/**
 *
 * @author Miguel Ballesteros
 *
 */
public class LanguageDetector {

    String frase;

    public LanguageDetector(String frase) {
        this.frase = frase;
    }

    public void init(String profileDirectory) throws LangDetectException {
        DetectorFactory.loadProfile(profileDirectory);
    }

    public String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }

    public ArrayList detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

    public String getLanguage() {
        try {
            init("profiles");
            return detect(frase);
        } catch (LangDetectException e) {
            e.printStackTrace();
        }
        return null;
    }
}