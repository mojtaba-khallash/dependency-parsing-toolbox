package ir.ac.iust.nlp.dependencyparser.optomization;

import ir.ac.iust.nlp.dependencyparser.BasePanel;
import java.io.File;

/**
 *
 * @author Mojtaba Khallash
 */
public class RunnableOptimizer implements Runnable {

    BasePanel target;
    
    int phase;
    String input;
    String crossValidation = "dev";
    
    public RunnableOptimizer(BasePanel target, int phaseNumber, String input,
            boolean useCrossValidation) {
        this.target = target;
        this.phase = phaseNumber;
        this.input = input;
        this.crossValidation = (useCrossValidation == true ? "cv" : "dev");
    }
    
    @Override
    public void run() {
        try {
            optimizer.Optimizer.main(new String[]{
                        "-p", String.valueOf(phase),
                        "-m", "lib" + File.separator + "maltParser.jar",
                        "-c", input,
                        "-v", crossValidation});
        }
        finally {
            if (target != null) {
                target.threadFinished();
            }
        }
    }
}