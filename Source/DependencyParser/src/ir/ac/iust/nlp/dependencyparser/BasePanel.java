package ir.ac.iust.nlp.dependencyparser;

import ir.ac.iust.nlp.dependencyparser.enumeration.EmphasizeSearchHits;
import ir.ac.iust.nlp.dependencyparser.enumeration.SearchBy;
import ir.ac.iust.nlp.dependencyparser.enumeration.SearchIn;
import ir.ac.iust.nlp.dependencyparser.utility.ExampleFileFilter;
import ir.ac.iust.nlp.dependencyparser.utility.FilePreviewer;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import se.vxu.msi.malteval.treeviewer.MaltTreeViewerGui;
import se.vxu.msi.malteval.treeviewer.gui.MaltTreeViewerMenu;
import se.vxu.msi.malteval.treeviewer.gui.MaltTreeViewerToolBar;
import se.vxu.msi.malteval.treeviewer.gui.NavigationPanel;

/**
 *
 * @author Mojtaba Khallash
 */
public abstract class BasePanel extends javax.swing.JPanel {

    protected String workingDir;
    public String getWorkingDirectory() {
        return workingDir;
    }
    
    protected MaltTreeViewerGui gui;
    
    private JComboBox search_in;
    private JComboBox search_strategy;
    private JComboBox search_for;
    private JComboBox sentence_to_search;
    private JButton btnSearch;
    protected JButton nextError;
    protected JButton prevError;
    // Emphasize Search Hits
    private JCheckBoxMenuItem for_words;
    private JCheckBoxMenuItem for_arcs;
    private JButton btnGoPrev;
    private JButton btnGoNext;
    private List<JMenuItem> btnSave;
    private HashMap<String, JRadioButtonMenuItem> image_types;

    /**
     * Creates new form BasePanel
     */
    public BasePanel(String dir) {
        this.workingDir = dir + File.separator + "tmp";
    }

    public abstract boolean canSave();

    public abstract void save();

    public abstract void saveAs();

    public static String showFileDialog(String currentDir, boolean isFolder) {
        return showFileDialog(currentDir, isFolder, null);
    }

    protected static String showFileDialog(String currentDir, boolean isFolder,
            FileNameExtensionFilter filter) {
        JFileChooser fc = new JFileChooser();
        if (currentDir.length() == 0) {
            fc.setCurrentDirectory(new java.io.File("."));
        } else {
            fc.setCurrentDirectory(new java.io.File(currentDir));
        }
        fc.setMultiSelectionEnabled(false);
        if (filter != null) {
            fc.setFileFilter(filter);
        }
        String title = "Select File";
        if (isFolder == true) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            title = "Select Folder";
        }

