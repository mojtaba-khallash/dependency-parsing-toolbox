package ir.ac.iust.nlp.dependencyparser.inputoutput;

import java.util.LinkedList;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.io.dataformat.DataFormatSpecification;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.trie.TrieSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.reader.SyntaxGraphReader;
import org.maltparser.core.syntaxgraph.reader.TabReader;
import org.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter;
import org.maltparser.core.syntaxgraph.writer.TabWriter;

/**
 * This example reads dependency graphs formatted according to the CoNLL format
 * and writes the graphs to another file.
 *
 * @author Johan Hall
 */
public class ReadWriteCoNLL {

    private DependencyGraph inputGraph;
    private SyntaxGraphReader tabReader;
    private SyntaxGraphWriter tabWriter;
    private boolean moreInput = false;
    private SymbolTableHandler symbolTables;

    public ReadWriteCoNLL(String dataFormatFileName) throws MaltChainedException {
        // Creates a symbol table handler
        symbolTables = new TrieSymbolTableHandler(
                TrieSymbolTableHandler.ADD_NEW_TO_TRIE);

        // Initialize data format instance of the CoNLL data format from conllx.xml (conllx.xml located in same directory)
        DataFormatSpecification dataFormat = new DataFormatSpecification();
        dataFormat.parseDataFormatXMLfile(dataFormatFileName);
        DataFormatInstance dataFormatInstance = dataFormat.createDataFormatInstance(symbolTables, "none");

        // Creates a dependency graph
        inputGraph = new DependencyGraph(symbolTables);

        // Creates a tabular reader with the CoNLL data format
        tabReader = new TabReader();
        tabReader.setDataFormatInstance(dataFormatInstance);

        // Creates a tabular writer with the CoNLL data format
        tabWriter = new TabWriter();
        tabWriter.setDataFormatInstance(dataFormatInstance);
    }

    public void run(String inFile, String outFile, String charSet) throws MaltChainedException {

        // Opens the input and output file with a character encoding set
        tabReader.open(inFile, charSet);
        tabWriter.open(outFile, charSet);

        moreInput = true;

        // Reads Sentences until moreInput is false
        while (moreInput) {
            // Read One "Sentence" and Create "Dependency Graph" correspond to it
            // and set it to inputGraph
            moreInput = tabReader.readSentence(inputGraph);
            if (inputGraph.hasTokens()) {
                tabWriter.writeSentence(inputGraph);
            }
        }

        // Closes the reader and writer
        tabReader.close();
        tabWriter.close();
    }

    // Opens the input file with a character encoding set [My Method]
    public void initRead(String inFile, String charSet) throws MaltChainedException {
        tabReader.open(inFile, charSet);
        moreInput = true;
    }

    // Read All Sentences and return All Dependency Graprhs [My Method]
    public List<DependencyGraph> readAll() throws MaltChainedException {
        List<DependencyGraph> all = new LinkedList<>();

        // Reads Sentences until moreInput is false
        while (moreInput) {
            // Read One "Sentence" and Create "Dependency Graph" correspond to it
            // and set it to inputGraph
            moreInput = tabReader.readSentence(inputGraph);
            if (inputGraph.hasTokens()) {
                all.add(inputGraph);
            }
            inputGraph = new DependencyGraph(symbolTables);
        }

        terminateRead();
        return all;
    }

    // Read Next Sentences and return Corresponding Dependency Graprh [My Method]
    public DependencyGraph readNext() throws MaltChainedException {
        // Reads Sentences until moreInput is false
        if (moreInput) {
            // Read One "Sentence" and Create "Dependency Graph" correspond to it
            moreInput = tabReader.readSentence(inputGraph);
            if (inputGraph.hasTokens()) {
                return inputGraph;
            }
        } else {
            terminateRead();
        }

        return null;
    }

    // Close reader [My Method]
    public void terminateRead() throws MaltChainedException {
        // Closes the reader
        tabReader.close();
    }

    // Opens the output file with a character encoding set [My Method]
    public void initWrite(String outFile, String charSet) throws MaltChainedException {
        tabWriter.open(outFile, charSet);
    }

    // Write All Dependency Graprhs in output file [My Method]
    public void writeAll(List<DependencyGraph> dgs) throws MaltChainedException {
        for (int i = 0; i < dgs.size(); i++) {
            inputGraph = dgs.get(i);
            if (inputGraph.hasTokens()) {
                tabWriter.writeSentence(inputGraph);
            }
        }

        terminateWrite();
    }

    // Write Next Dependency Graprh to output file [My Method]
    public void writeNext(DependencyGraph dg) throws MaltChainedException {
        if (dg.hasTokens()) {
            tabWriter.writeSentence(dg);
        }
    }

    // Close writer [My Method]
    public void terminateWrite() throws MaltChainedException {
        // Closes the writer
        tabWriter.close();
    }
}