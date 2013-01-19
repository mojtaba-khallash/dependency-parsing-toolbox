package edu.stanford.nlp.parser.ensemble;

import java.io.File;
import org.maltparser.core.helper.SystemLogger;

public class BaseModelRunnableJob {

    protected Ensemble ensemble;
    protected int baseModelIndex;
    protected String baseModel;
    protected String featureModel;
    protected File workingDirectory;
    protected boolean leftToRight;
    protected boolean pseudo_projective;

    protected BaseModelRunnableJob(Ensemble ensemble, int index) {
        this.ensemble = ensemble;
        this.baseModelIndex = index;
        this.baseModel = ensemble.baseModels[baseModelIndex];
        this.featureModel = ensemble.featureModels[baseModelIndex];
        this.leftToRight = (baseModel.toLowerCase().lastIndexOf("ltr") != -1);
        this.pseudo_projective = baseModel.toLowerCase().endsWith("+pp");
    }

    void createWorkingDirectory() {
        String dn = ensemble.workingDirectory + File.separator + baseModel;
        workingDirectory = new File(dn);
        if (workingDirectory.exists()) {
            throw new RuntimeException("ERROR: Working directory already exists!");
        }
        if (!workingDirectory.mkdir()) {
            throw new RuntimeException("ERROR: Cannot create working directory!");
        }
        workingDirectory.deleteOnExit();
        SystemLogger.logger().debug("Working directory for job #" + baseModelIndex + " set to " + workingDirectory.getAbsolutePath() + "\n");
    }
}