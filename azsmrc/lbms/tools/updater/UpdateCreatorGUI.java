package lbms.tools.updater;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;


import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JMenu;
import javax.swing.JMenuBar;


/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class UpdateCreatorGUI extends javax.swing.JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    {
        //Set Look & Feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel jPanel1;
    private JPanel detailsPanel;
    private JLabel label1;
    private JMenuItem exitMenuItem;
    private JButton remove;
    private JButton addFile;
    private JMenu jMenu3;
    private JMenuBar jMenuBar1;
    private JLabel size;
    private JLabel hash;
    private JTextField url;
    private JTextField version;
    private JTextField path;
    private JTextField name;
    private JComboBox TypeCombo;
    private JLabel TypeLabel;
    private JLabel SizeLabel;
    private JLabel HashLabel;
    private JLabel URLLabel;
    private JLabel VersionLabel;
    private JLabel PathLabel;
    private JLabel NameLabel;
    private JSeparator jSeparator1;
    private JMenuItem newPackage;

    private Update update;
    private UpdateCreator updateCreator;
    private JLabel UpdaterImportanceLabel;
    private JTextField updaterURL;
    private JLabel updaterURLLabel;
    private JLabel updaterLabel;
    private JPanel updaterOptionsPanel;
    private JPanel jPanel3;
    private JCheckBox UpdateCreatorCompressed;
    private JLabel UpdateCreatorDirLabel;
    private JButton savePackageFile;
    private JLabel UpdateCreatorName;
    private JPanel updateCreatorPanel;
    private JLabel UpdateCreatorNameLabel;
    private JButton create;
    private JPanel jPanel2;
    private JTabbedPane jTabbedPane1;
    private JButton chEnter;
    private JComboBox chType;
    private JTextArea chArea;
    private JLabel chLabel1;


    private UpdateFile currentUF;
    private JTextPane UpdateCreatorDir;
    private JComboBox UpdaterType;
    private JLabel UpdaterTypeLabel;
    private JComboBox UpdaterImportance;
    private DefaultComboBoxModel updateComboModel;

    private File currentDirFile;
    private File updateCreatorFile;
    private File currentDir;
    private Update currentUpdate;

    private Changelog log = new Changelog();
    private JButton jButton1;
    private JButton editSelected;
    private JButton addUpdate;
    private JComboBox updateCombo;
    private JScrollPane TreeScrollPane;
    private JTree fileTree;
    private DefaultMutableTreeNode fileTop;
    private JButton writeChangelog;
    private JScrollPane chTreeScrollPane;
    private JButton chRemove;
    private JTree chTree;
    private JCheckBox unpackFile;
    private JTextField updateVersion;
    private JLabel updateVersionLabel;
    private DefaultMutableTreeNode bugNode;
    private DefaultMutableTreeNode changeNode;
    private DefaultMutableTreeNode featureNode;
    private DefaultMutableTreeNode rootNode;
    private DefaultTreeModel treeModel;
    private DefaultTreeModel fileTreeModel;

    /**
     * Auto-generated main method to display this JFrame
     */
    public static void main(String[] args) {
        UpdateCreatorGUI inst = new UpdateCreatorGUI();
        inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inst.setVisible(true);
    }

    public UpdateCreatorGUI() {
        super();
        initGUI();
    }

    private void initGUI() {
        try {
            {
                {
                    jTabbedPane1 = new JTabbedPane();
                    getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
                    jTabbedPane1.setPreferredSize(new java.awt.Dimension(805, 424));
                    jTabbedPane1.setSize(805, 450);

                }

                {
                    jPanel2 = new JPanel();
                    GridBagLayout jPanel2Layout = new GridBagLayout();
                    jPanel2Layout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
                    jPanel2Layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7};
                    jPanel2Layout.columnWeights = new double[] {0.1, 0.1, 0.0, 0.1, 0.1};
                    jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
                    jPanel2.setLayout(jPanel2Layout);
                    jTabbedPane1.addTab("General", null, jPanel2, null);
                    {
                        create = new JButton();
                        jPanel2.add(create, new GridBagConstraints(3, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                        create.setText("Create Update");
                        create.setBounds(392, 301, 154, 28);
                        create.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                //First check to make sure a package file has been created
                                if(updateCreatorFile == null){
                                    JOptionPane.showMessageDialog(jPanel2, "Please select a package file to save.");
                                    return;
                                }

                                //check to make sure the UpdateCreator has some file
                                List<UpdateFile> files = updateCreator.getCurrentUpdate().getFileList();
                                if(files.size() == 0){
                                    JOptionPane.showMessageDialog(jPanel2, "Please add some files to this package.");
                                    return;
                                }

                                //Make sure that the changelog is what the user wants
                                if(log.getBugFixes().size() == 0 &&
                                        log.getChanges().size() == 0 &&
                                        log.getFeatures().size() == 0)
                                {
                                    JOptionPane.showMessageDialog(jPanel2, "Please add at least one changelog to this package.");
                                    return;
                                }

                                //Everthing looks ok..
                                //time to set all the values for the update and make the package

                                //-- URL
                                String urlText = updaterURL.getText();

                                if(urlText.endsWith("/"))
                                    urlText = urlText.substring(0, urlText.length()-1);

                                update.setUrl(urlText);

                                //-- Version
                                update.setVersion(new Version(updateVersion.getText()));

                                //-- Level
                                switch(UpdaterImportance.getSelectedIndex()){
                                case(0):
                                    update.setImportance_level(Update.LV_LOW);
                                break;

                                case(1):
                                    update.setImportance_level(Update.LV_FEATURE);
                                break;

                                case(2):
                                    update.setImportance_level(Update.LV_CHANGE);
                                break;

                                case(3):
                                    update.setImportance_level(Update.LV_BUGFIX);
                                break;

                                case(4):
                                    update.setImportance_level(Update.LV_SEC_RISK);
                                break;

                                default:
                                    update.setImportance_level(Update.LV_LOW);
                                break;
                                }

                                //-- Type
                                switch(UpdaterType.getSelectedIndex()){
                                case(0):
                                    update.setType(Update.TYPE_MAINTENANCE);
                                break;

                                case(1):
                                    update.setType(Update.TYPE_STABLE);
                                break;

                                case(2):
                                    update.setType(Update.TYPE_BETA);
                                break;

                                default:
                                    update.setType(Update.TYPE_MAINTENANCE);
                                break;
                                }

                                //--Changelog
                                update.setChangeLog(log);


                                //Do the master creation
                                try {
                                    if(!updateCreatorFile.exists()){
                                        updateCreatorFile.createNewFile();
                                    }
                                    if(updateCreator.construct(updateCreatorFile, updateCreatorFile.getName().endsWith(".gz")?true:false)){
                                        JOptionPane.showMessageDialog(jPanel2, "Package Creation successful.");
                                    }else{
                                        JOptionPane.showMessageDialog(jPanel2, "Update Creator reporting problems making package.");
                                    }
                                } catch (IOException e) {
                                    JOptionPane.showMessageDialog(jPanel2, "I/O Error writing the file!");
                                    e.printStackTrace();
                                }


                            }
                        });
                    }
                    {
                        updateCreatorPanel = new JPanel();
                        GridBagLayout updateCreatorPanelLayout = new GridBagLayout();
                        updateCreatorPanelLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
                        updateCreatorPanelLayout.rowHeights = new int[] {7, 7, 7, 7, 7};
                        updateCreatorPanelLayout.columnWeights = new double[] {0.1};
                        updateCreatorPanelLayout.columnWidths = new int[] {7};
                        updateCreatorPanel.setLayout(updateCreatorPanelLayout);
                        jPanel2.add(updateCreatorPanel, new GridBagConstraints(0, 1, 2, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                        updateCreatorPanel.setBounds(14, 49, 287, 231);
                        updateCreatorPanel.setBackground(new java.awt.Color(255,255,255));
                        updateCreatorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                        {
                            UpdateCreatorDir = new JTextPane();
                            updateCreatorPanel.add(UpdateCreatorDir, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            UpdateCreatorDir.setText("Not Selected Yet");
                            UpdateCreatorDir.setPreferredSize(new java.awt.Dimension(273, 70));
                            UpdateCreatorDir.setBounds(7, 119, 273, 70);
                        }
                        {
                            UpdateCreatorCompressed = new JCheckBox();
                            updateCreatorPanel.add(UpdateCreatorCompressed, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            UpdateCreatorCompressed.setText("Compress Update");
                            UpdateCreatorCompressed.setBackground(new java.awt.Color(255,255,255));
                            UpdateCreatorCompressed.setBounds(14, 182, 245, 49);
                            UpdateCreatorCompressed.addActionListener(new ActionListener(){

                                public void actionPerformed(ActionEvent arg0) {
                                    if(updateCreatorFile == null) return;
                                    if(UpdateCreatorCompressed.isSelected()){
                                        String name = updateCreatorFile.getPath();
                                        if(name.endsWith(".xml")){
                                            updateCreatorFile = new File(name + ".gz");
                                            UpdateCreatorName.setText(updateCreatorFile.getName());
                                            UpdateCreatorDir.setText(updateCreatorFile.getPath());
                                        }
                                    }else{
                                        String name = updateCreatorFile.getPath();
                                        if(updateCreatorFile.getPath().endsWith(".gz")){
                                            name = name.substring(0, name.length() - 3);
                                            if(!name.endsWith(".xml"))
                                                updateCreatorFile = new File(name + ".xml");
                                            else
                                                updateCreatorFile = new File(name);
                                            UpdateCreatorName.setText(updateCreatorFile.getName());
                                            UpdateCreatorDir.setText(updateCreatorFile.getPath());
                                        }
                                    }

                                }

                            });
                        }
                        {
                            UpdateCreatorDirLabel = new JLabel();
                            updateCreatorPanel.add(UpdateCreatorDirLabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                            UpdateCreatorDirLabel.setText("Update Save Location:");
                            UpdateCreatorDirLabel.setPreferredSize(new java.awt.Dimension(273, 28));
                            UpdateCreatorDirLabel.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
                            UpdateCreatorDirLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            UpdateCreatorDirLabel.setBounds(7, 84, 273, 28);
                        }
                        {
                            UpdateCreatorName = new JLabel();
                            updateCreatorPanel.add(UpdateCreatorName, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            UpdateCreatorName.setText("Not Selected Yet");
                            UpdateCreatorName.setPreferredSize(new java.awt.Dimension(273, 28));
                            UpdateCreatorName.setBounds(7, 49, 273, 28);
                        }

                        {
                            UpdateCreatorNameLabel = new JLabel();
                            updateCreatorPanel.add(UpdateCreatorNameLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                            UpdateCreatorNameLabel.setLayout(null);
                            UpdateCreatorNameLabel.setText("Update Name:");
                            UpdateCreatorNameLabel.setPreferredSize(new java.awt.Dimension(273, 28));
                            UpdateCreatorNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            UpdateCreatorNameLabel.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
                            UpdateCreatorNameLabel.setBounds(7, 21, 273, 28);
                        }
                    }
                    {
                        savePackageFile = new JButton();
                        jPanel2.add(savePackageFile, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                        savePackageFile.setText("Choose Update File");
                        savePackageFile.setBounds(73, 14, 161, 28);
                        savePackageFile.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                final JFileChooser fc = new JFileChooser();
                                fc.setName("Choose Main Package File Save Location");
                                if (currentDirFile != null)
                                    fc.setCurrentDirectory(currentDirFile);
                                else
                                    fc.setCurrentDirectory(new File("."));
                                int returnVal = fc.showSaveDialog(jPanel2);

                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    try {
                                        updateCreatorFile = fc.getSelectedFile();
                                        String smallName = updateCreatorFile.getName();
                                        if((!updateCreatorFile.getName().endsWith(".xml")) &&
                                                (!updateCreatorFile.getName().endsWith(".gz"))){
                                            String newFile = updateCreatorFile.getPath() + ".xml";
                                            updateCreatorFile = new File(newFile);
                                            UpdateCreatorCompressed.setSelected(false);
                                        }else if(updateCreatorFile.getName().endsWith(".gz")){
                                            UpdateCreatorCompressed.setSelected(true);
                                        }

                                        if(updateCreatorFile.exists()){

                                            updateCreator = new UpdateCreator(updateCreatorFile);
                                            update = updateCreator.getCurrentUpdate();

                                            //Fill out the update info
                                            if(update != null){
                                                switch(update.getType()){
                                                case(Update.TYPE_BETA):
                                                    UpdaterType.setSelectedIndex(2);
                                                break;

                                                case(Update.TYPE_STABLE):
                                                    UpdaterType.setSelectedIndex(1);
                                                break;

                                                case(Update.TYPE_MAINTENANCE):
                                                    UpdaterType.setSelectedIndex(1);
                                                break;
                                                }


                                                updaterURL.setText(update.getUrl());
                                                //"Low", "Feature","Change","BugFix","Security Risk"
                                                switch(update.getImportance_level()){
                                                case(Update.LV_LOW):
                                                    UpdaterImportance.setSelectedIndex(0);
                                                break;

                                                case(Update.LV_FEATURE):
                                                    UpdaterImportance.setSelectedIndex(1);
                                                break;

                                                case(Update.LV_CHANGE):
                                                    UpdaterImportance.setSelectedIndex(2);
                                                break;

                                                case(Update.LV_BUGFIX):
                                                    UpdaterImportance.setSelectedIndex(3);
                                                break;

                                                case(Update.LV_SEC_RISK):
                                                    UpdaterImportance.setSelectedIndex(4);
                                                }

                                                updateVersion.setText(update.getVersion().toString());
                                            }

                                            //Add in previous files if they exist
                                            if(update != null){
                                                List<UpdateFile> files = update.getFileList();
                                                for(int i = 0; i < files.size(); i++){
                                                    DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(files.get(i).getName());
                                                    fileTreeModel.insertNodeInto(nodeToAdd, fileTop, fileTop.getChildCount());
                                                }


                                                fileTree.scrollPathToVisible(new TreePath(fileTree.getPathForRow(fileTreeModel.getChildCount(fileTop))));

                                            }

                                            //pull in the changelog and populate 3rd tab
                                            log = update.getChangeLog();
                                            List<String> bugList = log.getBugFixes();
                                            List<String> changeList = log.getChanges();
                                            List<String> featureList = log.getFeatures();

                                            for (int i = 0; i < bugList.size(); i++){
                                                DefaultMutableTreeNode node = new DefaultMutableTreeNode(bugList.get(i));
                                                treeModel.insertNodeInto(node, bugNode, bugNode.getChildCount());
                                                chTree.scrollPathToVisible(new TreePath(node.getPath()));
                                            }

                                            for (int i = 0; i < changeList.size(); i++){
                                                DefaultMutableTreeNode node = new DefaultMutableTreeNode(changeList.get(i));
                                                treeModel.insertNodeInto(node, changeNode, changeNode.getChildCount());
                                                chTree.scrollPathToVisible(new TreePath(node.getPath()));
                                            }

                                            for (int i = 0; i < featureList.size(); i++){
                                                DefaultMutableTreeNode node = new DefaultMutableTreeNode(featureList.get(i));
                                                treeModel.insertNodeInto(node, featureNode, featureNode.getChildCount());
                                                chTree.scrollPathToVisible(new TreePath(node.getPath()));
                                            }

                                        }

                                        else{
                                            //pop up dialog to let the user name the first update in the file
                                            String s = (String)JOptionPane.showInputDialog(
                                                    jPanel2,
                                                    "Please give name of default update in the UpdateFile:\n",
                                                    "Customized Dialog",
                                                    JOptionPane.PLAIN_MESSAGE,
                                                    null,
                                                    null,
                                                    smallName);


                                            if ((s != null) && (s.length() > 0)) {
                                                System.out.println(s);
                                                updateComboModel.removeAllElements();
                                                updateComboModel.addElement(s);
                                                updateComboModel.setSelectedItem(s);

                                            }



                                        }


                                        UpdateCreatorName.setText(updateCreatorFile.getName());
                                        UpdateCreatorDir.setText(updateCreatorFile.getAbsolutePath());
                                        currentDir = new File(updateCreatorFile.getParent());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out
                                    .println("Save command cancelled by user.");
                                }
                            }
                        });
                    }
                    {
                        updaterOptionsPanel = new JPanel();
                        GridBagLayout updaterOptionsPanelLayout = new GridBagLayout();
                        updaterOptionsPanelLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
                        updaterOptionsPanelLayout.rowHeights = new int[] {7, 7, 7, 7, 7};
                        updaterOptionsPanelLayout.columnWeights = new double[] {0.1, 0.1};
                        updaterOptionsPanelLayout.columnWidths = new int[] {7, 7};
                        updaterOptionsPanel.setLayout(updaterOptionsPanelLayout);
                        jPanel2.add(updaterOptionsPanel, new GridBagConstraints(3, 1, 2, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                        updaterOptionsPanel.setBounds(308, 49, 322, 231);
                        updaterOptionsPanel.setBackground(new java.awt.Color(225,225,225));
                        updaterOptionsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                        {
                            ComboBoxModel UpdaterTypeModel = new DefaultComboBoxModel(
                                    new String[] { "Maintenance", "Stable","Beta" });
                            UpdaterType = new JComboBox();
                            updaterOptionsPanel.add(UpdaterType, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            UpdaterType.setModel(UpdaterTypeModel);
                            UpdaterType.setBounds(126, 189, 189, 28);
                        }
                        {
                            UpdaterTypeLabel = new JLabel();
                            updaterOptionsPanel.add(UpdaterTypeLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            UpdaterTypeLabel.setText("Update Type:");
                            UpdaterTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            UpdaterTypeLabel.setBounds(7, 182, 105, 35);
                        }
                        {
                            UpdaterImportanceLabel = new JLabel();
                            updaterOptionsPanel.add(UpdaterImportanceLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            UpdaterImportanceLabel.setText("Update Level:");
                            UpdaterImportanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            UpdaterImportanceLabel.setBounds(7, 133, 105, 35);
                        }
                        {
                            updaterLabel = new JLabel();
                            updaterOptionsPanel.add(updaterLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                            updaterLabel.setText("Update Options");
                            updaterLabel.setPreferredSize(new java.awt.Dimension(288, 14));
                            updaterLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            updaterLabel.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
                            updaterLabel.setBounds(17, 12, 288, 23);
                        }
                        {
                            updaterURLLabel = new JLabel();
                            updaterOptionsPanel.add(updaterURLLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            updaterURLLabel.setText("Update URL:");
                            updaterURLLabel.setPreferredSize(new java.awt.Dimension(98, 35));
                            updaterURLLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            updaterURLLabel.setBounds(7, 49, 98, 35);
                        }
                        {
                            updaterURL = new JTextField();
                            updaterOptionsPanel.add(updaterURL, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            updaterURL.setPreferredSize(new java.awt.Dimension(203, 21));
                            updaterURL.setBounds(112, 56, 203, 21);
                            updaterURL.addFocusListener(new FocusListener(){

                                public void focusGained(FocusEvent arg0) { }

                                public void focusLost(FocusEvent arg0) {
                                    String text = updaterURL.getText();
                                    if(text.endsWith("/")){
                                        text = text.substring(0,text.length()-1);
                                        update.setUrl(text);
                                        updaterURL.setText(text);
                                    }

                                }

                            });
                        }
                        {
                            ComboBoxModel UpdaterImportanceModel = new DefaultComboBoxModel(
                                    new String[] { "Low", "Feature","Change","BugFix","Security Risk" });
                            UpdaterImportance = new JComboBox();
                            updaterOptionsPanel.add(UpdaterImportance, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            UpdaterImportance.setModel(UpdaterImportanceModel);
                            UpdaterImportance.setBounds(126, 133, 189, 28);
                        }
                        {
                            updateVersionLabel = new JLabel();
                            updaterOptionsPanel.add(updateVersionLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            updateVersionLabel.setText("Update Version:");
                            updateVersionLabel.setBounds(7, 91, 98, 28);
                            updateVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            updateVersion = new JTextField();
                            updaterOptionsPanel.add(updateVersion, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            updateVersion.setBounds(112, 94, 203, 21);
                            updateVersion.setPreferredSize(new java.awt.Dimension(4, 21));
                        }




                    }
                    //START >>  updateCombo
                    updateComboModel = new DefaultComboBoxModel();
                    updateComboModel.addElement("Not Selected Yet");
                    /*ComboBoxModel updateComboModel = new DefaultComboBoxModel(
                            new String[] { "Item One", "Item Two" });
                    */
                    updateCombo = new JComboBox();
                    jPanel2.add(updateCombo, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    updateCombo.setModel(updateComboModel);
                    //END <<  updateCombo
                    //START >>  addUpdate
                    addUpdate = new JButton();
                    jPanel2.add(addUpdate, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    addUpdate.setText("Add New Update");
                    addUpdate.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            //TODO fix!
                        }
                    });
                    //END <<  addUpdate
                }

                this.setTitle("UpdateCreatorGUI");
                {
                    jPanel1 = new JPanel();
                    GridBagLayout jPanel1Layout = new GridBagLayout();
                    jPanel1Layout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
                    jPanel1Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
                    jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.1, 0.1};
                    jPanel1Layout.columnWidths = new int[] {7, 7, 7, 7, 7};

                    jTabbedPane1.addTab("Files", null, jPanel1, null);
                    jPanel1.setLayout(jPanel1Layout);
                    jPanel1.setPreferredSize(new java.awt.Dimension(644, 415));
                    {
                        detailsPanel = new JPanel();
                        GridBagLayout detailsPanelLayout = new GridBagLayout();
                        detailsPanelLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
                        detailsPanelLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7, 7};
                        detailsPanelLayout.columnWeights = new double[] {0.1, 0.1, 0.1};
                        detailsPanelLayout.columnWidths = new int[] {7, 7, 7};
                        detailsPanel.setLayout(detailsPanelLayout);
                        jPanel1.add(detailsPanel, new GridBagConstraints(2, 1, 3, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                        detailsPanel.setBorder(BorderFactory
                                .createBevelBorder(BevelBorder.LOWERED));
                        detailsPanel.setBounds(245, 42, 399, 273);
                        {
                            label1 = new JLabel();
                            detailsPanel.add(label1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            label1.setText("UpdateFile Details:");
                            label1.setBounds(7, 7, 385, 21);
                            label1.setBorder(new LineBorder(new java.awt.Color(
                                    0,
                                    0,
                                    0), 1, false));
                            label1
                            .setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            NameLabel = new JLabel();
                            detailsPanel.add(NameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            NameLabel.setText("Name:");
                            NameLabel.setBounds(7, 28, 63, 28);
                            NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            PathLabel = new JLabel();
                            detailsPanel.add(PathLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            PathLabel.setText("Path:");
                            PathLabel.setBounds(7, 53, 63, 28);
                            PathLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            VersionLabel = new JLabel();
                            detailsPanel.add(VersionLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            VersionLabel.setText("Version:");
                            VersionLabel.setBounds(7, 79, 63, 28);
                            VersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            URLLabel = new JLabel();
                            detailsPanel.add(URLLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            URLLabel.setText("URL:");
                            URLLabel.setBounds(7, 105, 63, 28);
                            URLLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            HashLabel = new JLabel();
                            detailsPanel.add(HashLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            HashLabel.setText("Hash:");
                            HashLabel.setBounds(7, 135, 63, 28);
                            HashLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            SizeLabel = new JLabel();
                            detailsPanel.add(SizeLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            SizeLabel.setText("Size:");
                            SizeLabel.setBounds(7, 205, 63, 28);
                            SizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            TypeLabel = new JLabel();
                            detailsPanel.add(TypeLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            TypeLabel.setText("Type:");
                            TypeLabel.setBounds(7, 168, 63, 28);
                            TypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        {
                            ComboBoxModel TypeComboModel = new DefaultComboBoxModel(
                                    new String[] { "Normal", "SourceForge" });
                            TypeCombo = new JComboBox();
                            detailsPanel.add(TypeCombo, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            TypeCombo.setModel(TypeComboModel);
                            TypeCombo.setBounds(63, 172, 196, 21);
                            TypeCombo.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    if (currentUF != null) {
                                        if (TypeCombo.getSelectedIndex() + 1 != currentUF
                                                .getType()) {
                                            currentUF.setType(TypeCombo
                                                    .getSelectedIndex() + 1);
                                        }
                                    }

                                }

                            });
                        }
                        {
                            name = new JTextField();
                            detailsPanel.add(name, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            name.setBounds(63, 35, 329, 21);
                            name.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getName()
                                            .equalsIgnoreCase(name.getText())) {

                                        currentUF.setName(name.getText());
                                        TreePath tPath = fileTree.getSelectionPath();
                                        fileTreeModel.valueForPathChanged(tPath,name.getText());
                                    }

                                }

                            });

                            name.addFocusListener(new FocusListener() {

                                public void focusGained(FocusEvent arg0) {
                                }

                                public void focusLost(FocusEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getName()
                                            .equalsIgnoreCase(name.getText())) {

                                        currentUF.setName(name.getText());
                                        TreePath tPath = fileTree.getSelectionPath();
                                        fileTreeModel.valueForPathChanged(tPath, name.getText());
                                    }

                                }

                            });
                        }
                        {
                            path = new JTextField();
                            detailsPanel.add(path, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            path.setBounds(63, 58, 329, 21);
                            path.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getPath()
                                            .equalsIgnoreCase(path.getText())) {

                                        String pathText = path.getText();
                                        if(!pathText.equalsIgnoreCase("") && !pathText.endsWith("/"))
                                            pathText += "/";

                                        currentUF.setPath(pathText);
                                        path.setText(pathText);
                                    }

                                }

                            });

                            path.addFocusListener(new FocusListener() {

                                public void focusGained(FocusEvent arg0) {
                                }

                                public void focusLost(FocusEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getPath()
                                            .equalsIgnoreCase(path.getText())) {

                                        String pathText = path.getText();
                                        if(!pathText.equalsIgnoreCase("") && !pathText.endsWith("/"))
                                            pathText += "/";

                                        currentUF.setPath(pathText);
                                        path.setText(pathText);
                                    }

                                }

                            });
                        }
                        {
                            version = new JTextField();
                            detailsPanel.add(version, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            version.setBounds(63, 81, 329, 21);
                            version.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF
                                            .getVersion()
                                            .toString()
                                            .equalsIgnoreCase(version.getText())) {

                                        try {
                                            currentUF.setVersion(new Version(
                                                    version.getText()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            version.setText("Invalid!");
                                        }

                                    }

                                }

                            });

                            version.addFocusListener(new FocusListener() {

                                public void focusGained(FocusEvent arg0) {
                                }

                                public void focusLost(FocusEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF
                                            .getVersion()
                                            .toString()
                                            .equalsIgnoreCase(version.getText())) {

                                        try {
                                            currentUF.setVersion(new Version(
                                                    version.getText()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            version.setText("Invalid!");
                                        }
                                    }

                                }

                            });
                        }
                        {
                            url = new JTextField();
                            detailsPanel.add(url, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                            url.setBounds(63, 105, 329, 21);
                            url.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getUrl()
                                            .equalsIgnoreCase(url.getText())) {

                                        currentUF.setUrl(url.getText());
                                    }

                                }

                            });

                            url.addFocusListener(new FocusListener() {

                                public void focusGained(FocusEvent arg0) {
                                }

                                public void focusLost(FocusEvent arg0) {
                                    if (currentUF != null
                                            && !currentUF.getUrl()
                                            .equalsIgnoreCase(url.getText())) {

                                        currentUF.setUrl(url.getText());
                                    }

                                }

                            });
                        }
                        {
                            hash = new JLabel();
                            detailsPanel.add(hash, new GridBagConstraints(1, 5, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            hash.setBounds(63, 138, 329, 21);
                        }
                        {
                            size = new JLabel();
                            detailsPanel.add(size, new GridBagConstraints(1, 7, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                            size.setBounds(63, 208, 329, 21);
                        }
                        //START >>  unpackFile
                        unpackFile = new JCheckBox();
                        detailsPanel.add(unpackFile, new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                        unpackFile.setText("Unpack this file");
                        unpackFile.setBounds(7, 238, 385, 28);
                        unpackFile.setEnabled(false);
                        unpackFile.setHorizontalAlignment(SwingConstants.CENTER);
                        unpackFile.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent arg0) {
                                currentUF.setArchive(unpackFile.isSelected());
                            }
                        });
                        //END <<  unpackFile
                    }

                    {
                        addFile = new JButton();
                        jPanel1.add(addFile, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                        addFile.setText("Add File");
                        addFile.setBounds(7, 7, 105, 28);
                        addFile.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                final JFileChooser fc = new JFileChooser();
                                fc.setName("Add File to Update Package");
                                if (currentDir != null)
                                    fc.setCurrentDirectory(currentDir);
                                else
                                    fc.setCurrentDirectory(new File("."));
                                int returnVal = fc.showOpenDialog(jPanel1);

                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    currentDirFile = fc.getSelectedFile();
                                    currentDir = new File(currentDirFile.getParent());
                                    try {
                                        updateCreator.addFile(
                                                currentDirFile,
                                                "",
                                                "",
                                                1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //add to the tree
                                    DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(currentDirFile.getName());
                                    fileTreeModel.insertNodeInto(nodeToAdd, fileTop, fileTop.getChildCount());
                                    fileTree.scrollPathToVisible(new TreePath(nodeToAdd.getPath()));


                                    remove.setEnabled(true);
                                } else {
                                    System.out
                                    .println("Open command cancelled by user.");
                                }
                            }
                        });
                    }
                    {
                        remove = new JButton();
                        jPanel1.add(remove, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                        remove.setText("Remove Selected");
                        remove.setEnabled(false);
                        remove.setBounds(119, 7, 140, 28);
                        remove.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                TreePath[] tPaths = fileTree.getSelectionPaths();
                                for(TreePath tPath:tPaths){
                                    String treeName = tPath.getLastPathComponent().toString();
                                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileTree.getSelectionPath().getLastPathComponent();
                                    List<UpdateFile> ufList = update.getFileList();
                                    Iterator iterator = ufList.iterator();
                                    while (iterator.hasNext()) {
                                        UpdateFile uf = (UpdateFile) iterator
                                        .next();
                                        if (uf.getName().equalsIgnoreCase(treeName)){
                                            fileTreeModel.removeNodeFromParent(node);
                                            updateCreator.removeFile(uf);
                                            name.setText("");
                                            path.setText("");
                                            version.setText("");
                                            url.setText("");
                                            hash.setText("");
                                            size.setText("");
                                            TypeCombo.setSelectedIndex(0);
                                            break;
                                        }
                                    }
                                }


                                /*    int index = fileList.getSelectedIndex();
								List<UpdateFile> ufList = update.getFileList();
								Iterator iterator = ufList.iterator();
								while (iterator.hasNext()) {
									UpdateFile uf = (UpdateFile) iterator
										.next();
									if (uf.getName().equalsIgnoreCase(
										(String) fileListModel
											.getElementAt(index))) {
										fileListModel.remove(index);
										updateCreator.removeFile(uf);
										name.setText("");
										path.setText("");
										version.setText("");
										url.setText("");
										hash.setText("");
										size.setText("");
										TypeCombo.setSelectedIndex(0);
										break;
									}
								}*/
                                int size = fileTreeModel.getChildCount(fileTop);
                                if (size == 0) { //Nobody's left, disable firing.
                                    remove.setEnabled(false);

                                } else { //Select an index.


                                    /*              if (index == fileListModel.getSize()) {
										//removed item in last position
										index--;
									}

									fileList.setSelectedIndex(index);
									fileList.ensureIndexIsVisible(index);*/
                                }
                            }
                        });
                    }
                    //START >>  TreeScrollPane
                    TreeScrollPane = new JScrollPane();
                    jPanel1.add(TreeScrollPane, new GridBagConstraints(0, 1, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                    //START >>  fileTree
                    fileTop = new DefaultMutableTreeNode("Files");
                    fileTreeModel = new DefaultTreeModel(fileTop);
                    fileTree = new JTree(fileTreeModel);
                    GridBagLayout fileTreeLayout = new GridBagLayout();
                    fileTreeLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
                    fileTreeLayout.rowHeights = new int[] {7, 7, 7, 7};
                    fileTreeLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1};
                    fileTreeLayout.columnWidths = new int[] {7, 7, 7, 7};
                    fileTree.setLayout(fileTreeLayout);
                    TreeScrollPane.setViewportView(fileTree);
                    fileTree.addTreeSelectionListener(new TreeSelectionListener() {
                        public void valueChanged(TreeSelectionEvent evt) {
                            //if(evt.getPaths().length > 1) return;
                            List<UpdateFile> ufList = update.getFileList();
                            Iterator iterator = ufList.iterator();

                            while (iterator.hasNext()) {
                                UpdateFile uf = (UpdateFile) iterator
                                .next();
                                if (uf.getName().equalsIgnoreCase(
                                        evt.getPath().getLastPathComponent().toString())) {
                                    currentUF = uf;
                                    break;
                                }
                            }
                            if (currentUF != null) {
                                name.setText(currentUF.getName());
                                path.setText(currentUF.getPath());
                                version.setText(currentUF.getVersion()
                                        .toString());
                                url.setText(currentUF.getUrl());
                                hash.setText(currentUF.getHash());
                                if (currentUF.getType() == 1) {
                                    TypeCombo.setSelectedIndex(0);
                                } else
                                    TypeCombo.setSelectedIndex(1);

                                size.setText(String.valueOf(currentUF
                                        .getSize()));
                                if(currentUF.getName().endsWith(".zip") || currentUF.getName().endsWith(".gz")){
                                    unpackFile.setEnabled(true);
                                    unpackFile.setSelected(currentUF.isArchive());
                                }else{
                                    unpackFile.setSelected(false);
                                    unpackFile.setEnabled(false);
                                }

                            }
                        }
                    });

                    //END <<  fileTree
                    //END <<  TreeScrollPane

                }
                {
                    jPanel3 = new JPanel();
                    GridBagLayout jPanel3Layout = new GridBagLayout();
                    jPanel3Layout.rowWeights = new double[] {0.1, 0.5, 0.1, 1.0, 0.1};
                    jPanel3Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
                    jPanel3Layout.columnWeights = new double[] {0.1, 0.1, 0.1};
                    jPanel3Layout.columnWidths = new int[] {7, 20, 7};
                    jTabbedPane1.addTab("Changelog", null, jPanel3, null);
                    jPanel3.setLayout(jPanel3Layout);
                    {
                        chLabel1 = new JLabel();
                        jPanel3.add(chLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                        chLabel1.setText("Enter Changelog Text then choose type then hit \"Enter\"");
                        chLabel1.setBounds(35, 7, 483, 28);
                    }
                    {
                        chArea = new JTextArea();
                        jPanel3.add(chArea, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 20), 0, 0));
                        chArea.setText("");
                        chArea.setBounds(35, 42, 574, 56);
                        chArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    }
                    {
                        ComboBoxModel chTypeModel = new DefaultComboBoxModel(
                                new String[] { "Bug Fix", "Change", "Feature" });
                        chType = new JComboBox();
                        jPanel3.add(chType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 0), 0, 0));
                        chType.setModel(chTypeModel);
                        chType.setBounds(42, 105, 245, 28);
                    }
                    {
                        chEnter = new JButton();
                        jPanel3.add(chEnter, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 0, 0));
                        chEnter.setText("Enter");
                        chEnter.setBounds(483, 105, 119, 28);
                        chEnter.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent arg0) {
                                if(chArea.getText().equalsIgnoreCase("")){
                                    JOptionPane.showMessageDialog(jPanel3, "Please enter valid text.");
                                    return;
                                }

                                switch(chType.getSelectedIndex()){
                                case(0):
                                    log.addBugFix(chArea.getText());
                                DefaultMutableTreeNode bugChildNode = new DefaultMutableTreeNode(chArea.getText());
                                treeModel.insertNodeInto(bugChildNode, bugNode, bugNode.getChildCount());
                                chTree.scrollPathToVisible(new TreePath(bugChildNode.getPath()));
                                chArea.setText("");
                                break;

                                case(1):
                                    log.addChange(chArea.getText());
                                DefaultMutableTreeNode changeChileNode = new DefaultMutableTreeNode(chArea.getText());
                                treeModel.insertNodeInto(changeChileNode, changeNode, changeNode.getChildCount());
                                chTree.scrollPathToVisible(new TreePath(changeChileNode.getPath()));
                                chArea.setText("");
                                break;

                                case(2):
                                    log.addFeature(chArea.getText());
                                DefaultMutableTreeNode featureChileNode = new DefaultMutableTreeNode(chArea.getText());
                                treeModel.insertNodeInto(featureChileNode, featureNode, featureNode.getChildCount());
                                chTree.scrollPathToVisible(new TreePath(featureChileNode.getPath()));
                                chArea.setText("");
                                break;
                                }

                            }

                        });
                    }
                    //START >>  chRemove
                    chRemove = new JButton();
                    jPanel3.add(chRemove, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    chRemove.setText("Remove Selected");
                    chRemove.setBounds(35, 312, 147, 28);
                    chRemove.addActionListener(new ActionListener(){

                        public void actionPerformed(ActionEvent arg0) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)chTree.getSelectionPath().getLastPathComponent();
                            if(node.isRoot()) return;

                            if(node.getParent().equals(bugNode)){
                                log.removeBugFix(node.toString());
                                treeModel.removeNodeFromParent(node);
                            }else if(node.getParent().equals(changeNode)){
                                log.removeChange(node.toString());
                                treeModel.removeNodeFromParent(node);
                            }else if(node.getParent().equals(featureNode)){
                                log.removeFeature(node.toString());
                                treeModel.removeNodeFromParent(node);
                            }


                        }

                    });


                    //END <<  chRemove
                    //START >>  chTreeScrollPane
                    chTreeScrollPane = new JScrollPane();
                    jPanel3.add(chTreeScrollPane, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                    chTreeScrollPane.setBounds(7, 140, 630, 168);
                    //START >>  chTree
                    bugNode = new DefaultMutableTreeNode("BugFixes");
                    changeNode = new DefaultMutableTreeNode("Changes");
                    featureNode = new DefaultMutableTreeNode("Features");
                    rootNode = new DefaultMutableTreeNode("Changelog");
                    rootNode.add(bugNode);
                    rootNode.add(changeNode);
                    rootNode.add(featureNode);

                    treeModel = new DefaultTreeModel(rootNode);

                    chTree = new JTree(treeModel);
                    chTreeScrollPane.setViewportView(chTree);
                    chTree.setBounds(14, 133, 616, 210);
                    chTree.setBorder(BorderFactory
                            .createBevelBorder(BevelBorder.LOWERED));
                    chTree.setPreferredSize(new java.awt.Dimension(612, 309));
                    //END <<  chTree
                    //END <<  chTreeScrollPane
                    //START >>  writeChangelog
                    writeChangelog = new JButton();
                    jPanel3.add(writeChangelog, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    writeChangelog.setText("Write Changelog to File");
                    writeChangelog.setBounds(399, 313, 210, 28);
                    writeChangelog.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent arg0) {
                            final JFileChooser fc = new JFileChooser();
                            fc.setName("Choose Changelog File Save Location");
                            if (currentDirFile != null)
                                fc.setCurrentDirectory(currentDirFile);
                            else
                                fc.setCurrentDirectory(new File("."));
                            int returnVal = fc.showSaveDialog(jPanel2);

                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                File changelogFile = fc.getSelectedFile();

                                try {
                                    System.out.println(updateCreator.toString());
                                    if(updateCreator == null){
                                        JOptionPane.showMessageDialog(jPanel2,
                                                "No Update File selcted yet.  Please go to the first tab and select a file before adding Changelogs to it.",
                                                "Inane warning",
                                                JOptionPane.WARNING_MESSAGE);
                                    }else
                                        updateCreator.generateChangelogTxt(changelogFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }




                            }

                        }

                    });
                    //END <<  writeChangelog
                    //START >>  editSelected
                    editSelected = new JButton();
                    jPanel3.add(editSelected, new GridBagConstraints(
                        1,
                        3,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0,
                        0));
                    editSelected.setText("Edit Selected");
                    //END <<  editSelected
                    //START >>  jButton1
                    jButton1 = new JButton();
                    jPanel3.add(jButton1, new GridBagConstraints(
                        1,
                        4,
                        1,
                        1,
                        0.0,
                        0.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0),
                        0,
                        0));
                    jButton1.setText("Edit Selected");
                    jButton1.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)chTree.getSelectionPath().getLastPathComponent();
                            if(node.isRoot()) return;

                            String s = (String)JOptionPane.showInputDialog(
                                    jPanel3,
                                    "Edit Changlog Message",
                                    "Customized Dialog",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    node.toString());


                            if ((s != null) && (s.length() > 0)) {
                                int location = bugNode.getIndex(node);

                                if(node.getParent().equals(bugNode)){
                                    log.removeBugFix(node.toString());
                                    log.addBugFix(s);
                                    treeModel.removeNodeFromParent(node);
                                    DefaultMutableTreeNode bugChildNode = new DefaultMutableTreeNode(s);
                                    treeModel.insertNodeInto(bugChildNode, bugNode, location);
                                    chTree.scrollPathToVisible(new TreePath(bugChildNode.getPath()));
                                }else if(node.getParent().equals(changeNode)){
                                    log.removeChange(node.toString());
                                    treeModel.removeNodeFromParent(node);
                                }else if(node.getParent().equals(featureNode)){
                                    log.removeFeature(node.toString());
                                    treeModel.removeNodeFromParent(node);
                                }
                            }


                        }
                    });
                    //END <<  jButton1

                }

            }
            updateCreator = new UpdateCreator();
            update = updateCreator.getCurrentUpdate();
            this.setSize(813, 475);
            {
                jMenuBar1 = new JMenuBar();
                setJMenuBar(jMenuBar1);
                {
                    jMenu3 = new JMenu();
                    jMenuBar1.add(jMenu3);
                    jMenu3.setText("File");


                    {
                        newPackage = new JMenuItem();
                        jMenu3.add(newPackage);
                        newPackage.setText("New Package");
                        newPackage.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                clearAll();

                            }
                        });
                        {
                            jSeparator1 = new JSeparator();
                            jMenu3.add(jSeparator1);
                        }

                        {
                            exitMenuItem = new JMenuItem();
                            jMenu3.add(exitMenuItem);
                            exitMenuItem.setText("Exit");
                            exitMenuItem.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    System.exit(0);
                                }
                            });
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAll(){
        //Remake the elements
        updateCreator = new UpdateCreator();
        update = updateCreator.getCurrentUpdate();
        log = new Changelog();
        currentDirFile = null;
        updateCreatorFile = null;

        //Clear 1st tab
        UpdateCreatorName.setText("Not Selected Yet");
        UpdateCreatorDir.setText("Not Selected Yet");
        UpdateCreatorCompressed.setSelected(false);

        UpdaterImportance.setSelectedIndex(0);
        UpdaterType.setSelectedIndex(0);
        updaterURL.setText("");
        updateVersion.setText("");

        //Clear 2nd tab
        currentUF = null;
        fileTop = new DefaultMutableTreeNode("Files");
        fileTreeModel = new DefaultTreeModel(fileTop);
        fileTree.setModel(fileTreeModel);
        name.setText("");
        path.setText("");
        version.setText("");
        url.setText("");
        hash.setText("");
        TypeCombo.setSelectedIndex(0);
        size.setText("");

        //Clear 3rd Tab
        chArea.setText("");
        bugNode = new DefaultMutableTreeNode("BugFixes");
        changeNode = new DefaultMutableTreeNode("Changes");
        featureNode = new DefaultMutableTreeNode("Features");
        rootNode = new DefaultMutableTreeNode("Changelog");
        rootNode.add(bugNode);
        rootNode.add(changeNode);
        rootNode.add(featureNode);
        treeModel = new DefaultTreeModel(rootNode);
        chTree.setModel(treeModel);
    }

}
