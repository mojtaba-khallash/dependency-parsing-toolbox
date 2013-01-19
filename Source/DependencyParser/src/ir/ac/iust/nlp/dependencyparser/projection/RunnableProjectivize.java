package ir.ac.iust.nlp.dependencyparser.projection;

import edu.stanford.nlp.parser.ensemble.utils.DeprojectivizeCorpus;
import edu.stanford.nlp.parser.ensemble.utils.ProjectivizeCorpus;
import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.utility.enumeration.TransformType;
import java.io.IOException;
import org.maltparser.core.exception.MaltChainedException;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableProjectivize implements Runnable {

    String workingDirectory;
    String input;
    String Output;
    String model;
    String markingStrategy;
    String coveredRoot;
    String liftingOrder;
    
    TransformType type;

    BasePanel target;

    public RunnableProjectivize(BasePanel target, TransformType type, 
            String workingDirectory, String input,
            String output, String model, String markingStrategy,
            String coveredRoot, String liftingOrder) {
        
        this.target = target;
        
        this.type = type;
        
        this.workingDirectory = workingDirectory;
        this.input = input;
        this.Output = output;
        this.model = model;
        this.markingStrategy = markingStrategy;
        this.coveredRoot = coveredRoot;
        this.liftingOrder = liftingOrder;
    }

    @Override
    public void run() {
        try {
            switch(type) {
                case Projectivize:
                    ProjectivizeCorpus.Projectivize(workingDirectory, input, Output, 
                            model, markingStrategy, coveredRoot, liftingOrder);
                    break;
                case Deprojectivize:
                    DeprojectivizeCorpus.Deprojectivize(workingDirectory, input, Output, model);
                    break;
            }
        } catch (MaltChainedException | IOException e) {
        }
        finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
}