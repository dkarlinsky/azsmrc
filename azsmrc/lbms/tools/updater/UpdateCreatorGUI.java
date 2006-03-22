package lbms.tools.updater;

import java.awt.BorderLayout;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
	private JList fileList;
	private JMenuItem exitMenuItem;
	private JButton remove;
	private JButton addFile;
	private JMenu jMenu3;
	private JMenuBar jMenuBar1;
	private DefaultListModel fileListModel;
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

	private File currentDirFile;
	private File updateCreatorFile;
    private File currentDir;
	private Changelog log = new Changelog();
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

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		UpdateCreatorGUI inst = new UpdateCreatorGUI();
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
					jTabbedPane1.setPreferredSize(new java.awt.Dimension(649, 390));

				}

				{
					jPanel2 = new JPanel();
					jPanel2.setLayout(null);
					jTabbedPane1.addTab("General", null, jPanel2, null);
					{
						create = new JButton();
						jPanel2.add(create);
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
								List<UpdateFile> files = updateCreator.getUpdate().getFileList();
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
						updateCreatorPanel.setLayout(null);
						jPanel2.add(updateCreatorPanel);
						updateCreatorPanel.setBounds(14, 49, 287, 231);
						updateCreatorPanel.setBackground(new java.awt.Color(255,255,255));
						updateCreatorPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						{
							UpdateCreatorDir = new JTextPane();
							updateCreatorPanel.add(UpdateCreatorDir);
							UpdateCreatorDir.setText("Not Selected Yet");
							UpdateCreatorDir.setPreferredSize(new java.awt.Dimension(273, 70));
							UpdateCreatorDir.setBounds(7, 119, 273, 70);
						}
						{
							UpdateCreatorCompressed = new JCheckBox();
							updateCreatorPanel.add(UpdateCreatorCompressed);
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
							updateCreatorPanel.add(UpdateCreatorDirLabel);
							UpdateCreatorDirLabel.setText("Update Save Location:");
							UpdateCreatorDirLabel.setPreferredSize(new java.awt.Dimension(273, 28));
							UpdateCreatorDirLabel.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
							UpdateCreatorDirLabel.setHorizontalAlignment(SwingConstants.CENTER);
							UpdateCreatorDirLabel.setBounds(7, 84, 273, 28);
						}
						{
							UpdateCreatorName = new JLabel();
							updateCreatorPanel.add(UpdateCreatorName);
							UpdateCreatorName.setText("Not Selected Yet");
							UpdateCreatorName.setPreferredSize(new java.awt.Dimension(273, 28));
							UpdateCreatorName.setBounds(7, 49, 273, 28);
						}

						{
							UpdateCreatorNameLabel = new JLabel();
							updateCreatorPanel.add(UpdateCreatorNameLabel);
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
						jPanel2.add(savePackageFile);
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
                                            update = updateCreator.getUpdate();

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
                                                    fileListModel.addElement(files.get(i).getName());
                                                }
                                                fileList.setSelectedIndex(0);
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
						updaterOptionsPanel.setLayout(null);
						jPanel2.add(updaterOptionsPanel);
						updaterOptionsPanel.setBounds(308, 49, 322, 231);
						updaterOptionsPanel.setBackground(new java.awt.Color(225,225,225));
						updaterOptionsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
						{
							ComboBoxModel UpdaterTypeModel = new DefaultComboBoxModel(
								new String[] { "Maintenance", "Stable","Beta" });
							UpdaterType = new JComboBox();
							updaterOptionsPanel.add(UpdaterType);
							UpdaterType.setModel(UpdaterTypeModel);
							UpdaterType.setBounds(126, 189, 189, 28);
						}
						{
							UpdaterTypeLabel = new JLabel();
							updaterOptionsPanel.add(UpdaterTypeLabel);
							UpdaterTypeLabel.setText("Update Type:");
							UpdaterTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
							UpdaterTypeLabel.setBounds(7, 182, 105, 35);
						}
						{
							UpdaterImportanceLabel = new JLabel();
							updaterOptionsPanel.add(UpdaterImportanceLabel);
							UpdaterImportanceLabel.setText("Update Level:");
							UpdaterImportanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
							UpdaterImportanceLabel.setBounds(7, 133, 105, 35);
						}
						{
							updaterLabel = new JLabel();
							updaterOptionsPanel.add(updaterLabel);
							updaterLabel.setText("Update Options");
							updaterLabel.setPreferredSize(new java.awt.Dimension(288, 14));
							updaterLabel.setHorizontalAlignment(SwingConstants.CENTER);
							updaterLabel.setBorder(new LineBorder(new java.awt.Color(0,0,0), 1, false));
							updaterLabel.setBounds(17, 12, 288, 23);
						}
						{
							updaterURLLabel = new JLabel();
							updaterOptionsPanel.add(updaterURLLabel);
							updaterURLLabel.setText("Update URL:");
							updaterURLLabel.setPreferredSize(new java.awt.Dimension(98, 35));
							updaterURLLabel.setHorizontalAlignment(SwingConstants.RIGHT);
							updaterURLLabel.setBounds(7, 49, 98, 35);
						}
						{
							updaterURL = new JTextField();
							updaterOptionsPanel.add(updaterURL);
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
							updaterOptionsPanel.add(UpdaterImportance);
							UpdaterImportance.setModel(UpdaterImportanceModel);
							UpdaterImportance.setBounds(126, 133, 189, 28);
						}
						{
							updateVersionLabel = new JLabel();
							updaterOptionsPanel.add(updateVersionLabel);
							updateVersionLabel.setText("Update Version:");
							updateVersionLabel.setBounds(7, 91, 98, 28);
							updateVersionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
						}
						{
							updateVersion = new JTextField();
							updaterOptionsPanel.add(updateVersion);
							updateVersion.setBounds(112, 94, 203, 21);
						}




					}
				}

				this.setTitle("UpdateCreatorGUI");
				{
					jPanel1 = new JPanel();

					jTabbedPane1.addTab("Files", null, jPanel1, null);
					jPanel1.setLayout(null);
					jPanel1.setPreferredSize(new java.awt.Dimension(644, 329));
					{
						detailsPanel = new JPanel();
						detailsPanel.setLayout(null);
						jPanel1.add(detailsPanel);
						detailsPanel.setBorder(BorderFactory
							.createBevelBorder(BevelBorder.LOWERED));
						detailsPanel.setBounds(245, 42, 399, 273);
						{
							label1 = new JLabel();
							detailsPanel.add(label1);
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
							detailsPanel.add(NameLabel);
							NameLabel.setText("Name:");
							NameLabel.setBounds(7, 28, 63, 28);
						}
						{
							PathLabel = new JLabel();
							detailsPanel.add(PathLabel);
							PathLabel.setText("Path:");
							PathLabel.setBounds(7, 53, 63, 28);
						}
						{
							VersionLabel = new JLabel();
							detailsPanel.add(VersionLabel);
							VersionLabel.setText("Version:");
							VersionLabel.setBounds(7, 79, 63, 28);
						}
						{
							URLLabel = new JLabel();
							detailsPanel.add(URLLabel);
							URLLabel.setText("URL:");
							URLLabel.setBounds(7, 105, 63, 28);
						}
						{
							HashLabel = new JLabel();
							detailsPanel.add(HashLabel);
							HashLabel.setText("Hash:");
							HashLabel.setBounds(7, 135, 63, 28);
						}
						{
							SizeLabel = new JLabel();
							detailsPanel.add(SizeLabel);
							SizeLabel.setText("Size:");
							SizeLabel.setBounds(7, 205, 63, 28);
						}
						{
							TypeLabel = new JLabel();
							detailsPanel.add(TypeLabel);
							TypeLabel.setText("Type:");
							TypeLabel.setBounds(7, 168, 63, 28);
						}
						{
							ComboBoxModel TypeComboModel = new DefaultComboBoxModel(
								new String[] { "Normal", "SourceForge" });
							TypeCombo = new JComboBox();
							detailsPanel.add(TypeCombo);
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
							detailsPanel.add(name);
							name.setBounds(63, 35, 329, 21);
							name.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {
									if (currentUF != null
										&& !currentUF.getName()
											.equalsIgnoreCase(name.getText())) {

										currentUF.setName(name.getText());
										fileListModel.setElementAt(name
											.getText(), fileList
											.getSelectedIndex());
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
										fileListModel.setElementAt(name
											.getText(), fileList
											.getSelectedIndex());
									}

								}

							});
						}
						{
							path = new JTextField();
							detailsPanel.add(path);
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
							detailsPanel.add(version);
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
							detailsPanel.add(url);
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
							detailsPanel.add(hash);
							hash.setBounds(63, 138, 329, 21);
						}
						{
							size = new JLabel();
							detailsPanel.add(size);
							size.setBounds(63, 208, 329, 21);
						}
                        //START >>  unpackFile
                        unpackFile = new JCheckBox();
                        detailsPanel.add(unpackFile);
                        unpackFile.setText("Unpack this file");
                        unpackFile.setBounds(7, 238, 385, 28);
                        unpackFile.setEnabled(false);
                        unpackFile.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent arg0) {
                                currentUF.setArchive(unpackFile.isSelected());
                            }
                        });
                        //END <<  unpackFile
					}
					{
						fileListModel = new DefaultListModel();
						fileList = new JList();
						jPanel1.add(fileList);
						fileList.setModel(fileListModel);
						fileList.setBorder(BorderFactory
							.createBevelBorder(BevelBorder.LOWERED));
						fileList.setBounds(7, 42, 224, 273);
						fileList
							.addListSelectionListener(new ListSelectionListener() {

								public void valueChanged(ListSelectionEvent e) {
									//currentUF = null;
									int index = fileList.getSelectedIndex();
									if(index < 0){
										currentUF = null;
										return;
									}
									List<UpdateFile> ufList = update
										.getFileList();
									Iterator iterator = ufList.iterator();

									while (iterator.hasNext()) {
										UpdateFile uf = (UpdateFile) iterator
											.next();
										if (uf.getName().equalsIgnoreCase(
											(String) fileListModel
												.getElementAt(index))) {
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
					}
					{
						addFile = new JButton();
						jPanel1.add(addFile);
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
									fileListModel.addElement(currentDirFile
										.getName());
									fileList.setSelectedIndex(fileListModel
										.getSize() - 1);
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
						jPanel1.add(remove);
						remove.setText("Remove Selected");
						remove.setEnabled(false);
						remove.setBounds(119, 7, 140, 28);
						remove.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent evt) {
								int index = fileList.getSelectedIndex();
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
								}
								int size = fileListModel.getSize();
								if (size == 0) { //Nobody's left, disable firing.
									remove.setEnabled(false);

								} else { //Select an index.
									if (index == fileListModel.getSize()) {
										//removed item in last position
										index--;
									}

									fileList.setSelectedIndex(index);
									fileList.ensureIndexIsVisible(index);
								}
							}
						});
					}





				}
				{
					jPanel3 = new JPanel();
					jTabbedPane1.addTab("Changelog", null, jPanel3, null);
					jPanel3.setLayout(null);
					{
						chLabel1 = new JLabel();
						jPanel3.add(chLabel1);
						chLabel1.setText("Enter Changelog Text then choose type then hit \"Enter\"");
						chLabel1.setBounds(35, 7, 483, 28);
					}
					{
						chArea = new JTextArea();
						jPanel3.add(chArea);
						chArea.setText("");
						chArea.setBounds(35, 42, 574, 56);
						chArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
					}
					{
						ComboBoxModel chTypeModel = new DefaultComboBoxModel(
							new String[] { "Bug Fix", "Change", "Feature" });
						chType = new JComboBox();
						jPanel3.add(chType);
						chType.setModel(chTypeModel);
						chType.setBounds(42, 105, 245, 28);
					}
					{
						chEnter = new JButton();
						jPanel3.add(chEnter);
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
                    jPanel3.add(chRemove);
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
                    jPanel3.add(chTreeScrollPane);
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
                    jPanel3.add(writeChangelog);
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
                                    updateCreator.generateChanglogTxt(changelogFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }




                            }

                        }

                    });
                    //END <<  writeChangelog

				}

			}
			updateCreator = new UpdateCreator();
			update = updateCreator.getUpdate();
			this.setSize(657, 419);
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
        update = updateCreator.getUpdate();
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
        fileListModel.removeAllElements();
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
