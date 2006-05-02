package lbms.tools.i18n.swt;

import java.io.File;
import java.io.IOException;
import java.util.Map;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import lbms.tools.i18n.*;

public class MainWindow {


	//global SWT stuff
	private Shell shell;
	private Label currentlyOpened;
	private Table mainTable;
	private Button add,save;
	private Color backgroundC;


	//non-swt stuff
	private File currentI18NDefaultFile;
	private File currentI18NLocalizedFile;
	private Map<String,String> defaultMap;
	private Map<String,String> transMap;
	private boolean isSaved;
	private String workingDir;


	private MainWindow(){
		shell = new Shell (I18NMe.getI18NMe().getDisplay());
		shell.setLayout(new GridLayout(1,false));

		//listener for shell disposal
		shell.addShellListener(new ShellListener(){

			public void shellActivated(ShellEvent arg0) {}

			public void shellClosed(ShellEvent arg0) {
				if(backgroundC != null && !backgroundC.isDisposed())
					backgroundC.dispose();

				I18NMe.getI18NMe().close();				
			}

			public void shellDeactivated(ShellEvent arg0) {}

			public void shellDeiconified(ShellEvent arg0) {}

			public void shellIconified(ShellEvent arg0) {}

		});


		//-------------------------Main Menu-------------------------------\\
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);

		//- File Submenu
		MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
		fileItem.setText ("File");
		Menu fileSubmenu = new Menu (shell, SWT.DROP_DOWN);
		fileItem.setMenu (fileSubmenu);

