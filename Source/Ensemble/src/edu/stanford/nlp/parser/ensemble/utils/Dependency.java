package edu.stanford.nlp.parser.ensemble.utils;

public interface Dependency {

    public int head();

    public int mod();

    public String label();

    public double score();

    public boolean sameDependency(Dependency other);
}
