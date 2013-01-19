package ir.ac.iust.nlp.dependencyparser.phrasestructuregraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.trie.TrieSymbolTableHandler;
import org.maltparser.core.syntaxgraph.PhraseStructureGraph;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

/**
 * This example creates phrase structure graph of the sentence "Johan likes graphs" using 
 * the syntax graph package.
 * 
 * @author Johan Hall
 */
public class CreatePhraseStructureGraph {

    SymbolTable formTable;
    SymbolTable postagTable;
    SymbolTable catTable;
    SymbolTable edgeLabelTable;
    
    PhraseStructureGraph graph;

    int phraseStructureIndex;
    
    public CreatePhraseStructureGraph() throws MaltChainedException {
        // Creates a symbol table handler
        SymbolTableHandler symbolTables = new TrieSymbolTableHandler(
                TrieSymbolTableHandler.ADD_NEW_TO_TRIE);
        
        // Adds three symbol tables (FORM, POSTAG, CAT, EDGELABEL)
        formTable = symbolTables.addSymbolTable("FORM");
        postagTable = symbolTables.addSymbolTable("POSTAG");
        catTable = symbolTables.addSymbolTable("CAT");
        edgeLabelTable = symbolTables.addSymbolTable("EDGELABEL");
        
        graph = new PhraseStructureGraph(symbolTables);
        
        phraseStructureIndex = 0;
    }

    public void addPhraseStructureNode(String wordForm, String posTag, String category) throws MaltChainedException {
        phraseStructureIndex++;
        
        // Add terminal (token) nodes
        PhraseStructureNode node;

        node = graph.addTerminalNode(phraseStructureIndex);
        node.addLabel(formTable, wordForm);
        node.addLabel(postagTable, posTag);
        
        // Add nonterminal node
        node = graph.addNonTerminalNode(phraseStructureIndex);
        node.addLabel(catTable, category);
    }
    
    public void addPhraseStructureEdge(int node1, int node2, String label) throws MaltChainedException {
        // Add edges between nonterminal and terminals
        Edge e;
        
        PhraseStructureNode Node1, Node2;
        
        if(node1 == 0)
            Node1 = graph.getPhraseStructureRoot();
        else
            Node1 = graph.getNonTerminalNode(node1);
        
        Node2 = graph.getNonTerminalNode(node2);
        
        e = graph.addPhraseStructureEdge(Node1, Node2);
        e.addLabel(edgeLabelTable, label);
    }
        
    public PhraseStructureGraph getDependencyGraph() {
        return graph;
    }
}