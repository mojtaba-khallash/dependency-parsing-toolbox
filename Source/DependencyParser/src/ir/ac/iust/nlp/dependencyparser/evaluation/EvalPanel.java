package ir.ac.iust.nlp.dependencyparser.evaluation;

import ir.ac.iust.nlp.dependencyparser.BasePanel;
import ir.ac.iust.nlp.dependencyparser.utility.ExampleFileFilter;
import java.awt.Color;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import org.apache.commons.io.FileUtils;
import se.vxu.msi.malteval.MaltEvalConsole;

/**
 *
 * @author Mojtaba Khallash
 */
public class EvalPanel extends BasePanel {

    boolean isInit = false;
    private String[][] selects = new String[][]{
        // Token                    -   0
        new String[]{"accuracy", "counter", "correctcounter"},
        // Wordform                 -   1
        new String[]{"accuracy", "counter", "correctcounter"},
        // Lemma                    -   2
        new String[]{"accuracy", "counter", "correctcounter"},
        // Cpostag                  -   3
        new String[]{"accuracy", "counter", "correctcounter"},
        // Postag                   -   4
        new String[]{"accuracy", "counter", "correctcounter"},
        // Feats                    -   5
        new String[]{"accuracy", "counter", "correctcounter"},
        // Deprel                   -   6
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // Sentence                 -   7
        new String[]{"accuracy", "exactmatch", "correctcounter", "includedtokenscount", "sentencelength", "isparserconnected", "istreebankconnected", "hasparsercycle", "hastreebankcycle", "isparserprojective", "istreebankprojective", "id"},
        // RelationLength           -   8
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // GroupedRelationLength    -   9
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // SentenceLength           -   10
        new String[]{"accuracy", "counter", "correctcounter"},
        // StartWordPosition        -   11
        new String[]{"accuracy", "counter", "correctcounter"},
        // EndWordPosition          -   12
        new String[]{"accuracy", "counter", "correctcounter"},
        // ArcDirection             -   13
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // ArcDepth                 -   14
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // BranchingFactor          -   15
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // ArcProjectivity          -   16
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},
        // Frame                    -   17
        new String[]{"parseraccuracy", "treebankaccuracy", "parsercounter", "treebankcounter", "parsercorrectcounter", "treebankcorrectcounter"},};

    /**
     * Creates new form EvalPanel
     */
    public EvalPanel(String dir) {
        super(dir);

        initComponents();

        setDrop();

        lblLess.setVisible(false);
        pnlAdvancedParameter.setVisible(false);

        setCurrentDirectory(dir);

        lstMetrics.setSelectedIndex(0);

        SpinnerNumberModel num_model = new SpinnerNumberModel(3, 1, 10, 1);
        spPattern.setModel(num_model);

        num_model = new SpinnerNumberModel(1, 1, 1000, 1);
        spMinSentenceLength.setModel(num_model);
        num_model = new SpinnerNumberModel(1, 1, 1000, 1);
        spMaxSentenceLength.setModel(num_model);

        setSelectSortCut();
    }
    
    private void setDrop() {
        initDrop(txtGoldFile, false);
        initDrop(txtParseFile, false);
        initDrop(txtOutputFile, true);
    }

    public final void setCurrentDirectory(String dir) {
        txtGoldFile.setText(dir + File.separator
                + "Treebank" + File.separator
                + "Persian" + File.separator
                + "test.conll");
        txtParseFile.setText(dir + File.separator
                + "Treebank" + File.separator
                + "Persian" + File.separator
                + "parse.conll");
        txtOutputFile.setText(dir + File.separator);
    }

    private void setSelectSortCut() {
        DefaultListModel modelSelect = (DefaultListModel) lstSelect.getModel();
        modelSelect.clear();
        DefaultListModel modelSort = (DefaultListModel) lstSort.getModel();
        modelSort.clear();
        DefaultListModel modelCut = (DefaultListModel) lstCut.getModel();
        modelCut.clear();

        String[] selectors = selects[cboGroupBy.getSelectedIndex()];
        for (int i = 0; i < selectors.length; i++) {
            modelSelect.addElement(selectors[i]);
            modelSort.addElement(" ");
            modelCut.addElement(" ");
        }
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public void save() {
        // Output
        ExampleFileFilter filter = new ExampleFileFilter("txt", "Text Files");
        saveText(filter, "Evaluation_log.txt", txtEvalResults.getText());
    }

    @Override
    public void saveAs() {
        save();
    }
    
    @Override
    public void threadFinished() {
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pnlSettings = new javax.swing.JPanel();
        lblGoldFile = new javax.swing.JLabel();
        txtGoldFile = new javax.swing.JTextField();
        btnBrowseGoldFile = new javax.swing.JButton();
        lblParseFile = new javax.swing.JLabel();
        txtParseFile = new javax.swing.JTextField();
        btnBrowseParseFile = new javax.swing.JButton();
        pnlMore = new javax.swing.JPanel();
        lblMore = new javax.swing.JLabel();
        lblLess = new javax.swing.JLabel();
        pnlAdvancedParameter = new javax.swing.JPanel();
        lblOutputFile = new javax.swing.JLabel();
        txtOutputFile = new javax.swing.JTextField();
        btnBrowseOutputFile = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnlEvaluationSettings = new javax.swing.JPanel();
        lblMetric = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstMetrics = new javax.swing.JList();
        pnlGroupBy = new javax.swing.JPanel();
        cboGroupBy = new javax.swing.JComboBox();
        lblSelect = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstSelect = new javax.swing.JList(new DefaultListModel());
        lblSort = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        lstSort = new javax.swing.JList(new DefaultListModel());
        chkSelectAll = new javax.swing.JCheckBox();
        lblCut = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        lstCut = new javax.swing.JList(new DefaultListModel());
        pnlSentenceLength = new javax.swing.JPanel();
        spMinSentenceLength = new javax.swing.JSpinner();
        spMaxSentenceLength = new javax.swing.JSpinner();
        chkMinSentenceLength = new javax.swing.JCheckBox();
        chkMaxSentenceLength = new javax.swing.JCheckBox();
        pnlExclude = new javax.swing.JPanel();
        cboExcludeType = new javax.swing.JComboBox();
        txtExcludeVal = new javax.swing.JTextField();
        btnAddExclude = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        lstExcludeList = new javax.swing.JList(new DefaultListModel());
        btnRemoveExclude = new javax.swing.JButton();
        btnRemoveAllExclude = new javax.swing.JButton();
        lblAlarm = new javax.swing.JLabel();
        pnlFormatSettings = new javax.swing.JPanel();
        lblPattern = new javax.swing.JLabel();
        txtPattern = new javax.swing.JTextField();
        spPattern = new javax.swing.JSpinner();
        chkDetails = new javax.swing.JCheckBox();
        chkColumnHeader = new javax.swing.JCheckBox();
        chkRowHeader = new javax.swing.JCheckBox();
        chkConfusionMatrix = new javax.swing.JCheckBox();
        chkMergeTables = new javax.swing.JCheckBox();
        chkTabSeparate = new javax.swing.JCheckBox();
        btnEval = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtEvalResults = new javax.swing.JTextArea();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(380, 2));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("Evaluation");

        pnlSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));

        lblGoldFile.setText("Gold File:");

        txtGoldFile.setEditable(false);

        btnBrowseGoldFile.setText("...");
        btnBrowseGoldFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseGoldFile_Click(evt);
            }
        });

        lblParseFile.setText("Parse File:");

        txtParseFile.setEditable(false);

        btnBrowseParseFile.setText("...");
        btnBrowseParseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseParseFile_Click(evt);
            }
        });

        pnlMore.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 240, 240)));

        lblMore.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblMore.setForeground(new java.awt.Color(153, 153, 153));
        lblMore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ir/ac/iust/nlp/dependencyparser/evaluation/directional_down.png"))); // NOI18N
        lblMore.setText("  More");
        lblMore.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMore_mouseClick(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlMore_mouseEnter(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlMore_mouseExit(evt);
            }
        });

        lblLess.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblLess.setForeground(new java.awt.Color(153, 153, 153));
        lblLess.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ir/ac/iust/nlp/dependencyparser/evaluation/directional_up.png"))); // NOI18N
        lblLess.setText("  Less");
        lblLess.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLess_mouseClick(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlMore_mouseEnter(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlMore_mouseExit(evt);
            }
        });

        javax.swing.GroupLayout pnlMoreLayout = new javax.swing.GroupLayout(pnlMore);
        pnlMore.setLayout(pnlMoreLayout);
        pnlMoreLayout.setHorizontalGroup(
            pnlMoreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMoreLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblMore, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLess)
                .addContainerGap())
        );
        pnlMoreLayout.setVerticalGroup(
            pnlMoreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMoreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblLess, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblMore, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        lblOutputFile.setText("Output File:");

        txtOutputFile.setEditable(false);

        btnBrowseOutputFile.setText("...");
        btnBrowseOutputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseOutputFile_Click(evt);
            }
        });

        lblMetric.setText("Metric");

        lstMetrics.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "LAS", "LA", "UAS", "AnyRight", "AnyWrong", "BothRight", "BothWrong", "HeadRight", "HeadWrong", "LabelRight", "LabelWrong", "DirectionRight", "GroupedHeadToChildDistanceRight", "HeadToChildDistanceRight" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstMetrics);

        pnlGroupBy.setBorder(javax.swing.BorderFactory.createTitledBorder("GroupBy"));

        cboGroupBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Token", "Wordform", "Lemma", "Cpostag", "Postag", "Feats", "Deprel", "Sentence", "RelationLength", "GroupedRelationLength", "SentenceLength", "StartWordPosition", "EndWordPosition", "ArcDirection", "ArcDepth", "BranchingFactor", "ArcProjectivity", "Frame" }));
        cboGroupBy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboGroupBy_stateChanged(evt);
            }
        });

        lblSelect.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSelect.setText("Select");

        lstSelect.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstSelect_valueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstSelect);

        lblSort.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSort.setText("Sort");

        lstSort.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstSort.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstSort_mouseClick(evt);
            }
        });
        jScrollPane4.setViewportView(lstSort);

        chkSelectAll.setText("Select All");
        chkSelectAll.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkSelectAll_stateChanged(evt);
            }
        });

        lblCut.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCut.setText("Cut");

        lstCut.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstCut_mouseClick(evt);
            }
        });
        jScrollPane6.setViewportView(lstCut);

        javax.swing.GroupLayout pnlGroupByLayout = new javax.swing.GroupLayout(pnlGroupBy);
        pnlGroupBy.setLayout(pnlGroupByLayout);
        pnlGroupByLayout.setHorizontalGroup(
            pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGroupByLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboGroupBy, 0, 208, Short.MAX_VALUE)
                    .addComponent(chkSelectAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlGroupByLayout.createSequentialGroup()
                        .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSort, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCut, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        pnlGroupByLayout.setVerticalGroup(
            pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGroupByLayout.createSequentialGroup()
                .addComponent(cboGroupBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelect)
                    .addComponent(lblSort)
                    .addComponent(lblCut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlGroupByLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkSelectAll))
        );

        pnlSentenceLength.setBorder(javax.swing.BorderFactory.createTitledBorder("Sentence Length"));

        spMinSentenceLength.setEnabled(false);
        spMinSentenceLength.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spMinSentenceLength_stateChanged(evt);
            }
        });

        spMaxSentenceLength.setEnabled(false);

        chkMinSentenceLength.setText("Min:");
        chkMinSentenceLength.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkMinSentenceLength_stateChanged(evt);
            }
        });

        chkMaxSentenceLength.setText("Max:");
        chkMaxSentenceLength.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chkMaxSentenceLength_stateChanged(evt);
            }
        });

        javax.swing.GroupLayout pnlSentenceLengthLayout = new javax.swing.GroupLayout(pnlSentenceLength);
        pnlSentenceLength.setLayout(pnlSentenceLengthLayout);
        pnlSentenceLengthLayout.setHorizontalGroup(
            pnlSentenceLengthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSentenceLengthLayout.createSequentialGroup()
                .addGroup(pnlSentenceLengthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSentenceLengthLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(chkMaxSentenceLength)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spMaxSentenceLength, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                    .addGroup(pnlSentenceLengthLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(chkMinSentenceLength)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spMinSentenceLength)))
                .addContainerGap())
        );
        pnlSentenceLengthLayout.setVerticalGroup(
            pnlSentenceLengthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSentenceLengthLayout.createSequentialGroup()
                .addGroup(pnlSentenceLengthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spMinSentenceLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkMinSentenceLength))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSentenceLengthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spMaxSentenceLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkMaxSentenceLength))
                .addGap(0, 9, Short.MAX_VALUE))
        );

        pnlExclude.setBorder(javax.swing.BorderFactory.createTitledBorder("Exclude"));

        cboExcludeType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Wordforms", "Lemmas", "Cpostags", "Postags", "Feats", "Deprels", "Pdeprels" }));

        txtExcludeVal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtExcludeVal_keyReleased(evt);
            }
        });

        btnAddExclude.setText("Add");
        btnAddExclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddExclude_Click(evt);
            }
        });

        lstExcludeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstExcludeList_valueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(lstExcludeList);

        btnRemoveExclude.setText("Remove");
        btnRemoveExclude.setEnabled(false);
        btnRemoveExclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveExclude_Click(evt);
            }
        });

        btnRemoveAllExclude.setText("Remove All");
        btnRemoveAllExclude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveAllExclude_Click(evt);
            }
        });

        lblAlarm.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAlarm.setText("use \"|\" for \"and\" some excludes.");

        javax.swing.GroupLayout pnlExcludeLayout = new javax.swing.GroupLayout(pnlExclude);
        pnlExclude.setLayout(pnlExcludeLayout);
        pnlExcludeLayout.setHorizontalGroup(
            pnlExcludeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExcludeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExcludeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAlarm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(cboExcludeType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlExcludeLayout.createSequentialGroup()
                        .addComponent(txtExcludeVal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddExclude))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlExcludeLayout.createSequentialGroup()
                        .addComponent(btnRemoveExclude)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveAllExclude)))
                .addContainerGap())
        );
        pnlExcludeLayout.setVerticalGroup(
            pnlExcludeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExcludeLayout.createSequentialGroup()
                .addComponent(cboExcludeType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExcludeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtExcludeVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddExclude))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlExcludeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemoveExclude)
                    .addComponent(btnRemoveAllExclude)))
        );

        javax.swing.GroupLayout pnlEvaluationSettingsLayout = new javax.swing.GroupLayout(pnlEvaluationSettings);
        pnlEvaluationSettings.setLayout(pnlEvaluationSettingsLayout);
        pnlEvaluationSettingsLayout.setHorizontalGroup(
            pnlEvaluationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEvaluationSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEvaluationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                    .addComponent(lblMetric))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlGroupBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlEvaluationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlSentenceLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlExclude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlEvaluationSettingsLayout.setVerticalGroup(
            pnlEvaluationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEvaluationSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEvaluationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEvaluationSettingsLayout.createSequentialGroup()
                        .addComponent(lblMetric)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1))
                    .addComponent(pnlGroupBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlEvaluationSettingsLayout.createSequentialGroup()
                        .addComponent(pnlSentenceLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlExclude, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Evaluation Settings", pnlEvaluationSettings);

        lblPattern.setText("Pattern:");

        txtPattern.setText("0.000");

        spPattern.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spPattern_Changed(evt);
            }
        });

        chkDetails.setText("Show Details");

        chkColumnHeader.setSelected(true);
        chkColumnHeader.setText("Show Column Header ");

        chkRowHeader.setSelected(true);
        chkRowHeader.setText("Show Row Header");

        chkConfusionMatrix.setText("Compute Confusion Matrix");

        chkMergeTables.setSelected(true);
        chkMergeTables.setText("Merge Tables");

        chkTabSeparate.setText("Tab Separate Column");

        javax.swing.GroupLayout pnlFormatSettingsLayout = new javax.swing.GroupLayout(pnlFormatSettings);
        pnlFormatSettings.setLayout(pnlFormatSettingsLayout);
        pnlFormatSettingsLayout.setHorizontalGroup(
            pnlFormatSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormatSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormatSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormatSettingsLayout.createSequentialGroup()
                        .addComponent(lblPattern)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 309, Short.MAX_VALUE)
                        .addComponent(chkConfusionMatrix))
                    .addGroup(pnlFormatSettingsLayout.createSequentialGroup()
                        .addGroup(pnlFormatSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkTabSeparate)
                            .addComponent(chkMergeTables)
                            .addComponent(chkRowHeader)
                            .addComponent(chkColumnHeader)
                            .addComponent(chkDetails))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlFormatSettingsLayout.setVerticalGroup(
            pnlFormatSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormatSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormatSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPattern)
                    .addComponent(spPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkConfusionMatrix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkDetails)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkColumnHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkRowHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkMergeTables)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkTabSeparate)
                .addContainerGap(150, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Format Settings", pnlFormatSettings);

        javax.swing.GroupLayout pnlAdvancedParameterLayout = new javax.swing.GroupLayout(pnlAdvancedParameter);
        pnlAdvancedParameter.setLayout(pnlAdvancedParameterLayout);
        pnlAdvancedParameterLayout.setHorizontalGroup(
            pnlAdvancedParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdvancedParameterLayout.createSequentialGroup()
                .addComponent(lblOutputFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtOutputFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBrowseOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jTabbedPane1)
        );
        pnlAdvancedParameterLayout.setVerticalGroup(
            pnlAdvancedParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAdvancedParameterLayout.createSequentialGroup()
                .addGroup(pnlAdvancedParameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblOutputFile)
                    .addComponent(btnBrowseOutputFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        btnEval.setText("Evaluate");
        btnEval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEval_Click(evt);
            }
        });

        javax.swing.GroupLayout pnlSettingsLayout = new javax.swing.GroupLayout(pnlSettings);
        pnlSettings.setLayout(pnlSettingsLayout);
        pnlSettingsLayout.setHorizontalGroup(
            pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlAdvancedParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlSettingsLayout.createSequentialGroup()
                        .addComponent(pnlMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEval))
                    .addGroup(pnlSettingsLayout.createSequentialGroup()
                        .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblParseFile)
                            .addComponent(lblGoldFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtGoldFile)
                            .addComponent(txtParseFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnBrowseGoldFile, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(btnBrowseParseFile, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        pnlSettingsLayout.setVerticalGroup(
            pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSettingsLayout.createSequentialGroup()
                .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGoldFile)
                    .addComponent(txtGoldFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseGoldFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblParseFile)
                    .addComponent(txtParseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBrowseParseFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlMore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEval))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdvancedParameter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtEvalResults.setColumns(20);
        txtEvalResults.setEditable(false);
        txtEvalResults.setRows(5);
        jScrollPane2.setViewportView(txtEvalResults);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .addComponent(pnlSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseGoldFile_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseGoldFile_Click
        txtGoldFile.setText(showFileDialog(txtGoldFile.getText(), false));
    }//GEN-LAST:event_btnBrowseGoldFile_Click

    private void btnBrowseParseFile_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseParseFile_Click
        txtParseFile.setText(showFileDialog(txtParseFile.getText(), false));
    }//GEN-LAST:event_btnBrowseParseFile_Click

    private void btnEval_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEval_Click
        File goldFile = null,
                parseFile = null,
                log_from = null,
                out = null;
        String out_name = workingDir + File.separator + "result.out";
        try {
            if (isInit == false) {
                isInit = true;

                String inputFile = txtGoldFile.getText();
                // Ensure have an absolute path
                File gold = new File(inputFile).getAbsoluteFile();
                String gold_name = gold.getName();

                goldFile = new File(workingDir + File.separator + gold_name);

                // Copy input file to working directory
                if (!gold.equals(goldFile)) {
                    FileUtils.copyFile(gold, goldFile);
                }

                String inputModelFile = txtParseFile.getText();
                File parse = new File(inputModelFile).getAbsoluteFile();
                String parse_name = parse.getName();

                parseFile = new File(workingDir + File.separator + parse_name);

                // Copy model to working directory
                if (!parse.equals(parseFile)) {
                    FileUtils.copyFile(parse, parseFile);
                }

                StringBuilder metrics = new StringBuilder();
                for (int i = 0; i < lstMetrics.getSelectedIndices().length; i++) {
                    if (i != 0) {
                        metrics.append(";");
                    }
                    metrics.append(lstMetrics.getSelectedValuesList().get(i));
                }

                EvalSettings settings = new EvalSettings();

                settings.goldFile = goldFile.getAbsolutePath();
                settings.parseFile = parseFile.getAbsolutePath();
                settings.outputFile = out_name;
                settings.metrics = metrics.toString();


                StringBuilder groupByVal = new StringBuilder(cboGroupBy.getSelectedItem().toString());
                // Select - Sort - Cut
                if (chkSelectAll.isSelected()) {
                    groupByVal.append(":all");
                } else {
                    int count = lstSelect.getSelectedIndices().length;
                    DefaultListModel modelSort = (DefaultListModel) lstSort.getModel();
                    DefaultListModel modelCut = (DefaultListModel) lstCut.getModel();
                    if (count != 0) {
                        groupByVal.append(":");
                    }
                    for (int i = 0; i < count; i++) {
                        String select = lstSelect.getSelectedValuesList().get(i).toString();
                        String sort = modelSort.getElementAt(i).toString();
                        String cut = modelCut.getElementAt(i).toString();
                        if (i != 0) {
                            groupByVal.append("|");
                        }
                        groupByVal.append(select);
                        if (!sort.equals(" ")) {
                            groupByVal.append(sort);
                        }
                        if (!cut.equals(" ")) {
                            groupByVal.append(cut);
                        }
                    }
                }
                settings.groupByVal = groupByVal.toString();

                if (chkMinSentenceLength.isSelected()) {
                    int min = Integer.parseInt(spMinSentenceLength.getModel().getValue().toString());
                    settings.minSentenceLength = min;
                }
                else {
                    settings.minSentenceLength = -1;
                }
                if (chkMaxSentenceLength.isSelected()) {
                    int max = Integer.parseInt(spMaxSentenceLength.getModel().getValue().toString());
                    settings.maxSentenceLength = max;
                }
                else {
                    settings.maxSentenceLength = -1;
                }

                DefaultListModel model = (DefaultListModel) lstExcludeList.getModel();
                int count = model.getSize();
                if (count != 0) {
                    HashMap<String, String> map = new HashMap<>();
                    for (int i = 0; i < count; i++) {
                        String[] parts = model.get(i).toString().split("=");
                        String key = parts[0];
                        String list = map.get(key);
                        if (list == null) {
                            list = parts[1];
                        } else {
                            list += ";" + parts[1];
                        }
                        map.put(key, list);
                    }

                    Iterator<String> iter = map.keySet().iterator();
                    List keys = new LinkedList();
                    List vals = new LinkedList();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        keys.add(key);
                        String list = map.get(key);
                        vals.add(list);
                    }
                    settings.ExcludeKeys = (String[])keys.toArray(new String[0]);
                    settings.ExcludeVals = (String[])vals.toArray(new String[0]);
                }

                settings.useConfusionMatrix = chkConfusionMatrix.isSelected();
                settings.showDetails = chkDetails.isSelected();
                settings.showHeaderInfo = chkColumnHeader.isSelected();
                settings.showRowInfo = chkRowHeader.isSelected();
                settings.mergeTables = chkMergeTables.isSelected();
                settings.useTabSeparate = chkTabSeparate.isSelected();
                settings.pattern = txtPattern.getText();

                MaltEvalConsole.main(settings.getParameters());
                try (BufferedReader reader = new BufferedReader(new FileReader(out_name))) {
                    String line;
                    StringBuilder text = new StringBuilder("");
                    while ((line = reader.readLine()) != null) {
                        line = new String(line.getBytes(), "UTF-8");
                        text.append(line).append("\n");
                    }
                    txtEvalResults.setText(text.toString());
                }

                // Copy result to destination file
                out = new File(out_name);
                File log_to = new File(txtOutputFile.getText() + "result.out");
                if (!out.equals(log_to)) {
                    FileUtils.copyFile(out, log_to);
                }
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "MaltAPITest exception",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "MaltAPITest exception",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "MaltAPITest exception",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Reading Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (goldFile != null) {
                    FileUtils.forceDelete(goldFile);
                }

                if (parseFile != null) {
                    FileUtils.forceDelete(parseFile);
                }

                if (log_from != null) {
                    FileUtils.forceDelete(log_from);
                }

                if (out != null) {
                    FileUtils.forceDelete(out);
                }
            } catch (Exception e) {
            }

            isInit = false;
        }
    }//GEN-LAST:event_btnEval_Click

    private void btnBrowseOutputFile_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseOutputFile_Click
        txtOutputFile.setText(showFileDialog(txtOutputFile.getText(), true));
    }//GEN-LAST:event_btnBrowseOutputFile_Click

    private void spPattern_Changed(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spPattern_Changed
        StringBuilder patern = new StringBuilder("0.");
        int count = Integer.parseInt(spPattern.getModel().getValue().toString());
        for (int i = 0; i < count; i++) {
            patern.append("0");
        }
        txtPattern.setText(patern.toString());
    }//GEN-LAST:event_spPattern_Changed

    private void cboGroupBy_stateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboGroupBy_stateChanged
        setSelectSortCut();
    }//GEN-LAST:event_cboGroupBy_stateChanged

    private void chkSelectAll_stateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkSelectAll_stateChanged
        lstSelect.setEnabled(!chkSelectAll.isSelected());
        lstSort.setEnabled(!chkSelectAll.isSelected());
        lstCut.setEnabled(!chkSelectAll.isSelected());
    }//GEN-LAST:event_chkSelectAll_stateChanged

    private void lstSort_mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstSort_mouseClick
        int sel = lstSort.getSelectedIndex();
        int count = lstSelect.getSelectedIndices().length;
        boolean exist = false;
        for (int i = 0; i < count; i++) {
            if (sel == lstSelect.getSelectedIndices()[i]) {
                exist = true;
            }
        }

        if (exist == true) {
            DefaultListModel modelSort = (DefaultListModel) lstSort.getModel();
            DefaultListModel modelCut = (DefaultListModel) lstCut.getModel();
            String sort = lstSort.getSelectedValue().toString();
            switch (sort) {
                case " ":
                    modelSort.set(sel, "+");
                    break;
                case "+":
                    modelSort.set(sel, "-");
                    break;
                case "-":
                    modelSort.set(sel, " ");
                    break;
            }
            for (int i = 0; i < modelSort.getSize(); i++) {
                if (i != sel) {
                    modelSort.set(i, " ");
                    modelCut.set(i, " ");
                }
            }
        }
    }//GEN-LAST:event_lstSort_mouseClick

    private void lstCut_mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstCut_mouseClick
        int sel = lstCut.getSelectedIndex();
        int count = lstSelect.getSelectedIndices().length;
        boolean exist = false;
        for (int i = 0; i < count; i++) {
            if (sel == lstSelect.getSelectedIndices()[i]) {
                exist = true;
            }
        }

        DefaultListModel modelSort = (DefaultListModel) lstSort.getModel();
        String sort = modelSort.getElementAt(sel).toString();

        DefaultListModel modelCut = (DefaultListModel) lstCut.getModel();
        if (exist == true && !sort.equals(" ")) {
            String number = JOptionPane.showInputDialog("Enter number of item to cut.");
            try {
                modelCut.set(sel, Integer.parseInt(number));
            } catch (Exception ex) {
                modelCut.set(sel, " ");
            }
        }
    }//GEN-LAST:event_lstCut_mouseClick

    private void lstSelect_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstSelect_valueChanged
        DefaultListModel modelSelect = (DefaultListModel) lstSelect.getModel();
        DefaultListModel modelSort = (DefaultListModel) lstSort.getModel();
        DefaultListModel modelCut = (DefaultListModel) lstCut.getModel();
        StringBuilder inds = new StringBuilder();
        for (int i = 0; i < lstSelect.getSelectedIndices().length; i++) {
            inds.append("#").append(lstSelect.getSelectedIndices()[i]).append("#");
        }

        for (int i = 0; i < modelSelect.getSize(); i++) {
            if (inds.indexOf("#" + i + "#") == -1) {
                modelSort.setElementAt(" ", i);
                modelCut.setElementAt(" ", i);
            }
        }
    }//GEN-LAST:event_lstSelect_valueChanged

    private void spMinSentenceLength_stateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spMinSentenceLength_stateChanged
        int minVal = Integer.parseInt(spMinSentenceLength.getModel().getValue().toString());
        int curVal = Integer.parseInt(spMaxSentenceLength.getModel().getValue().toString());
        if (curVal < minVal) {
            curVal = minVal;
        }
        SpinnerNumberModel num_model = new SpinnerNumberModel(curVal, minVal, 1000, 1);
        spMaxSentenceLength.setModel(num_model);

    }//GEN-LAST:event_spMinSentenceLength_stateChanged

    private void chkMinSentenceLength_stateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkMinSentenceLength_stateChanged
        spMinSentenceLength.setEnabled(chkMinSentenceLength.isSelected());
    }//GEN-LAST:event_chkMinSentenceLength_stateChanged

    private void chkMaxSentenceLength_stateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chkMaxSentenceLength_stateChanged
        spMaxSentenceLength.setEnabled(chkMaxSentenceLength.isSelected());
    }//GEN-LAST:event_chkMaxSentenceLength_stateChanged

    private void btnRemoveAllExclude_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveAllExclude_Click
        DefaultListModel model = (DefaultListModel) lstExcludeList.getModel();
        model.removeAllElements();
    }//GEN-LAST:event_btnRemoveAllExclude_Click

    private void lblMore_mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMore_mouseClick
        lblMore.setVisible(false);
        lblLess.setVisible(true);
        pnlAdvancedParameter.setVisible(true);
    }//GEN-LAST:event_lblMore_mouseClick

    private void lblLess_mouseClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLess_mouseClick
        pnlAdvancedParameter.setVisible(false);
        lblLess.setVisible(false);
        lblMore.setVisible(true);
    }//GEN-LAST:event_lblLess_mouseClick

    private void pnlMore_mouseEnter(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMore_mouseEnter
        pnlMore.setBackground(new Color(235, 232, 232));
        pnlMore.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, false));
    }//GEN-LAST:event_pnlMore_mouseEnter

    private void pnlMore_mouseExit(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMore_mouseExit
        pnlMore.setBackground(new Color(240, 240, 240));
        pnlMore.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(240, 240, 240), 1, false));
    }//GEN-LAST:event_pnlMore_mouseExit

    private void btnAddExclude_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddExclude_Click
        DefaultListModel model = (DefaultListModel) lstExcludeList.getModel();
        model.addElement(cboExcludeType.getSelectedItem().toString() + "=" + txtExcludeVal.getText());
        txtExcludeVal.setText("");
    }//GEN-LAST:event_btnAddExclude_Click

    private void txtExcludeVal_keyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtExcludeVal_keyReleased
        if (evt.getKeyCode() == 10) {
            btnAddExclude.doClick();
        }
    }//GEN-LAST:event_txtExcludeVal_keyReleased

    private void lstExcludeList_valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstExcludeList_valueChanged
        if (lstExcludeList.getSelectedIndices().length == 0) {
            btnRemoveExclude.setEnabled(false);
        } else {
            btnRemoveExclude.setEnabled(true);
        }
    }//GEN-LAST:event_lstExcludeList_valueChanged

    private void btnRemoveExclude_Click(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveExclude_Click
        int count = lstExcludeList.getSelectedIndices().length;
        DefaultListModel model = (DefaultListModel) lstExcludeList.getModel();
        for (int i = count - 1; i >= 0; i--) {
            model.removeElementAt(lstExcludeList.getSelectedIndices()[i]);
        }
    }//GEN-LAST:event_btnRemoveExclude_Click
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddExclude;
    private javax.swing.JButton btnBrowseGoldFile;
    private javax.swing.JButton btnBrowseOutputFile;
    private javax.swing.JButton btnBrowseParseFile;
    private javax.swing.JButton btnEval;
    private javax.swing.JButton btnRemoveAllExclude;
    private javax.swing.JButton btnRemoveExclude;
    private javax.swing.JComboBox cboExcludeType;
    private javax.swing.JComboBox cboGroupBy;
    private javax.swing.JCheckBox chkColumnHeader;
    private javax.swing.JCheckBox chkConfusionMatrix;
    private javax.swing.JCheckBox chkDetails;
    private javax.swing.JCheckBox chkMaxSentenceLength;
    private javax.swing.JCheckBox chkMergeTables;
    private javax.swing.JCheckBox chkMinSentenceLength;
    private javax.swing.JCheckBox chkRowHeader;
    private javax.swing.JCheckBox chkSelectAll;
    private javax.swing.JCheckBox chkTabSeparate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblAlarm;
    private javax.swing.JLabel lblCut;
    private javax.swing.JLabel lblGoldFile;
    private javax.swing.JLabel lblLess;
    private javax.swing.JLabel lblMetric;
    private javax.swing.JLabel lblMore;
    private javax.swing.JLabel lblOutputFile;
    private javax.swing.JLabel lblParseFile;
    private javax.swing.JLabel lblPattern;
    private javax.swing.JLabel lblSelect;
    private javax.swing.JLabel lblSort;
    private javax.swing.JList lstCut;
    private javax.swing.JList lstExcludeList;
    private javax.swing.JList lstMetrics;
    private javax.swing.JList lstSelect;
    private javax.swing.JList lstSort;
    private javax.swing.JPanel pnlAdvancedParameter;
    private javax.swing.JPanel pnlEvaluationSettings;
    private javax.swing.JPanel pnlExclude;
    private javax.swing.JPanel pnlFormatSettings;
    private javax.swing.JPanel pnlGroupBy;
    private javax.swing.JPanel pnlMore;
    private javax.swing.JPanel pnlSentenceLength;
    private javax.swing.JPanel pnlSettings;
    private javax.swing.JSpinner spMaxSentenceLength;
    private javax.swing.JSpinner spMinSentenceLength;
    private javax.swing.JSpinner spPattern;
    private javax.swing.JTextArea txtEvalResults;
    private javax.swing.JTextField txtExcludeVal;
    private javax.swing.JTextField txtGoldFile;
    private javax.swing.JTextField txtOutputFile;
    private javax.swing.JTextField txtParseFile;
    private javax.swing.JTextField txtPattern;
    // End of variables declaration//GEN-END:variables
}