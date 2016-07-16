package lbms.tools.updater;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.swing.tree.TreeSelectionModel;
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
    private boolean bCurrentUFisArch;
    private JTextPane UpdateCreatorDir;
    private JComboBox UpdaterType;
    private JLabel UpdaterTypeLabel;
    private JComboBox UpdaterImportance;
    private DefaultComboBoxModel updateComboModel;

    private File currentDirFile;
    private File updateCreatorFile;
    private File currentDir;


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

    HashMap<String,File> map = new HashMap<String,File>();
    HashMap<String,File> totalMap = new HashMap<String,File>();

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
                    JPanel JpanelMain = new JPanel();
                    getContentPane().add(JpanelMain, BorderLayout.CENTER);
                    JpanelMain.setPreferredSize(new java.awt.Dimension(805, 500));
                    JpanelMain.setSize(805, 450);
					//START >>  updateCombo
					updateComboModel = new DefaultComboBoxModel();
					updateComboModel.addElement("Not Selected Yet");

					updateCombo = new JComboBox();
					JpanelMain.add(updateCombo);
					updateCombo.setModel(updateComboModel);
					updateCombo.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent arg0) {
							if (updateComboModel.getSize() <= 1)
								return;
							Version tempVer = (Version) updateComboModel
								.getSelectedItem();
							if (tempVer == null)
								return;
							Iterator it = updateCreator.getUpdateList()
								.getUpdateSet().iterator();
							while (it.hasNext()) {
								Update nextUpdate = (Update) it.next();
								if (nextUpdate.getVersion().compareTo(tempVer) == 0) {
									updateCreator.setCurrentUpdate(nextUpdate);
									update = nextUpdate;
									loadUpdate(update);
									return;
								}
							}
						}
					});
					//END <<  updateCombo


                	jTabbedPane1 = new JTabbedPane();
                	JpanelMain.add(jTabbedPane1);
                    //getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
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

                                //check to make sure the UpdateCreator has a file

                                Iterator it = updateCreator.getUpdateList().getUpdateSet().iterator();
                                while(it.hasNext()){
                                    List<UpdateFile> files = ((Update)it.next()).getFileList();
                                    if(files.size() == 0){
                                        JOptionPane.showMessageDialog(jPanel2, "Please add files to each update in this package.");
                                        return;
                                    }
                                }


                                //Make sure that the changelog is what the user wants
                                it = updateCreator.getUpdateList().getUpdateSet().iterator();
                                while(it.hasNext()){
                                    Changelog tempLog = ((Update)it.next()).getChangeLog();
                                    if(tempLog.getBugFixes().size() == 0 &&
                                            tempLog.getChanges().size() == 0 &&
                                            tempLog.getFeatures().size() == 0)
                                    {
                                        JOptionPane.showMessageDialog(jPanel2, "Please add at least one changelog to each update in this package.");
                                        return;
                                    }

                                }


                                //Everthing looks ok..

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

                                            updateComboModel.removeAllElements();
                                            UpdateList updateList = updateCreator.getUpdateList();
                                            Set updateSet = updateList.getUpdateSet();
                                            Iterator iterator = updateSet.iterator();
                                            while(iterator.hasNext()){
                                                Update nextUpdate = (Update)iterator.next();
                                                updateComboModel.addElement(nextUpdate.getVersion());
                                            }

                                            update = updateList.getUpdateSet().first();
                                            updateComboModel.setSelectedItem(update.getVersion());
                                            updateCreator.setCurrentUpdate(update);
                                            loadUpdate(update);

                                        }else{
                                            clearAll(false);
                                            updateCreator = new UpdateCreator();
                                            update = updateCreator.getCurrentUpdate();
                                            update.setImportance_level(Update.LV_LOW);
                                            update.setType(Update.TYPE_MAINTENANCE);
                                            update.setUrl("");
                                            updateComboModel.removeAllElements();
                                            updateComboModel.addElement(update.getVersion());
                                            updateComboModel.setSelectedItem(update.getVersion());
                                            updateVersion.setText(update.getVersion().toString());
                                            loadUpdate(update);
                                        }


                                        UpdateCreatorName.setText(updateCreatorFile.getName());
                                        UpdateCreatorDir.setText(updateCreatorFile.getAbsolutePath());
                                        currentDir = new File(updateCreatorFile.getParent());

                                        //Turn on everything!
                                        setEnabledAll(true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out.println("Save command cancelled by user.");
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
                            UpdaterType.addItemListener(new ItemListener(){

                                public void itemStateChanged(ItemEvent arg0) {
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

                                }

                            });
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
                                        updaterURL.setText(text);
                                    }
                                    update.setUrl(text);
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
                            UpdaterImportance.addItemListener(new ItemListener(){
                                public void itemStateChanged(ItemEvent e) {

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


                                }

                            });
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
                            updateVersion.addFocusListener(new FocusListener(){

                                public void focusGained(FocusEvent arg0) { }

                                public void focusLost(FocusEvent arg0) {
                                    if(!update.getVersion().toString().equalsIgnoreCase(updateVersion.getText())){
                                        String oldVer = update.getVersion().toString();
                                        update.setVersion(new Version(updateVersion.getText()));
                                        updateComboModel.insertElementAt(update.getVersion(), updateCombo.getSelectedIndex());
                                        for(int i = 0; i < updateComboModel.getSize(); i++){
                                            Version ver = (Version)updateComboModel.getElementAt(i);
                                            if(ver.toString().equalsIgnoreCase(oldVer)){
                                                updateComboModel.removeElementAt(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            });

                            updateVersion.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent evt) {
                                    if(!update.getVersion().toString().equalsIgnoreCase(updateVersion.getText())){
                                        String oldVer = update.getVersion().toString();
                                        update.setVersion(new Version(updateVersion.getText()));
                                        updateComboModel.insertElementAt(update.getVersion(), updateCombo.getSelectedIndex());
                                        for(int i = 0; i < updateComboModel.getSize(); i++){
                                            Version ver = (Version)updateComboModel.getElementAt(i);
                                            if(ver.toString().equalsIgnoreCase(oldVer)){
                                                updateComboModel.removeElementAt(i);
                                                break;
                                            }
                                        }
                                    }                                }
                            });


                        }




                    }

                    //START >>  addUpdate
                    addUpdate = new JButton();
                    jPanel2.add(addUpdate, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                    addUpdate.setText("Add New Update to Package");
                    addUpdate.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            update = updateCreator.newUpdate();
                            int sizeOfSet = updateCreator.getUpdateList().getUpdateSet().size();
                            boolean bcontainsUpdateVersion = false;
                            for(int i = 0 ; i < updateComboModel.getSize(); i++){
                                Version testV = (Version)updateComboModel.getElementAt(i);
                                if(testV.toString().equalsIgnoreCase(String.valueOf(sizeOfSet))){
                                    bcontainsUpdateVersion = true;
                                }
                            }
                            if(!bcontainsUpdateVersion){
                                update.setVersion(new Version(String.valueOf(updateCreator.getUpdateList().getUpdateSet().size())));
                            }else
                                update.setVersion(new Version("0"));
                            update.setChangeLog(new Changelog());

                            //add it to the updateCreator list
                            updateCreator.addUpdateToList(update);

                            //Set the defaults
                            update.setImportance_level(Update.LV_LOW);
                            update.setType(Update.TYPE_MAINTENANCE);
                            update.setUrl("");

                            //load it's stats in the tabs
                            loadUpdate(update);

                            //add it to the comboboxmodel and select it
                            updateComboModel.addElement(update.getVersion());
                            updateComboModel.setSelectedItem(update.getVersion());

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
                    jPanel1.setPreferredSize(new java.awt.Dimension(800, 452));
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
                                File archiveFile = (File)map.get(currentUF.getName());
                                if(unpackFile.isSelected()){
                                    try{
                                        updateCreator.setZipArchivFiles(archiveFile, currentUF);
                                        if(currentUF.getArchivFiles().size() > 0){
                                            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) fileTree.getSelectionPath().getLastPathComponent();
                                            List<UpdateFile> archList = currentUF.getArchivFiles();
                                            Iterator it = archList.iterator();
                                            while(it.hasNext()){
                                                UpdateFile nextUF = (UpdateFile)it.next();
                                                DefaultMutableTreeNode archNodeToAdd = new DefaultMutableTreeNode(nextUF.getName());
                                                fileTreeModel.insertNodeInto(archNodeToAdd, parentNode, parentNode.getChildCount());
                                                fileTree.scrollPathToVisible(new TreePath(archNodeToAdd.getPath()));
                                            }
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                }else{
                                    currentUF.clearArcive();
                                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) fileTree.getSelectionPath().getLastPathComponent();
                                    while(parentNode.getChildCount() > 0){
                                        DefaultMutableTreeNode subNode = (DefaultMutableTreeNode)parentNode.getLastChild();
                                        fileTreeModel.removeNodeFromParent(subNode);
                                    }
                                }
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
                                    if(totalMap.containsKey(currentDirFile.getName())){
                                        JOptionPane.showMessageDialog(jPanel2, "A file by that name is already in an upadate package.  No duplicate file names allowed.");
                                        return;
                                    }else
                                        totalMap.put(currentDirFile.getName(),currentDirFile);
                                    currentDir = new File(currentDirFile.getParent());
                                    try {
                                        UpdateFile uf = updateCreator.addFile(
                                                currentDirFile,
                                                "",
                                                "",
                                                1);

                                        //add to the tree
                                        DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(uf.getName());
                                        fileTreeModel.insertNodeInto(nodeToAdd, fileTop, fileTop.getChildCount());
                                        fileTree.scrollPathToVisible(new TreePath(nodeToAdd.getPath()));
                                        String ufName = uf.getName();
                                        if(ufName.endsWith(".gz") || ufName.endsWith(".zip")){
                                            map.put(ufName, currentDirFile);                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }



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
                                if(tPaths == null) return;
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


                                int size = fileTreeModel.getChildCount(fileTop);
                                if (size == 0) { //Nobody's left, disable firing.
                                    remove.setEnabled(false);

                                } else {

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
                            currentUF = null;


                            List<UpdateFile> ufList = update.getFileList();
                            Iterator iterator = ufList.iterator();

                            //If the user selects the root, return
                            if(evt.getPath().getLastPathComponent().toString().equalsIgnoreCase("Files")) return;

                            //Check to see if root is parent
                            if(evt.getPath().getParentPath().getLastPathComponent().toString().equalsIgnoreCase("Files")){
                                while (iterator.hasNext()) {
                                    UpdateFile uf = (UpdateFile) iterator
                                    .next();
                                    if (uf.getName().equalsIgnoreCase(
                                            evt.getPath().getLastPathComponent().toString())) {
                                        currentUF = uf;
                                        bCurrentUFisArch = false;
                                        break;
                                    }
                                }
                            }else{
                                //we are dealing with an archive.. so we need to step through the muck
                                UpdateFile parentUF = null;
                                while (iterator.hasNext()) {
                                    UpdateFile uf = (UpdateFile) iterator
                                    .next();
                                    if (uf.getName().equalsIgnoreCase(
                                            evt.getPath().getParentPath().getLastPathComponent().toString())) {
                                        parentUF = uf;
                                        break;
                                    }
                                }

                                //we now have the parentUF .. so we need to step through IT to find the right one!

                                if(parentUF == null)return;

                                List<UpdateFile> ufArchiveList = parentUF.getArchivFiles();
                                Iterator archIterator = ufArchiveList.iterator();
                                while (archIterator.hasNext()) {
                                    UpdateFile uf = (UpdateFile) archIterator.next();
                                    if (uf.getName().equalsIgnoreCase(
                                            evt.getPath().getLastPathComponent().toString())) {
                                        currentUF = uf;
                                        bCurrentUFisArch = true;
                                        break;
                                    }
                                }

                            }


                            if (currentUF != null) {
                                if(bCurrentUFisArch){
                                    path.setText("");
                                    url.setText("");
                                    path.setEditable(false);
                                    name.setEditable(false);
                                    TypeCombo.setEnabled(false);
                                    url.setEditable(false);
                                    unpackFile.setSelected(false);
                                    unpackFile.setEnabled(false);
                                }else{
                                    name.setEditable(true);
                                    path.setEditable(true);
                                    TypeCombo.setEnabled(true);
                                    url.setEditable(true);
                                    path.setText(currentUF.getPath());
                                    url.setText(currentUF.getUrl());
                                    if (currentUF.getType() == 1) {
                                        TypeCombo.setSelectedIndex(0);
                                    } else
                                        TypeCombo.setSelectedIndex(1);
                                    if(currentUF.getName().endsWith(".zip") || currentUF.getName().endsWith(".gz")){
                                        unpackFile.setEnabled(true);
                                        unpackFile.setSelected(currentUF.isArchive());
                                    }else{
                                        unpackFile.setSelected(false);
                                        unpackFile.setEnabled(false);
                                    }
                                }
                                name.setText(currentUF.getName());
                                version.setText(currentUF.getVersion().toString());
                                hash.setText(currentUF.getHash());
                                size.setText(String.valueOf(currentUF.getSize()));
                            }
                        }
                    });
                    fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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
                                    if(updateCreator.getCurrentUpdate().getChangeLog()==null){
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
                            if(node.equals(bugNode)|| node.equals(changeNode) || node.equals(featureNode)) return;
                            String s = (String)JOptionPane.showInputDialog(
                                    jPanel3,
                                    "Edit Changlog Message",
                                    "Customized Dialog",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    node.toString());


                            if ((s != null) && (s.length() > 0)) {


                                if(node.getParent().equals(bugNode)){
                                    int location = bugNode.getIndex(node);
                                    log.removeBugFix(node.toString());
                                    log.addBugFix(s);
                                    treeModel.removeNodeFromParent(node);
                                    DefaultMutableTreeNode bugChildNode = new DefaultMutableTreeNode(s);
                                    treeModel.insertNodeInto(bugChildNode, bugNode, location);
                                    chTree.scrollPathToVisible(new TreePath(bugChildNode.getPath()));
                                }else if(node.getParent().equals(changeNode)){
                                    int location = changeNode.getIndex(node);
                                    log.removeChange(node.toString());
                                    log.addChange(s);
                                    treeModel.removeNodeFromParent(node);
                                    DefaultMutableTreeNode changeChildNode = new DefaultMutableTreeNode(s);
                                    treeModel.insertNodeInto(changeChildNode, changeNode, location);
                                    chTree.scrollPathToVisible(new TreePath(changeChildNode.getPath()));
                                }else if(node.getParent().equals(featureNode)){
                                    int location = featureNode.getIndex(node);
                                    log.removeFeature(node.toString());
                                    log.addFeature(s);
                                    treeModel.removeNodeFromParent(node);
                                    DefaultMutableTreeNode featureChildNode = new DefaultMutableTreeNode(s);
                                    treeModel.insertNodeInto(featureChildNode, featureNode, location);
                                    chTree.scrollPathToVisible(new TreePath(featureChildNode.getPath()));
                                }
                            }


                        }
                    });
                    //END <<  jButton1

                }

            }
            updateCreator = new UpdateCreator();
            update = updateCreator.getCurrentUpdate();

            //Turn off everything!
            setEnabledAll(false);

            this.setSize(813, 496);
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
                                clearAll(true);

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

    /**
     * Clears everything.. and remaps the updates if bClearUpdate is true
     * @param bClearUpdates
     */
    public void clearAll(boolean bClearUpdates){
        //Remake the elements
        if(bClearUpdates){
            updateCreator = new UpdateCreator();
            update = updateCreator.getCurrentUpdate();
            log = new Changelog();
            currentDirFile = null;
            updateCreatorFile = null;
        }

        //Clear 1st tab
        UpdateCreatorName.setText("Not Selected Yet");
        UpdateCreatorDir.setText("Not Selected Yet");
        UpdateCreatorCompressed.setSelected(false);

        UpdaterImportance.setSelectedIndex(0);
        UpdaterType.setSelectedIndex(0);
        updaterURL.setText("");
        updateVersion.setText("");


        updateComboModel.removeAllElements();
        updateComboModel.addElement("Not Selected Yet");

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

        //turn off everything!
        setEnabledAll(false);
    }

    public void setEnabledAll(boolean bEnabled){
        remove.setEnabled(bEnabled);
        addFile.setEnabled(bEnabled);
        url.setEnabled(bEnabled);
        version.setEnabled(bEnabled);
        path.setEnabled(bEnabled);
        name.setEnabled(bEnabled);
        TypeCombo.setEnabled(bEnabled);
        updaterURL.setEnabled(bEnabled);
        UpdateCreatorCompressed.setEnabled(bEnabled);
        create.setEnabled(bEnabled);
        chEnter.setEnabled(bEnabled);
        chType.setEnabled(bEnabled);
        UpdaterType.setEnabled(bEnabled);
        UpdaterImportance.setEnabled(bEnabled);
        jButton1.setEnabled(bEnabled);
        editSelected.setEnabled(bEnabled);
        addUpdate.setEnabled(bEnabled);
        updateCombo.setEnabled(bEnabled);
        writeChangelog.setEnabled(bEnabled);
        chRemove.setEnabled(bEnabled);
        unpackFile.setEnabled(bEnabled);
        updateVersion.setEnabled(bEnabled);
    }


    public void loadUpdate(Update updateToLoad){
        //Fill out the update info
        if(updateToLoad == null) return;

        switch(updateToLoad.getType()){
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


        updaterURL.setText(updateToLoad.getUrl());


        //"Low", "Feature","Change","BugFix","Security Risk"
        switch(updateToLoad.getImportance_level()){
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

        updateVersion.setText(updateToLoad.getVersion().toString());


        //Add in previous files if they exist


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


        List<UpdateFile> files = updateToLoad.getFileList();
        for(int i = 0; i < files.size(); i++){
            UpdateFile uf = files.get(i);
            DefaultMutableTreeNode nodeToAdd = new DefaultMutableTreeNode(uf.getName());
            fileTreeModel.insertNodeInto(nodeToAdd, fileTop, fileTop.getChildCount());
            fileTree.scrollPathToVisible(new TreePath(nodeToAdd.getPath()));

            if(uf.isArchive()){
                if(uf.getArchivFiles().size() > 0){
                    List<UpdateFile> archList = uf.getArchivFiles();
                    Iterator it = archList.iterator();
                    while(it.hasNext()){
                        UpdateFile nextUF = (UpdateFile)it.next();
                        DefaultMutableTreeNode archNodeToAdd = new DefaultMutableTreeNode(nextUF.getName());
                        fileTreeModel.insertNodeInto(archNodeToAdd, nodeToAdd, nodeToAdd.getChildCount());
                        fileTree.scrollPathToVisible(new TreePath(archNodeToAdd.getPath()));
                    }
                }
            }
        }

        //pull in the changelog and populate 3rd tab
        if(updateToLoad.getChangeLog() == null)
            updateToLoad.setChangeLog(new Changelog());
        log = updateToLoad.getChangeLog();
        List<String> bugList = log.getBugFixes();
        List<String> changeList = log.getChanges();
        List<String> featureList = log.getFeatures();



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

}
