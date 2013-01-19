package clear.model;

import clear.train.kernel.PolynomialKernel;
import clear.util.tuple.JIntDoubleTuple;
import com.carrotsearch.hppc.IntArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class SupportVectorModel {

    public int n_sv;
    public boolean b_binary;
    public double[] d_alpha;
    public int[][] i_sv;
    public double[][] d_sv;

    public SupportVectorModel() {
    }

    public SupportVectorModel(double[] alpha, int[][] iSVs, double[][] dSVs) {
        n_sv = alpha.length;
        b_binary = dSVs == null;
        d_alpha = alpha;
        i_sv = iSVs;
        d_sv = dSVs;
    }

    public void load(BufferedReader fin) throws Exception {
        n_sv = Integer.parseInt(fin.readLine());
        b_binary = Boolean.parseBoolean(fin.readLine());
        loadAlphas(fin);
        loadSupportVectors(fin);
    }

    private void loadAlphas(BufferedReader fin) throws IOException {
        String[] tmp = fin.readLine().split(" ");
        d_alpha = new double[n_sv];

        for (int i = 0; i < n_sv; i++) {
            d_alpha[i] = Double.parseDouble(tmp[i]);
        }
    }

    private void loadSupportVectors(BufferedReader fin) throws IOException {
        int gap = b_binary ? 1 : 2, i, j, k, length;
        String[] tmp;

        i_sv = new int[n_sv][];
        if (!b_binary) {
            d_sv = new double[n_sv][];
        }

        for (i = 0; i < n_sv; i++) {
            tmp = fin.readLine().split(" ");
            length = tmp.length / gap;

            int[] xi = new int[length];

            for (j = 0; j < length; j++) {
                xi[j] = Integer.parseInt(tmp[j]);
            }

            i_sv[i] = xi;

            if (!b_binary) {
                double[] vi = new double[length];

                for (j = 0, k = length; j < length; j++, k++) {
                    vi[j] = Double.parseDouble(tmp[k]);
                }

                d_sv[i] = vi;
            }
        }
    }

    public void print(PrintStream fout) {
        fout.println(n_sv);
        fout.println(b_binary);
        printAlphas(fout);
        printSupportVectors(fout);
    }

    private void printAlphas(PrintStream fout) {
        StringBuilder build = new StringBuilder();

        for (int i = 0; i < d_alpha.length; i++) {
            build.append(d_alpha[i]);
            build.append(" ");
        }

        fout.println(build.toString());
    }

    private void printSupportVectors(PrintStream fout) {
        StringBuilder build = new StringBuilder();
        int i, j;
        int[] xi;
        double[] vi;

        for (i = 0; i < n_sv; i++) {
            xi = i_sv[i];

            for (j = 0; j < xi.length; j++) {
                build.append(xi[j]);
                build.append(" ");
            }

            if (!b_binary) {
                vi = d_sv[i];

                for (j = 0; j < vi.length; j++) {
                    build.append(vi[j]);
                    build.append(" ");
                }
            }

            build.append("\n");
        }

        fout.print(build.toString());
    }

    public double getScore(int[] x, double gamma, double coef, int degree) {
        double score = 0, scala;
        int i;

        for (i = 0; i < n_sv; i++) {
            scala = PolynomialKernel.getScala(x, i_sv[i]);
            score += d_alpha[i] * PolynomialKernel.getPolyValue(scala, gamma, coef, degree);
        }

        return score;
    }

    public double getScore(IntArrayList x, double gamma, double coef, int degree) {
        double score = 0, scala;
        int i;

        for (i = 0; i < n_sv; i++) {
            scala = PolynomialKernel.getScala(x, i_sv[i]);
            score += d_alpha[i] * PolynomialKernel.getPolyValue(scala, gamma, coef, degree);
        }

        return score;
    }

    public double getScore(ArrayList<JIntDoubleTuple> x, double gamma, double coef, int degree) {
        double score = 0, scala;
        int i;

        for (i = 0; i < n_sv; i++) {
            scala = PolynomialKernel.getScala(x, i_sv[i], d_sv[i]);
            score += d_alpha[i] * PolynomialKernel.getPolyValue(scala, gamma, coef, degree);
        }

        return score;
    }
}