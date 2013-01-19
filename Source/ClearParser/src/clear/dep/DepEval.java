/**
 * Copyright (c) 2009, Regents of the University of Colorado All rights
 * reserved.
 * 
* Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
* Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of the University of Colorado at
 * Boulder nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package clear.dep;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Compare two dependency trees.
 *
 * @author Jinho D. Choi <b>Last update:</b> 4/26/2010
 */
public class DepEval {

    private int n_las;
    private int n_uas;
    private int n_ls;
    private int n_dep;
    private double d_las;
    private double d_uas;
    private double d_ls;
    private int n_tree;
    private ObjectIntOpenHashMap<String> m_las;
    private ObjectIntOpenHashMap<String> m_uas;
    private ObjectIntOpenHashMap<String> m_ls;
    private ObjectIntOpenHashMap<String> m_dep;
    private byte b_skip;

    public DepEval(byte skip) {
        n_las = n_uas = n_ls = n_dep = 0;
        d_las = d_uas = d_ls = n_tree = 0;
        m_las = new ObjectIntOpenHashMap<>();
        m_uas = new ObjectIntOpenHashMap<>();
        m_ls = new ObjectIntOpenHashMap<>();
        m_dep = new ObjectIntOpenHashMap<>();

        b_skip = skip;
    }

    public void evaluate(DepTree gold, DepTree sys) {
        int[] acc = new int[3];

        for (int i = 1; i < gold.size(); i++) {
            DepNode gNode = gold.get(i);
            DepNode sNode = sys.get(i);

            if (b_skip == 1 && sNode.headId == DepLib.NULL_HEAD_ID) {
                continue;
            }

            if (gNode.isPos("-NONE-")) {
                continue;
            }

            measure(gNode, sNode, acc);
        }

        int total = gold.size() - 1;

        n_las += acc[0];
        n_uas += acc[1];
        n_ls += acc[2];
        n_dep += total;

        d_las += (double) acc[0] / total;
        d_uas += (double) acc[1] / total;
        d_ls += (double) acc[2] / total;
        n_tree++;
    }

    static public double getLas(DepTree gold, DepTree sys) {
        int correct = 0, size = gold.size();

        for (int i = 1; i < size; i++) {
            DepNode gNode = gold.get(i);
            DepNode sNode = sys.get(i);

            if (gNode.isDeprel(sNode.deprel) && gNode.headId == sNode.headId) {
                correct++;
            }
        }

        return (double) correct / (size - 1);
    }

    private void measure(DepNode gNode, DepNode sNode, int[] acc) {
        String gDeprel = gNode.deprel;

        if (gNode.isDeprel(sNode.deprel)) {
            acc[2]++;
            m_ls.put(gDeprel, m_ls.get(gDeprel) + 1);

            if (gNode.headId == sNode.headId) {
                acc[0]++;
                m_las.put(gDeprel, m_las.get(gDeprel) + 1);
            }
        }

        if (gNode.headId == sNode.headId) {
            acc[1]++;
            m_uas.put(gDeprel, m_uas.get(gDeprel) + 1);
        }

        m_dep.put(gDeprel, m_dep.get(gDeprel) + 1);
    }

    public double getLas() {
        return (double) n_las / n_dep;
    }

    public double getUas() {
        return (double) n_uas / n_dep;
    }

    public double getLs() {
        return (double) n_ls / n_dep;
    }

    public void printTotal() {
        System.out.println("--------------------------------------------------");
        System.out.printf("%10s%10s%10s%10s%10s\n", "Label", "Dist", "LAS", "UAS", "LS");
        System.out.println("--------------------------------------------------");

        double las = getLas() * 100;
        double uas = getUas() * 100;
        double ls = getLs() * 100;

        System.out.printf("%10s%10.2f%10.2f%10.2f%10.2f\n", "Micro", 100d, las, uas, ls);

        las = (d_las / n_tree) * 100;
        uas = (d_uas / n_tree) * 100;
        ls = (d_ls / n_tree) * 100;

        System.out.printf("%10s%10.2f%10.2f%10.2f%10.2f\n", "Macro", 100d, las, uas, ls);
        System.out.println("--------------------------------------------------");

        ArrayList<String> labels = new ArrayList<>(m_dep.size());
        for (ObjectCursor<String> cur : m_dep.keys()) {
            labels.add(cur.value);
        }

        Collections.sort(labels);
        int total;

        for (String label : labels) {
            total = m_dep.get(label);
            las = 100d * m_las.get(label) / total;
            uas = 100d * m_uas.get(label) / total;
            ls = 100d * m_ls.get(label) / total;

            System.out.printf("%10s%10.2f%10.2f%10.2f%10.2f\n", label, 100d * total / n_dep, las, uas, ls);
        }

        System.out.println("--------------------------------------------------");
    }
}