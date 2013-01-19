package mstparser;

/**
 * @author Andre Martins afm@cs.cmu.edu 03/19/2008 Class to check the feature
 * options for stacked parsing
 *
 */
public class StackedFeaturesOptions {

    public boolean usePredEdge = true;
    public boolean usePrevSibl = true;
    public boolean useNextSibl = true;
    public boolean useLabels = true;
    public boolean useGrandparents = true;
    public boolean useValency = false;
    public boolean useAllChildren = true;
    public boolean usePredHead = true;

    public void display() {
        DependencyParser.out.println("Stacked features options:");
        DependencyParser.out.println("  usePredEdge = " + usePredEdge);
        DependencyParser.out.println("  usePrevSibl = " + usePrevSibl);
        DependencyParser.out.println("  useNextSibl = " + useNextSibl);
        DependencyParser.out.println("  useLabels = " + useLabels);
        DependencyParser.out.println("  useGrandparents = " + useGrandparents);
        DependencyParser.out.println("  useValency = " + useValency);
        DependencyParser.out.println("  useAllChildren = " + useAllChildren);
        DependencyParser.out.println("  usePredHead = " + usePredHead);
    }
}