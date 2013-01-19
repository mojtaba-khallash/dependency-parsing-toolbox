package optimizer;

/**
 *
 * @author Mojtaba Khallash
 */
public class GuidesGenerator {
    public static String generateGuides(String feature) {
        int ind = feature.lastIndexOf(".xml");
        if (ind != -1) {
            feature = feature.substring(0, ind);
        }
        
        switch (feature) {
            case "NivreEager":
                return generateNivreEager();
            case "NivreStandard":
                return generateNivreStandard();
            case "StackProjective":
                return generateStackProjective();
            case "StackSwap":
                return generateStackSwap();
            case "CovingtonProjective":
                return generateCovingtonProjective();
            case "CovingtonNonProjective":
                return generateCovingtonNonProjective();
        }
        
        return "";
    }
    
    private static String generateNivreEager() {
        StringBuilder guide = new StringBuilder();

        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("<featuremodels>\n");
	guide.append("\t<featuremodel name=\"nivreeager\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[0])</feature>\n");
	guide.append("\t\t<feature>InputColumn(POSTAG, Input[1])</feature>\n");
	guide.append("\t\t<feature>InputColumn(POSTAG, Input[2])</feature>\n");
	guide.append("\t\t<feature>InputColumn(POSTAG, Input[3])</feature>\n");
	guide.append("\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>\n");
	guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]))</feature>\n");
	guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]))</feature>\n");
	guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]), InputColumn(POSTAG, Input[1]))</feature>\n");
	guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Input[0]), InputColumn(POSTAG, Input[1]), InputColumn(POSTAG, Input[2]))</feature>\n");
	guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Input[1]), InputColumn(POSTAG, Input[2]), InputColumn(POSTAG, Input[3]))</feature>\n");
	guide.append("\t\t<feature>OutputColumn(DEPREL, Stack[0])</feature>\n");
	guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[0]))</feature>\n");
	guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>\n");
	guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Input[0]))</feature>\n");
	guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>\n");
	guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, Stack[0]))</feature>\n");
	guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, ldep(Input[0])))</feature>\n");
	guide.append("\t\t<feature>InputColumn(FORM, Stack[0])</feature>\n");
	guide.append("\t\t<feature>InputColumn(FORM, Input[0])</feature>\n");
	guide.append("\t\t<feature>InputColumn(FORM, Input[1])</feature>\n");
	guide.append("\t\t<feature>InputColumn(FORM, head(Stack[0]))</feature>\n");
	guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
    
    private static String generateNivreStandard() {
        StringBuilder guide = new StringBuilder();
 
        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("\t<featuremodels>\n");
        guide.append("\t\t<featuremodel name=\"nivrestandard\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[2])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[3])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Input[0]), InputColumn(POSTAG, Input[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Input[0]), InputColumn(POSTAG, Input[1]), InputColumn(POSTAG, Input[2]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Input[1]), InputColumn(POSTAG, Input[2]), InputColumn(POSTAG, Input[3]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Input[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Input[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Input[0]), OutputColumn(DEPREL, ldep(Input[0])), OutputColumn(DEPREL, rdep(Input[0])))</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Input[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Input[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, head(Stack[0]))</feature>\n");
        guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
    
    private static String generateStackProjective() {
        StringBuilder guide = new StringBuilder();
        
        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("\t<featuremodels>\n");
        guide.append("\t\t<featuremodel name=\"stackprojective\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[2])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[2])</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[1]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[1])), OutputColumn(DEPREL, rdep(Stack[1])))</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Stack[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Lookahead[0])</feature>\n");
        guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
    
    private static String generateStackSwap() {
        StringBuilder guide = new StringBuilder();
    
        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("\t<featuremodels>\n");
        guide.append("\t\t<featuremodel name=\"stackswap\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Stack[2])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Input[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Lookahead[2])</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[2]), InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Lookahead[0]), InputColumn(POSTAG, Lookahead[1]), InputColumn(POSTAG, Lookahead[2]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Stack[1]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Stack[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[0]), OutputColumn(DEPREL, ldep(Stack[0])), OutputColumn(DEPREL, rdep(Stack[0])))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Stack[1]), OutputColumn(DEPREL, ldep(Stack[1])), OutputColumn(DEPREL, rdep(Stack[1])))</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Stack[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Stack[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Lookahead[0])</feature>\n");
        guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
    
    private static String generateCovingtonProjective() {
        StringBuilder guide = new StringBuilder();
        
        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("\t<featuremodels>\n");
        guide.append("\t\t<featuremodel name=\"covnonproj\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Left[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[2])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[3])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Left[1])</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[1]), InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]), InputColumn(POSTAG, Right[3]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, Left[0])</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Left[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Left[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Right[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, ldep(Left[0])), OutputColumn(DEPREL, rdep(Left[0])))</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, Left[0]))</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, ldep(Right[0])))</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Left[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Right[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Right[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, head(Left[0]))</feature>\n");
        guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
    
    private static String generateCovingtonNonProjective() {
        StringBuilder guide = new StringBuilder();
        
        guide.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        guide.append("\t<featuremodels>\n");
        guide.append("\t\t<featuremodel name=\"covnonproj\">\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Left[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[2])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Right[3])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, Left[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, LeftContext[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(POSTAG, RightContext[0])</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[1]), InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Right[0]), InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]))</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Right[1]), InputColumn(POSTAG, Right[2]), InputColumn(POSTAG, Right[3]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, Left[0])</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Left[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, rdep(Left[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, ldep(Right[0]))</feature>\n");
        guide.append("\t\t<feature>OutputColumn(DEPREL, Right[0])</feature>\n");
        guide.append("\t\t<feature>Merge3(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, ldep(Left[0])), OutputColumn(DEPREL, rdep(Left[0])))</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Left[0]), OutputColumn(DEPREL, Left[0]))</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, ldep(Right[0])))</feature>\n");
        guide.append("\t\t<feature>Merge(InputColumn(POSTAG, Right[0]), OutputColumn(DEPREL, Right[0]))</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Left[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Right[0])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, Right[1])</feature>\n");
        guide.append("\t\t<feature>InputColumn(FORM, head(Left[0]))</feature>\n");
        guide.append("\t</featuremodel>\n");
        guide.append("</featuremodels>\n");
        
        return guide.toString();
    }
}