        if (fc.showDialog(null, title) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            String path = file.getPath();
            if (isFolder == true && path.lastIndexOf(File.separator) != path.length() - 1) {
                path = path + File.separator;
            }

            return path;
        } else {
            return currentDir;
        }
    }
    
    protected void saveText(ExampleFileFilter filter, String name, String body) {
        ExampleFileFilter[] ffs = new ExampleFileFilter[] {filter};

        JFileChooser fc = showFileDialogWithFilters(".", true, ffs);
        if (fc != null) {
            String output = fc.getSelectedFile().getAbsoluteFile() + File.separator + name;
            Writer writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(output, true), "UTF-8"));
                writer.write(body);
            } catch(Exception ex) {}
            finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }
    
    protected void saveTree(int index) {
        if (gui != null) {
            ExampleFileFilter pngFilter = new ExampleFileFilter("png", "Portable Network Graphics Image Files");
            ExampleFileFilter bmpFilter = new ExampleFileFilter("bmp", "Bitmap Image Files");
            ExampleFileFilter jpgFilter = new ExampleFileFilter("jpg", "JPEG Compressed Image Files");
            ExampleFileFilter gifFilter = new ExampleFileFilter("gif", "GIF Image Files");
    //        ExampleFileFilter wbmpFilter = new ExampleFileFilter("wbmp", "Wireless Bitmap Image Files");
            ExampleFileFilter[] ffs = new ExampleFileFilter[] {pngFilter, bmpFilter, jpgFilter, gifFilter};

            JFileChooser fc = showFileDialogWithFilters(".", true, ffs);
            if (fc != null) {
                String filter = ((ExampleFileFilter)fc.getFileFilter()).getFirstFilter();
                String name = saveImage(filter, index);
                File from = new File(System.getProperty("user.dir") + File.separator + name);
                File to = new File(fc.getSelectedFile().getAbsoluteFile() + File.separator + name);
                if (!from.equals(to)) {
                    try {
                        FileUtils.copyFile(from, to);
                    }
                    catch (Exception ex) {}
                    from.delete();

                }
            }
        }
    }

    protected JFileChooser showFileDialogWithFilters(String currentDir, boolean isFolder,
            ExampleFileFilter[] ffs) {
        JFileChooser fc = new JFileChooser();
                
        if (currentDir.length() == 0) {
            fc.setCurrentDirectory(new java.io.File("."));
        } else {
            fc.setCurrentDirectory(new java.io.File(currentDir));
        }
        fc.setMultiSelectionEnabled(false);
        if (ffs != null) {
            FilePreviewer previewer = new FilePreviewer(fc);
            fc.setAccessory(previewer);
         
            fc.setAcceptAllFileFilterUsed(false);
            for (int i = 0; i < ffs.length; i++) {
                fc.addChoosableFileFilter(ffs[i]);
            }
        }
        String title = "Select File";
        if (isFolder == true) {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            title = "Select Folder";
        }

        if (fc.showDialog(this, title) == JFileChooser.APPROVE_OPTION) {
//            File file = fc.getSelectedFile();
//            String path = file.getPath();
            return fc;
        } else {
            return null;
        }
    }

    protected void initTreeGui() {
        // Menu
        MaltTreeViewerMenu menu = new MaltTreeViewerMenu(gui);

        JMenu file_menu = (JMenu) menu.getComponent(0);
        JMenu export_menu = (JMenu) file_menu.getMenuComponent(0);
        JMenu cur_sentence_menu = (JMenu) export_menu.getMenuComponent(0);
        this.btnSave = new LinkedList<>();
        for (int i = 0; i < cur_sentence_menu.getItemCount(); i++) {
            this.btnSave.add((JMenuItem) cur_sentence_menu.getMenuComponent(i));
        }
        JMenu format_menu = (JMenu) export_menu.getMenuComponent(2);
        this.image_types = new HashMap<>();
        for (int i = 0; i < format_menu.getItemCount(); i++) {
            JRadioButtonMenuItem mnui = (JRadioButtonMenuItem) format_menu.getMenuComponent(i);
            image_types.put(mnui.getText(), mnui);
        }
        
        JMenu setting_menu = (JMenu) menu.getComponent(1);
        JMenu emphasize_search_hits = (JMenu) setting_menu.getMenuComponent(2);
        this.for_words = (JCheckBoxMenuItem) emphasize_search_hits.getMenuComponent(0);
        this.for_arcs = (JCheckBoxMenuItem) emphasize_search_hits.getMenuComponent(1);


        // Toolbar
        MaltTreeViewerToolBar toolbar = new MaltTreeViewerToolBar(gui);

        this.search_in = (JComboBox) toolbar.getComponentAtIndex(1);
        this.search_strategy = (JComboBox) toolbar.getComponentAtIndex(4);
        this.search_for = (JComboBox) toolbar.getComponentAtIndex(7);
        this.sentence_to_search = (JComboBox) toolbar.getComponent(12);
        this.btnSearch = (JButton) toolbar.getComponentAtIndex(9);


        // Navigation Panel
        NavigationPanel pnlNavigation = new NavigationPanel(gui);

        JPanel pnlButtons = (JPanel) pnlNavigation.getComponent(1);
        this.btnGoPrev = (JButton) pnlButtons.getComponent(1);
        this.btnGoNext = (JButton) pnlButtons.getComponent(2);
        this.prevError = (JButton)pnlButtons.getComponent(4);
        this.nextError = (JButton)pnlButtons.getComponent(5);
   }
    
    protected String saveImage(String imageType, int index) {
        String type = imageType.toLowerCase();
        JRadioButtonMenuItem mnui = this.image_types.get(type);
        if (mnui == null) {
            type = imageType.toUpperCase();
            mnui = this.image_types.get(type);
        }
        
        if (mnui != null) {
            mnui.doClick();
            this.btnSave.get(index).doClick();
            return this.btnSave.get(index).getText() + "_s" + (gui.getCurrentSentence() + 1) + "." + type;
        }
        else
            return null;
    }

    protected void goPreviousSentence() {
        this.btnGoPrev.doClick();
    }

    protected void goNextSentence() {
        this.btnGoNext.doClick();
    }

    protected void searchTree(int sentenceId, SearchIn searchIn,
            SearchBy searchBy, String searchFor, EmphasizeSearchHits emphasize) {

        boolean word_click, arc_click;
        switch (emphasize) {
            default:
            case None:
                word_click = (this.for_words.getModel().isSelected() == true);
                arc_click = (this.for_arcs.getModel().isSelected() == true);
                break;
            case ForWords:
                word_click = (this.for_words.getModel().isSelected() == false);
                arc_click = (this.for_arcs.getModel().isSelected() == true);
                break;
            case ForArcs:
                word_click = (this.for_words.getModel().isSelected() == true);
                arc_click = (this.for_arcs.getModel().isSelected() == false);
                break;
            case ForBoth:
                word_click = (this.for_words.getModel().isSelected() == false);
                arc_click = (this.for_arcs.getModel().isSelected() == false);
                break;
        }
        if (word_click == true) {
            this.for_words.doClick();
        }
        if (arc_click == true) {
            this.for_arcs.doClick();
        }

        this.search_in.setSelectedIndex(searchIn.ordinal());

        this.search_strategy.setSelectedIndex(searchBy.ordinal());

        this.search_for.getModel().setSelectedItem(searchFor);

        this.btnSearch.doClick();

        this.sentence_to_search.setSelectedIndex(sentenceId);
    }

    public static void initDrop(final JTextField text, final boolean isFolder) {
        FileDrop fd;
        fd = new FileDrop(null, text, new FileDrop.Listener() {

            @Override
            public void filesDropped(java.io.File[] files) {
                if (files.length > 0) {
                    try {
                        if (isFolder == false) {
                            boolean dropped = false;
                            for (int i = 0; i < files.length; i++) {
                                if (files[i].isFile()) {
                                    text.setText(files[i].getCanonicalPath());
                                    dropped = true;
                                    break;
                                }
                            }
                            if (dropped == false) {
                                JOptionPane.showMessageDialog(null, "File needed.");
                            }
                        } else {
                            if (files[0].isFile()) {
                                files[0] = files[0].getParentFile();
                            }

                            text.setText(files[0].getCanonicalPath() + File.separator);
                        }
                    } // end try
                    catch (java.io.IOException e) {
                    }
                }   // end for: through each dropped file
            }   // end filesDropped
        }); // end FileDrop.Listener
    }
    
    protected void initDrop(final JList list) {
        FileDrop fd;
        fd = new FileDrop(null, list, new FileDrop.Listener() {

            @Override
            public void filesDropped(java.io.File[] files) {
                if (files.length > 0) {
                    try {
                        DefaultListModel model = (DefaultListModel)list.getModel();
                        for( int i = 0; i < files.length; i++ ) {
                            if (files[i].isFile()) {
                                model.addElement(files[i].getCanonicalPath());
                            }
                        }
                    } // end try
                    catch (java.io.IOException e) {
                    }
                }   // end for: through each dropped file
            }   // end filesDropped
        }); // end FileDrop.Listener
    }
    
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
    
    public abstract void threadFinished();
    
    protected String getRam(boolean maximum) {
        OperatingSystemMXBean mxbean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunmxbean = 
                (com.sun.management.OperatingSystemMXBean) mxbean;
        double ram;
        if (maximum == true)
            ram = sunmxbean.getTotalPhysicalMemorySize();
        else
            ram = sunmxbean.getFreePhysicalMemorySize();
        ram = ram / 1024d;
        if (ram < 1024) {
            return (int)Math.round(ram) + "b";
        }
        ram = ram / 1024d;
        if (ram < 1024) {
            return (int)Math.round(ram) + "m";
        }
        ram = ram / 1024d;
        if (ram < 1024) {
            return (int)Math.round(ram) + "g";
        }
        
        return "";
    }
}