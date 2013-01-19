package ir.ac.iust.nlp.dependencyparser.utility.parsing;

/**
 *
 * @author Mojtaba Khallash
 */
public class MaltStackSettings extends MaltSettings {
    
    // 0
    public int Level;

    public int AugmentNParts = 5;

    public String AugmentedTrainFile;
    
    public MaltStackSettings() {}
    
    public MaltStackSettings(MaltStackSettings settings) {
        super(settings);
        
        this.Level = settings.Level;
        this.AugmentNParts = settings.AugmentNParts;
        this.AugmentedTrainFile = settings.AugmentedTrainFile;
    }
}