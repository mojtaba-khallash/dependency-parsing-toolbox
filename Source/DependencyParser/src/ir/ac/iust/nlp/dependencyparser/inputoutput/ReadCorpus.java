package ir.ac.iust.nlp.dependencyparser.inputoutput;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mojtaba Khallash
 */
public class ReadCorpus {
    public static void getStatistics(String source) {
        int sen = 0;
        int nonProjSen = 0;
        int projSen = 0;
        int word = 0;
        int nonProj = 0;
        int proj = 0;
        int totlaLen = 0;
        int maxLen = 0;
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                         new FileInputStream(source), "UTF8"))) {
                List<Integer> Min = new LinkedList<>();
                List<Integer> Max = new LinkedList<>();
                String line;
                boolean isProjSen = true;
                int len = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() != 0) {
                        len++;
                        word++;
                        String[] parts = line.split("\t");
                        
                        int current = Integer.parseInt(parts[0]);
                        int head = Integer.parseInt(parts[6]);

                        if (Math.abs(current - head) > 1) {
                            boolean isProj = true;
                            for (int i = 0; i < Min.size(); i++) {
                                int min = Min.get(i);
                                int max = Max.get(i);
                                if (min < head && head < max &&
                                    (current < min || max < current)) {
                                    nonProj++;
                                    isProj = false;
                                    isProjSen = false;
                                    break;
                                }
                                else if (min < current && current < max &&
                                        (head < min || max < head)) {
                                    nonProj++;
                                    isProj = false;
                                    isProjSen = false;
                                    break;
                                }
                            }
                            if (isProj == true) {
                                proj++;
                            }
                            
                            Min.add(Math.min(current, head));
                            Max.add(Math.max(current, head));
                        }
                        else {
                            proj++;
                        }
                    }
                    else {
                        if (isProjSen == false) {
                            nonProjSen++;
                        }
                        else {
                            projSen++;
                        }
                        isProjSen = true;
                        
                        totlaLen += len;
                        maxLen = Math.max(maxLen, len);
                        len = 0;
                        
                        sen++;
                        
                        Min = new LinkedList<>();
                        Max = new LinkedList<>();
                    }
                }
            }
        }
        catch(IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
        String pattern = "%.2f";
        
        System.out.println("# Sentences:                " + sen);
        System.out.println("# Words:                    " + word);
        System.out.println("------------------------------------------------");
        System.out.println("# Maximum length:           " + maxLen);
        System.out.println("# Average length:           " + String.format(pattern, totlaLen / (float)sen));
        System.out.println("------------------------------------------------");
        System.out.println("# Projective Arcs:          " + proj + " (" + String.format(pattern, (proj * 100.0 / word)) + "%)");
        System.out.println("# Non-Projective Arcs:      " + nonProj + " (" + String.format(pattern, (nonProj * 100.0 / word)) + "%)");
        System.out.println("------------------------------------------------");
        System.out.println("# Projective Sentences:     " + projSen + " (" + String.format(pattern, (projSen * 100.0 / sen)) + "%)");
        System.out.println("# Non-Projective Sentences: " + nonProjSen + " (" + String.format(pattern, (nonProjSen * 100.0 / sen)) + "%)");
    }
}