		MenuItem menuNew = new MenuItem(fileSubmenu, SWT.PUSH);
		menuNew.setText ("&New Default File\tCtrl+N");
		menuNew.setAccelerator (SWT.MOD1 + 'N');
		menuNew.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.SAVE);
				dialog.setText("Choose New Default File");
				String fileString = dialog.open();
				if(fileString != null){
					try{
						currentI18NDefaultFile = new File(fileString);
						if(currentI18NDefaultFile.isFile()){
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK | SWT.CANCEL);
							mb.setText("File Exists");
							mb.setMessage("Chosen file already exists.  Are you sure you want to overwrite it with a blank file?");
							int choice = mb.open();
							if(choice == SWT.CANCEL) return;
							currentI18NDefaultFile.delete();
						}
						currentI18NDefaultFile.createNewFile();
						currentI18NLocalizedFile = null;
					}catch(Exception error){
						error.printStackTrace();
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("File Error");
						mb.setMessage("Error writing file.  IOError.  Please check your settings and try again.");
						mb.open();
					}

					try {
						defaultMap = I18NTools.readFromFile(currentI18NDefaultFile);
						transMap = I18NTools.duplicate(defaultMap);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Open Error");
						mb.setMessage("Problems initializing I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
					clearTable();
					setIsSaved(false,true);
				}
			}
		});



		//Open
		MenuItem menuOpen = new MenuItem(fileSubmenu, SWT.PUSH);
		menuOpen.setText ("&Open Default File\tCtrl+O");
		menuOpen.setAccelerator (SWT.MOD1 + 'O');
		menuOpen.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.OPEN);
				dialog.setText("Open Default File");
				String fileString = dialog.open();
				if(fileString != null){
					try{
						currentI18NDefaultFile = new File(fileString);
						if(!currentI18NDefaultFile.isFile() ||
								!currentI18NDefaultFile.canRead() ||
								!currentI18NDefaultFile.canWrite()){
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Open Error");
							mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
							"or it cannot be read or written to.");
							mb.open();
						}
					}catch(Exception error){
						error.printStackTrace();
					}

					try {
						currentI18NLocalizedFile = null;
						defaultMap = I18NTools.readFromFile(currentI18NDefaultFile);
						transMap = I18NTools.duplicate(defaultMap);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Open Error");
						mb.setMessage("Problems initializing I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
					clearTable();
					setIsSaved(false,true);
				}
			}
		});

		final MenuItem menuTransOpen = new MenuItem(fileSubmenu, SWT.PUSH);
		menuTransOpen.setText ("Open &Localized File\tCtrl+L");
		menuTransOpen.setAccelerator (SWT.MOD1 + 'L');
		menuTransOpen.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.OPEN);
				dialog.setText("Open Localized Default File");
				String fileString = dialog.open();
				if(fileString != null){
					try{
						currentI18NLocalizedFile = new File(fileString);
						if(!currentI18NLocalizedFile.exists())
							currentI18NLocalizedFile.createNewFile();

						if(!currentI18NLocalizedFile.isFile() ||
								!currentI18NLocalizedFile.canRead() ||
								!currentI18NLocalizedFile.canWrite()){
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Open Error");
							mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
							"or it cannot be read or written to.");
							mb.open();
						}
					}catch(Exception error){
						error.printStackTrace();
					}

					try {
						transMap = I18NTools.readFromFile(currentI18NLocalizedFile);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Open Error");
						mb.setMessage("Problems loading I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
					clearTable();
					setIsSaved(false, true);
				}
			}
		});
		final MenuItem menuSave = new MenuItem(fileSubmenu, SWT.PUSH);
		menuSave.setText ("&Save Localized File\tCtrl+S");
		menuSave.setAccelerator (SWT.MOD1 + 'S');
		menuSave.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				if(currentI18NLocalizedFile != null){
					try {
						I18NTools.writeToFile(currentI18NLocalizedFile, transMap);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Save Error");
						mb.setMessage("Problems writing I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
				}else{
					FileDialog dialog = new FileDialog (shell, SWT.SAVE);
					dialog.setText("Save Localized File");
					String fileString = dialog.open();
					if(fileString != null){
						try{
							currentI18NLocalizedFile = new File(fileString);
							if(!currentI18NLocalizedFile.exists())
								currentI18NLocalizedFile.createNewFile();

							if(!currentI18NLocalizedFile.isFile() ||
									!currentI18NLocalizedFile.canRead() ||
									!currentI18NLocalizedFile.canWrite()){
								MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
								mb.setText("Open Error");
								mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
								"or it cannot be read or written to.");
								mb.open();
							}
						}catch(Exception error){
							error.printStackTrace();
						}

						try {
							I18NTools.writeToFile(currentI18NLocalizedFile, transMap);
						} catch (IOException e1) {
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Save Error");
							mb.setMessage("Problems writing I18N File.  IOException error!");
							mb.open();
							e1.printStackTrace();
						}
					}
					clearTable();

				}
			}
		});



		final MenuItem menuSaveAs = new MenuItem(fileSubmenu, SWT.PUSH);
		menuSaveAs.setText ("Save Localized File &As");		
		menuSaveAs.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.SAVE);
				dialog.setText("Save Localized File As");
				String fileString = dialog.open();
				if(fileString != null){
					try{
						currentI18NLocalizedFile = new File(fileString);
						if(!currentI18NLocalizedFile.exists())
							currentI18NLocalizedFile.createNewFile();

						if(!currentI18NLocalizedFile.isFile() ||
								!currentI18NLocalizedFile.canRead() ||
								!currentI18NLocalizedFile.canWrite()){
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Open Error");
							mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
							"or it cannot be read or written to.");
							mb.open();
						}
					}catch(Exception error){
						error.printStackTrace();
					}

					try {
						I18NTools.writeToFile(currentI18NLocalizedFile, transMap);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Save Error");
						mb.setMessage("Problems writing I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
					clearTable();

				}
			}
		});




		//Separator
		new MenuItem(fileSubmenu,SWT.SEPARATOR);

		//Exit
		MenuItem menuExit = new MenuItem (fileSubmenu, SWT.PUSH);
		menuExit.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				I18NMe.getI18NMe().close();
			}
		});
		menuExit.setText ("&Exit\tCtrl+Q");
		menuExit.setAccelerator (SWT.MOD1 + 'Q');

		fileSubmenu.addMenuListener(new MenuListener(){

			public void menuHidden(MenuEvent arg0) {}

			public void menuShown(MenuEvent arg0) {
				if(defaultMap == null)
					menuTransOpen.setEnabled(false);
				else
					menuTransOpen.setEnabled(true);


				if(isSaved || transMap == null){
					menuSave.setEnabled(false);
					menuSaveAs.setEnabled(false);
				}else{
					menuSave.setEnabled(true);
					menuSaveAs.setEnabled(true);
				}
			}

		});


		//---------------------Main Composite---------------------------\\
		Composite parent = new Composite(shell,SWT.NULL);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		gl.marginHeight = 0;
		parent.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gd);

		currentlyOpened = new Label(parent,SWT.BORDER | SWT.CENTER);
		currentlyOpened.setBackground(I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		currentlyOpened.setLayoutData(gd);

		//Set the text
		setOpenedLabels();


		//---------------- Table  Group
		Group tableGroup = new Group(parent,SWT.NULL);
		tableGroup.setText("Items to Translate");
		tableGroup.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 90;
		tableGroup.setLayoutData(gd);

		Composite bComp = new Composite(tableGroup, SWT.NULL);
		bComp.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		bComp.setLayoutData(gd);

		add = new Button(bComp, SWT.PUSH);
		add.setText("Add New Key");		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		add.setLayoutData(gd);
		add.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				NewDialog dlog = new NewDialog("Enter New Key and Default Value");
				String[] newKey = dlog.open();				
				if(newKey[0] == null) return; //User Cancelled
				if(newKey[0].equalsIgnoreCase("")){
					MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
					mb.setText("Error");
					mb.setMessage("You did not enter a value for the key, please choose Add again and enter a value for the key.");
					mb.open();
					return;
				}

				defaultMap.put(newKey[0], newKey[1]);
				transMap.put(newKey[0], "");
				clearTable();			

			}			
		});


		save = new Button (bComp, SWT.PUSH);
		save.setText("Save All");
		save.setToolTipText("Quick save all changes");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		save.setLayoutData(gd);
		save.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				if(currentI18NLocalizedFile != null && currentI18NDefaultFile != null){
					try {
						I18NTools.writeToFile(currentI18NDefaultFile, defaultMap);
						I18NTools.writeToFile(currentI18NLocalizedFile, transMap);
					} catch (IOException e1) {
						MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
						mb.setText("Save Error");
						mb.setMessage("Problems writing I18N File.  IOException error!");
						mb.open();
						e1.printStackTrace();
					}
				}else{
					FileDialog dialog = new FileDialog (shell, SWT.SAVE);
					dialog.setText("Enter Translated File Name to Save");
					String fileString = dialog.open();
					if(fileString != null){
						try{
							currentI18NLocalizedFile = new File(fileString);
							if(!currentI18NLocalizedFile.exists())
								currentI18NLocalizedFile.createNewFile();

							if(!currentI18NLocalizedFile.isFile() ||
									!currentI18NLocalizedFile.canRead() ||
									!currentI18NLocalizedFile.canWrite()){
								MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
								mb.setText("Open Error");
								mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
								"or it cannot be read or written to.");
								mb.open();
							}
						}catch(Exception error){
							error.printStackTrace();
						}

						try {
							I18NTools.writeToFile(currentI18NDefaultFile, defaultMap);
							I18NTools.writeToFile(currentI18NLocalizedFile, transMap);
						} catch (IOException e1) {
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Save Error");
							mb.setMessage("Problems writing I18N File.  IOException error!");
							mb.open();
							e1.printStackTrace();
						}
					}
				}
			}

		});



		//----Main Table
		mainTable = new Table(tableGroup, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		mainTable.setLayoutData(gd);
		mainTable.setHeaderVisible(true);

		TableColumn numCol = new TableColumn(mainTable, SWT.LEFT);
		numCol.setWidth(30);
		numCol.setText("#");

		TableColumn keyCol = new TableColumn(mainTable,SWT.LEFT);
		keyCol.setText("Key");
		keyCol.setWidth(200);

		TableColumn defaultCol = new TableColumn(mainTable, SWT.LEFT);
		defaultCol.setText("Default");
		defaultCol.setWidth(300);

		TableColumn transCol = new TableColumn(mainTable,SWT.LEFT);
		transCol.setText("Translated");
		transCol.setToolTipText("Double Click on a question to open it");
		transCol.setWidth(400);


		//main setdata listener for table
		mainTable.addListener(SWT.SetData, new Listener(){

			public void handleEvent(Event event) {
				if(defaultMap == null || transMap == null) return;
				TableItem item = (TableItem)event.item;
				int tableIndex = mainTable.indexOf(item);
				item.setText(0,String.valueOf(tableIndex+1));

				String[] keys = defaultMap.keySet().toArray(new String[defaultMap.size()]);
				item.setText(1,keys[tableIndex]);
				item.setText(2,defaultMap.get(keys[tableIndex]).replace("\n", "\\n"));
				item.setText(3,transMap.get(keys[tableIndex]).replace("\n", "\\n"));

				if(item.getText(3).equalsIgnoreCase("")){
					item.setText(3,defaultMap.get(keys[tableIndex]).replace("\n", "\\n"));
					item.setForeground(3,I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_RED));
				}else
					item.setForeground(3,I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_BLACK));

				if(tableIndex%2==0) {
					item.setBackground(getBackgroundColor());					
				}

			}

		});

		final TableEditor editor = new TableEditor (mainTable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		mainTable.addListener (SWT.MouseDown, new Listener () {
			public void handleEvent (Event event) {
				Rectangle clientArea = mainTable.getClientArea ();
				Point pt = new Point (event.x, event.y);
				int index = mainTable.getTopIndex ();
				while (index < mainTable.getItemCount ()) {
					boolean visible = false;
					final TableItem item = mainTable.getItem (index);					
					for (int i=1; i<mainTable.getColumnCount (); i++) {
						Rectangle rect = item.getBounds (i);
						if (rect.contains (pt)) {
							final int column = i;							
							final Text text = new Text (mainTable, SWT.NONE);
							Listener textListener = new Listener () {
								public void handleEvent (final Event e) {
									int open = 0;
									switch (e.type) {
									case SWT.FocusOut:
										if(column == 1 && !item.getText(1).equalsIgnoreCase(text.getText())){
											if(defaultMap.containsKey(text.getText())){
												if(open != SWT.OPEN){
													MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
													mb.setText("Error");
													mb.setMessage("You have entered a key value that is already in the file.  Please choose a unique key.");
													open = mb.open();														
												}
												text.setText (item.getText (column));														
												text.selectAll ();
												text.setFocus ();
												break;
											}
											
											//Key is different!
											String key = item.getText(1);
											String defaultValue = item.getText(2);
											String transValue = item.getText(3);
											if(defaultValue.equalsIgnoreCase(transValue))
												transValue = "";
											//remove old keys
											if(defaultMap.containsKey(key))
												defaultMap.remove(key);
											if(transMap.containsKey(key))
												transMap.remove(key);
											//add them back in with new key
											String newKey = text.getText();
											defaultMap.put(newKey, defaultValue);
											transMap.put(newKey, defaultValue);										
											
										}else if(column == 2 && !item.getText(2).equalsIgnoreCase(text.getText())){
											//default is different!
											defaultMap.put(item.getText(1), text.getText());
										}else if(column == 3 && !item.getText(3).equalsIgnoreCase(text.getText())){
											//trans if different!
											transMap.put(item.getText(1), text.getText());
										}										
										clearTable();
										setIsSaved(false, true);
										text.dispose ();											
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:											
											if(column == 1 && !item.getText(1).equalsIgnoreCase(text.getText())){
												if(defaultMap.containsKey(text.getText())){
													if(open != SWT.OPEN){
														MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
														mb.setText("Error");
														mb.setMessage("You have entered a key value that is already in the file.  Please choose a unique key.");
														open = mb.open();	
													}
													
													text.setText (item.getText (column));														
													text.selectAll ();
													text.setFocus ();
													break;
												}
												
												//Key is different!
												String key = item.getText(1);
												String defaultValue = item.getText(2);
												String transValue = item.getText(3);
												if(defaultValue.equalsIgnoreCase(transValue))
													transValue = "";
												//remove old keys
												if(defaultMap.containsKey(key))
													defaultMap.remove(key);
												if(transMap.containsKey(key))
													transMap.remove(key);
												//add them back in with new key
												String newKey = text.getText();
												defaultMap.put(newKey, defaultValue);
												transMap.put(newKey, defaultValue);										
												
											}else if(column == 2 && !item.getText(2).equalsIgnoreCase(text.getText())){
												//default is different!
												defaultMap.put(item.getText(1), text.getText());
											}else if(column == 4 && !item.getText(3).equalsIgnoreCase(text.getText())){
												//trans if different!
												transMap.put(item.getText(1), text.getText());
											}
											clearTable();
											setIsSaved(false, true);												

											//FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose ();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener (SWT.FocusOut, textListener);
							text.addListener (SWT.Traverse, textListener);							
							editor.setEditor (text, item, i);
							text.setText (item.getText (i));														
							text.selectAll ();
							text.setFocus ();
							return;
						}
						if (!visible && rect.intersects (clientArea)) {
							visible = true;
						}
					}
					if (!visible) return;
					index++;
				}
			}
		});
		

		

		//Open the main shell
		centerShellandOpen(shell);

		setIsSaved(false, false);

	}



	/**
	 * Sets the main Label
	 * @param newlyOpenedExam
	 */
	private void setOpenedLabels(){
		I18NMe.getI18NMe().getDisplay().syncExec(new Runnable(){
			public void run() {
				String defaultName = "None";
				String transName = "None";
				if(currentI18NDefaultFile != null)
					defaultName = currentI18NDefaultFile.getName();
				if(currentI18NLocalizedFile != null)
					transName = currentI18NLocalizedFile.getName(); 
				if(currentlyOpened != null || !currentlyOpened.isDisposed())
					currentlyOpened.setText("Current Default File: " + defaultName + 
							"\nCurrent Localized File: " + transName);
				if(shell != null || !shell.isDisposed()){
					if(defaultName.equalsIgnoreCase("None"))
						shell.setText("I18NMe");
					else
						shell.setText("I18NMe: " + defaultName);
				}
			}
		});
	}

	public static MainWindow open(){
		return new MainWindow();
	}







	public void setButtons(final boolean baddNew){
		I18NMe.getI18NMe().getDisplay().syncExec(new Runnable(){

			public void run() {

			}

		});
	}

	public void clearTable(){
		I18NMe.getI18NMe().getDisplay().syncExec(new Runnable(){

			public void run() {
				if(mainTable != null || !mainTable.isDisposed()){
					setOpenedLabels();
					if(defaultMap != null)
						mainTable.setEnabled(true);
					else{
						mainTable.setEnabled(false);
						return;
					}

					mainTable.setItemCount(defaultMap.size());
					mainTable.clearAll();
				}
			}

		});

	}


	private void setIsSaved(final boolean bisSaved, final boolean bSaveAddButtons){
		I18NMe.getI18NMe().getDisplay().asyncExec(new Runnable(){
			public void run() {
				isSaved = bisSaved;
				save.setEnabled(bSaveAddButtons);
				add.setEnabled(bSaveAddButtons);
			}
		});
	}


	/** Centers a Shell and opens it relative to the users Monitor
	 *
	 * @param shell
	 */

	public static void centerShellandOpen(Shell shell){
		//open shell
		shell.pack();

		//Center Shell
		Monitor primary = I18NMe.getI18NMe().getDisplay().getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = shell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y +(bounds.height - rect.height) / 2;
		shell.setLocation (x, y);

		//open shell
		shell.open();
	}

	/**
	 * Generate a nice light background color for tables based off of list_background
	 * @return Color
	 */
	public Color getBackgroundColor(){


		if(I18NMe.getI18NMe().getDisplay()==null && I18NMe.getI18NMe().getDisplay().isDisposed()){
			backgroundC = null;
			return backgroundC;
		}
		try{
			backgroundC = new Color(I18NMe.getI18NMe().getDisplay() ,
					new RGB(I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRed()-20,
							I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getGreen()-20,
							I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getBlue()-20));

		}catch(Exception e){
			backgroundC = I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		}



		return backgroundC;
	}

}//EOF