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

/**
 * @author Dipanjan Das 6/4/08 dipanjan@cs.cmu.edu
 *
 * Adapted from code by Ryan McDonald (ryanmcd@google.com)
 *
 */
public class MalletFeatures {

    // Change this method to add new features
    // Add features for child ch, in sentence toks, with POS tags pos and parent function par
    public static String getFeats(String[] toks, String[] pos, String[] labs, int[] par, String[] depPred, int[] headPred, int ch) {

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

            if (depPred != null || headPred != null) {
                //include things here
                int ach = ch;
                int apa = headPred[ch];
                String aux_lab = depPred[ch];
                res += " " + "EXT=" + aux_lab + suff;
                res += " " + "POSEXT=" + aux_lab + "__" + pos[pa] + "_" + pos[ch] + suff;
                res += " " + "EXT=" + aux_lab + suff + "_" + (ach == ch && apa == par[ch]);
                res += " " + "POSEXT=" + aux_lab + "__" + pos[pa] + "_" + pos[ch] + suff + "_" + (ach == ch && apa == par[ch]);
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
}