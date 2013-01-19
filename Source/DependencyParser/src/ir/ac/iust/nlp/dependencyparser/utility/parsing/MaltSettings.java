package ir.ac.iust.nlp.dependencyparser.utility.parsing;

import java.io.File;

/**
 *
 * @author Mojtaba Khallash
 */
public class MaltSettings extends Settings {
    
    public String OptionsFile;
    public String GuidesFile;
    
    public String WorkingDirectory;
    
    public MaltSettings() {}
    
    public MaltSettings(MaltSettings settings) {
        super(settings);
        
        this.OptionsFile = settings.OptionsFile;
        this.GuidesFile = settings.GuidesFile;
        this.WorkingDirectory = settings.WorkingDirectory;
    }
    
    @Override
    public String[] getTrainParameters() {
        return null;
    }
    public String getTrainParameters1() {
        StringBuilder pars = new StringBuilder();

        // flowchart: Flow chart
        //  -learn	[Learn a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("learn");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(Model);

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(WorkingDirectory);

        // infile: Path to input file
        pars.append(" -i ");
        pars.append("tmp").append(File.separator).append(Input);

        pars.append(" -f ");
        pars.append("tmp").append(File.separator).append(OptionsFile);

        pars.append(" -F ");
        pars.append("tmp").append(File.separator).append(GuidesFile);

        return pars.toString();
    }
    
    @Override
    public String[] getTestParameters() {
        return null;
    }
    public String getTestParameters1() {
        StringBuilder pars = new StringBuilder();

        // flowchart: Flow chart
        //  -parse	[Parse with a Single MaltParser configuration]
        pars.append("-m ");
        pars.append("parse");

        // name: Configuration name
        pars.append(" -c ");
        pars.append(Model);

        // workingdir: Working directory
        pars.append(" -w ");
        pars.append(WorkingDirectory);

        // infile: Path to input file
        pars.append(" -i ");
        pars.append("tmp").append(File.separator).append(Input);

        // outfile: Path to output file
        pars.append(" -o ");
        pars.append("tmp").append(File.separator).append(Output);

        return pars.toString();
    }
}