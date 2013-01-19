package clear.dep.feat;

abstract public class AbstractFeat {

    public String[] feats;

    public String get(int index) {
        return feats[index];
    }

    abstract public void set(String feats);
}