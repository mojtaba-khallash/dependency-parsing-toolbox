///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2008 Carnegie Mellon University and 
// (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
// 
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////
package mstparser.mallet;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Dipanjan Das 6/4/08 dipanjan@cs.cmu.edu
 *
 * Adapted from code by Ryan McDonald (ryanmcd@google.com)
 *
 */
public class Convert {

    public static String[] convert(BufferedReader in, BufferedReader[] aux_in) throws IOException {

        String line = in.readLine();

        if (line == null) {
            return null;
        }

        String[] tokstmp = line.split("\t");
        String[] postmp = in.readLine().split("\t");
        in.readLine();
        String[] labtmp = in.readLine().split("\t");
        String[] depstmp = in.readLine().split("\t");
        String[][] aux_lines = new String[aux_in.length][];
        for (int i = 0; i < aux_lines.length; i++) {
            aux_lines[i] = aux_in[i].readLine().split(" ");
        }

        String[] res = convert(tokstmp, postmp, labtmp, depstmp, aux_lines);

        return res;
    }

    public static String[] convert(String[] tokstmp, String[] postmp, String[] labtmp, String[] depstmp, String[][] aux_lines) {
        String[] toks = new String[tokstmp.length + 1];
        String[] pos = new String[postmp.length + 1];
        String[] lab = new String[labtmp.length + 1];
        int[] par = new int[depstmp.length + 1];
        toks[0] = "<root>";
        pos[0] = "<root-POS>";
        lab[0] = "<root-LAB>";
        par[0] = -1;
        for (int i = 0; i < depstmp.length; i++) {
            par[i + 1] = Integer.parseInt(depstmp[i]);
            toks[i + 1] = tokstmp[i];
            pos[i + 1] = postmp[i];
            lab[i + 1] = labtmp[i];
        }

        String[] res = new String[toks.length - 1];
        for (int i = 1; i < par.length; i++) {
            res[i - 1] = lab[i] + " " + getFeats(toks, pos, aux_lines, lab, par, i).trim();
        }

        return res;
    }

