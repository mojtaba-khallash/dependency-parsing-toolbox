package examples;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Options;
import is2.parser.Parser;
import is2.tag.Tagger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Bernd Bohnet, 13.09.2010
 *
 * Illustrates the application of some components: lemmatizer, tagger, and
 * parser
 */
public class Pipeline {

    //	how to parse a sentences and call the tools
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
            // provide a default sentence 
            i.init(new String[]{"<root>", "This", "is", "a", "test", "."});
        }

        //print the forms
        for (String l : i.forms) {
            Parser.out.println("form : " + l);
        }

        // tell the lemmatizer the location of the model
        is2.lemmatizer.Options optsLemmatizer = new is2.lemmatizer.Options(new String[]{"-model", "models/lemma-eng.model"});

        // create a lemmatizer
        Lemmatizer lemmatizer = new Lemmatizer(optsLemmatizer);

        // lemmatize a sentence; the result is stored in the stenenceData09 i 
        i = lemmatizer.apply(i);


        // output the lemmata
        for (String l : i.plemmas) {
            Parser.out.println("lemma : " + l);
        }

        // tell the tagger the location of the model
        is2.tag.Options optsTagger = new is2.tag.Options(new String[]{"-model", "models/tag-eng.model"});
        Tagger tagger = new Tagger(optsTagger);



//		String pos[] =tagger.tag(i.forms, i.lemmas);
//		i.setPPos(pos);


        SentenceData09 tagged = tagger.tag(i);
        for (String p : tagged.ppos) {
            Parser.out.println("pos " + p);
        }



        // initialize the options 
        Options optsParser = new Options(new String[]{"-model", "models/prs-eng-x.model"});

        // create a parser
        Parser parser = new Parser(optsParser);

        // parse the sentence (you get a copy of the input i)
        SentenceData09 parse = parser.apply(tagged);

        Parser.out.println(parse.toString());

        // create some trash on the hard drive :-)
        is2.io.CONLLWriter09 writer = new is2.io.CONLLWriter09("example-out.txt");

        writer.write(i);
        writer.finishWriting();
    }
}