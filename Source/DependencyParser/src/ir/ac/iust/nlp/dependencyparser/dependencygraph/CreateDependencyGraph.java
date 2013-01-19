package ir.ac.iust.nlp.dependencyparser.dependencygraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.trie.TrieSymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyGraph;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

/**
 * This example creates dependency graph of the sentence "Johan likes graphs" using 
 * the syntax graph package.
 * 
 * Example:
 * 	node = graph.addDependencyNode(1);
 *	node.addLabel(formTable, "Johan");
 *	node.addLabel(postagTable, "N");
 *		
 *	node = graph.addDependencyNode(2);
 *	node.addLabel(formTable, "likes");
 *	node.addLabel(postagTable, "V");
 *		
 *	node = graph.addDependencyNode(3);
 *	node.addLabel(formTable, "graphs");
 *	node.addLabel(postagTable, "N");
 * 
 * 
 * 	edge = graph.addDependencyEdge(0, 2); // The root node has index 0
 *	edge.addLabel(deprelTable, "PRED");
 *		
 *	edge = graph.addDependencyEdge(2, 1);
 *	edge.addLabel(deprelTable, "SUB");
 *		
 *	edge = graph.addDependencyEdge(2, 3);
 *	edge.addLabel(deprelTable, "OBJ");
 *
 * @author Johan Hall
 */
public class CreateDependencyGraph {

    SymbolTable formTable;
    SymbolTable postagTable;
    SymbolTable deprelTable;

    DependencyGraph graph;

    int dependencyIndex;

    public CreateDependencyGraph() throws MaltChainedException {
        // Creates a symbol table handler
        SymbolTableHandler symbolTables = new TrieSymbolTableHandler(
                TrieSymbolTableHandler.ADD_NEW_TO_TRIE);

        // Adds three symbol tables (FORM, POSTAG and DEPREL)
        formTable = symbolTables.addSymbolTable("FORM");
        postagTable = symbolTables.addSymbolTable("POSTAG");
        deprelTable = symbolTables.addSymbolTable("DEPREL");

        // Creates a dependency graph
        graph = new DependencyGraph(symbolTables);

        // The root node has index 0
        dependencyIndex = 0;
    }

    public void addDependencyNode(String wordForm, String posTag) throws MaltChainedException {
        dependencyIndex++;

        // Adds dependency (token) nodes
        DependencyNode node;

        node = graph.addDependencyNode(dependencyIndex);
        node.addLabel(formTable, wordForm);
        node.addLabel(postagTable, posTag);
    }

    public void addDependencyEdge(int head, int modifier, String label) throws MaltChainedException {
        // Adds dependency relations (edges)
        Edge e;

        e = graph.addDependencyEdge(head, modifier); 
        e.addLabel(deprelTable, label);
    }

    public DependencyGraph getDependencyGraph() {
        return graph;
    }
}