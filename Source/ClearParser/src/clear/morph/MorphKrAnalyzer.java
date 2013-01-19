package clear.morph;

import clear.util.tuple.JMorphTuple;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MorphKrAnalyzer {

    static public Pattern M_PS = Pattern.compile("SF|SP|SS|SE|SO|SY");
    static public Pattern M_CP = Pattern.compile("J.+");
    static public Pattern M_EM = Pattern.compile("E.+");
//	static public Pattern M_DS = Pattern.compile("XS.+");
    static public Pattern M_DS = Pattern.compile("SN|SV|SJ");

    static public boolean isPunctuation(String pos) {
        return M_PS.matcher(pos).find();
    }

    static public boolean isCaseParticle(String pos) {
        return M_CP.matcher(pos).find();
    }

    static public boolean isEndingMarker(String pos) {
        return M_EM.matcher(pos).find();
    }

    static public boolean isDerivationalSuffix(String pos) {
        return M_DS.matcher(pos).find();
    }

    static public boolean isX(ArrayList<JMorphTuple> list) {
        for (JMorphTuple tup : list) {
            if (!isCaseParticle(tup.pos) || !isEndingMarker(tup.pos)) {
                return false;
            }
        }

        return true;
    }
}