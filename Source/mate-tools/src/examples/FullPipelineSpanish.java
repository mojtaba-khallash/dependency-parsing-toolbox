package examples;

import is2.data.SentenceData09;
import is2.io.CONLLWriter09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;
import is2.tools.Tool;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Bernd Bohnet, 13.09.2010
 *
 * Illustrates the application the full pipeline: lemmatizer, morphologic,
 * tagger, and parser
 */
public class FullPipelineSpanish {

    //	shows how to parse a sentences and call the tools
    public static void main(String[] args) throws IOException {

        // Create a data container for a sentence
        SentenceData09 i = new SentenceData09();

        if (args.length == 1) { // input might be a sentence: "This is another test ." 
            StringTokenizer st = new StringTokenizer(args[0]);
            ArrayList<String> forms = new ArrayList<>();

            forms.add("<root>");
            while (st.hasMoreTokens()) {
                forms.add(st.nextToken());
            }

            i.init(forms.toArray(new String[0]));

        } else {
            // provide a default sentence: Haus has a mutated vowel
            i.init(new String[]{"<root>", "También", "estuve", "emocionado", "pero", "no", "pude", "imaginar", "mi", "vida", "sin", "la",
                        "gente", "tan", "intima", "a", "mí", "."});

        }

        // lemmatizing

        Parser.out.println("\nReading the model of the lemmatizer");
        Tool lemmatizer = new Lemmatizer("models/lemma-spa.model");  // create a lemmatizer

        Parser.out.println("Applying the lemmatizer");
        lemmatizer.apply(i);

        Parser.out.print(i.toString());
        Parser.out.print("Lemmata: ");
        for (String l : i.plemmas) {
            Parser.out.print(l + " ");
        }
        Parser.out.println();

        // morphologic tagging

        Parser.out.println("\nReading the model of the morphologic tagger");
        is2.mtag.Tagger morphTagger = new is2.mtag.Tagger("models/mtag-spa.model");

        Parser.out.println("\nApplying the morpholoigc tagger");
        morphTagger.apply(i);

        Parser.out.print(i.toString());
        Parser.out.print("Morph: ");
        for (String f : i.pfeats) {
            Parser.out.print(f + " ");
        }
        Parser.out.println();

        // part-of-speech tagging

        Parser.out.println("\nReading the model of the part-of-speech tagger");
        Tool tagger = new Tagger("models/tag-spa.model");

        Parser.out.println("\nApplying the part-of-speech tagger");
        tagger.apply(i);

        Parser.out.print(i.toString());
        Parser.out.print("Part-of-Speech tags: ");
        for (String p : i.ppos) {
            Parser.out.print(p + " ");
        }
        Parser.out.println();

        // parsing

        Parser.out.println("\nReading the model of the dependency parser");
        Tool parser = new Parser("models/prs-spa.model");

        Parser.out.println("\nApplying the parser");
        parser.apply(i);

        Parser.out.println(i.toString());

        // write the result to a file

        CONLLWriter09 writer = new is2.io.CONLLWriter09("example-out.txt");

        writer.write(i, CONLLWriter09.NO_ROOT);
        writer.finishWriting();
    }
}