    // Change this method to add new features
    // Add features for child ch, in sentence toks, with POS tags pos and parent function par
    public static String getFeats(String[] toks, String[] pos, String[][] aux_lines, String[] labs, int[] par, int ch) {

        String[] toks_low = new String[toks.length];
        for (int i = 0; i < toks.length; i++) {
            toks_low[i] = toks[i].toLowerCase();
        }
        toks = toks_low;

        String att = ch < par[ch] ? "LFT" : "RGT";
        int pa = par[ch];
        String res = att;

        for (int a = 0; a < 2; a++) {
            String suff = a == 0 ? "" : "_" + att;

            // auxiliary files
            for (int i = 0; i < aux_lines.length; i++) {
                int ach = Integer.parseInt(aux_lines[i][ch - 1].split("[\\|:]")[1]);
                int apa = Integer.parseInt(aux_lines[i][ch - 1].split("[\\|:]")[0]);
                String aux_lab = aux_lines[i][ch - 1].split(":")[1];
                res += " " + "EXT" + i + "=" + aux_lab + suff;
                res += " " + "POSEXT" + i + "=" + aux_lab + "__" + pos[pa] + "_" + pos[ch] + suff;
                res += " " + "EXT" + i + "=" + aux_lab + suff + "_" + (ach == ch && apa == par[ch]);
                res += " " + "POSEXT" + i + "=" + aux_lab + "__" + pos[pa] + "_" + pos[ch] + suff + "_" + (ach == ch && apa == par[ch]);
            }

            // standard word/pos features
            res += " " + "POSCH=" + pos[ch] + suff;
            res += " " + "POSPA=" + pos[par[ch]] + suff;
            res += " " + "WRDCH=" + toks[ch] + suff;
            res += " " + "WRDPA=" + toks[par[ch]] + suff;
            res += " " + "POSP=" + pos[pa] + "_" + pos[ch] + suff;
            res += " " + "WRDP=" + toks[pa] + "_" + toks[ch] + suff;
            res += " " + "WRDPOS=" + toks[pa] + "_" + pos[ch] + suff;
            res += " " + "POSWRD=" + pos[pa] + "_" + toks[ch] + suff;

            if (ch > 0) {
                res += " " + "POSCH-1=" + pos[ch - 1] + suff;
                res += " " + "APOSCH-1=" + pos[ch - 1] + "_" + pos[ch] + suff;
                res += " " + "WRDCH-1=" + toks[ch - 1] + suff;
            }
            if (ch > 1) {
                res += " " + "POSCH-2=" + pos[ch - 2] + suff;
                res += " " + "APOSCH-2=" + pos[ch - 2] + "_" + pos[ch] + suff;
                res += " " + "WRDCH-2=" + toks[ch - 2] + suff;
            }
            if (ch < toks.length - 2) {
                res += " " + "POSCH+2=" + pos[ch + 2] + suff;
                res += " " + "APOSCH+1=" + pos[ch + 2] + "_" + pos[ch] + suff;
                res += " " + "WRDCH+2=" + toks[ch + 2] + suff;
            }
            if (ch < toks.length - 1) {
                res += " " + "POSCH+1=" + pos[ch + 1] + suff;
                res += " " + "APOSCH+1=" + pos[ch + 1] + "_" + pos[ch] + suff;
                res += " " + "WRDCH+1=" + toks[ch + 1] + suff;
            }
            if (ch > 0 && ch < toks.length - 1) {
                res += " " + "APOSCH+1-1=" + pos[ch - 1] + "_" + pos[ch] + "_" + pos[ch + 1] + suff;
            }

            if (pa > 0) {
                res += " " + "POSPA-1=" + pos[pa - 1] + suff;
                res += " " + "APOSPA-1=" + pos[pa - 1] + "_" + pos[pa] + suff;
                res += " " + "WRDPA-1=" + toks[pa - 1] + suff;
            }
            if (pa > 1) {
                res += " " + "POSPA-2=" + pos[pa - 2] + suff;
                res += " " + "APOSPA-2=" + pos[pa - 2] + "_" + pos[pa] + suff;
                res += " " + "WRDPA-2=" + toks[pa - 2] + suff;
            }
            if (pa < toks.length - 2) {
                res += " " + "POSPA+2=" + pos[pa + 2] + suff;
                res += " " + "APOSPA+2=" + pos[pa + 2] + "_" + pos[pa] + suff;
                res += " " + "WRDPA+2=" + toks[pa + 2] + suff;
            }
            if (pa < toks.length - 1) {
                res += " " + "POSPA+1=" + pos[pa + 1] + suff;
                res += " " + "APOSPA+1=" + pos[pa + 1] + "_" + pos[pa] + suff;
                res += " " + "WRDPA+1=" + toks[pa + 1] + suff;
            }
            if (pa > 0 && pa < toks.length - 1) {
                res += " " + "APOSPA+1-1=" + pos[pa - 1] + "_" + pos[pa] + "_" + pos[pa + 1] + suff;
            }


            // POS in-between
            for (int i = Math.min(ch, pa) + 1; i < Math.max(ch, pa); i++) {
                res += " " + "POST=" + pos[pa] + "_" + pos[ch] + "_" + pos[i] + suff;
                res += " " + "APOST=" + pos[ch] + "_" + pos[i] + suff;
                res += " " + "BPOST=" + pos[pa] + "_" + pos[i] + suff;
                res += " " + "CPOST=" + pos[i] + suff;
            }

        }

        return res;
    }

    private static int getSibL(int ch, int[] par) {
        for (int i = ch; i >= 0; i--) {
            if (par[i] == par[ch]) {
                return i;
            }
        }
        return -1;
    }

    private static int getSibR(int ch, int[] par) {
        for (int i = ch; i < par.length; i++) {
            if (par[i] == par[ch]) {
                return i;
            }
        }
        return -1;
    }
}