package clear.util.tuple;

public class JMorphTuple {

    public String lemma, pos;

    public JMorphTuple(String lemma, String pos) {
        set(lemma, pos);
    }

    public void set(String lemma, String pos) {
        this.lemma = lemma;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return lemma + ":" + pos;
    }
}