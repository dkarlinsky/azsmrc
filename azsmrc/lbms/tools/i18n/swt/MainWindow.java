package lbms.tools.i18n.swt;

import java.io.File;
import java.io.IOException;
import java.util.Map;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
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
	private Button save;



	//non-swt stuff
	private File currentI18NDefaultFile;
	private File currentI18NLocalizedFile;
	private Map<String,String> defaultMap;
	private Map<String,String> transMap;
	private boolean isSaved;


	private MainWindow(){
		shell = new Shell (I18NMe.getI18NMe().getDisplay());
		shell.setLayout(new GridLayout(1,false));

		//listener for shell disposal
		shell.addShellListener(new ShellListener(){

			public void shellActivated(ShellEvent arg0) {}

			public void shellClosed(ShellEvent arg0) {
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


		//Open
		MenuItem menuOpen = new MenuItem(fileSubmenu, SWT.PUSH);
		menuOpen.setText ("&Open Default File\tCtrl+O");
		menuOpen.setAccelerator (SWT.MOD1 + 'O');
		menuOpen.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.OPEN);

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

				}
			}
		});

		final MenuItem menuTransOpen = new MenuItem(fileSubmenu, SWT.PUSH);
		menuTransOpen.setText ("&Open Localized File\tCtrl+L");
		menuTransOpen.setAccelerator (SWT.MOD1 + 'L');
		menuTransOpen.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.SAVE);

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
					setIsSaved(false);
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


				if(isSaved || transMap == null)
					menuSave.setEnabled(false);
				else
					menuSave.setEnabled(true);
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
		gd.verticalSpan = 55;
		tableGroup.setLayoutData(gd);

		Composite bComp = new Composite(tableGroup, SWT.NULL);
		bComp.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		bComp.setLayoutData(gd);

		save = new Button (bComp, SWT.PUSH);
		save.setText("Save");
		save.setToolTipText("Delete selected questions");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		save.setLayoutData(gd);
		save.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
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
				}
			}

		});


		//----Main Table
		mainTable = new Table(tableGroup, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		//gd.verticalSpan = 50;
		gd.horizontalSpan = 2;
		mainTable.setLayoutData(gd);
		mainTable.setHeaderVisible(true);

		TableColumn numCol = new TableColumn(mainTable, SWT.LEFT);
		numCol.setWidth(30);
		numCol.setText("#");

		TableColumn keyCol = new TableColumn(mainTable,SWT.LEFT);
		keyCol.setText("Key");
		keyCol.setWidth(300);

		TableColumn defaultCol = new TableColumn(mainTable, SWT.LEFT);
		defaultCol.setText("Default");
		defaultCol.setWidth(300);

		TableColumn transCol = new TableColumn(mainTable,SWT.LEFT);
		transCol.setText("Translated");
		transCol.setToolTipText("Double Click on a question to open it");
		transCol.setWidth(300);


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
					item.setForeground(3,I18NMe.getI18NMe().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
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
					for (int i=0; i<mainTable.getColumnCount (); i++) {
						Rectangle rect = item.getBounds (i);
						if (rect.contains (pt)) {
							final int column = i;
							final Text text = new Text (mainTable, SWT.NONE);
							Listener textListener = new Listener () {
								public void handleEvent (final Event e) {
									switch (e.type) {
										case SWT.FocusOut:
											item.setText (column, text.getText ());
											text.dispose ();
											break;
										case SWT.Traverse:
											switch (e.detail) {
												case SWT.TRAVERSE_RETURN:
													item.setText (column, text.getText ());
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

		setIsSaved(false);

	}



	/**
	 * Sets the main Label
	 * @param newlyOpenedExam
	 */
	private void setOpenedLabels(){
		I18NMe.getI18NMe().getDisplay().syncExec(new Runnable(){
			public void run() {
				String defaultName = "None";

				if(currentI18NDefaultFile != null)
					defaultName = currentI18NDefaultFile.getName();

				if(currentlyOpened != null || !currentlyOpened.isDisposed())
					currentlyOpened.setText("Current Default File: " + defaultName);
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


	private void setIsSaved(final boolean bisSaved){
		I18NMe.getI18NMe().getDisplay().asyncExec(new Runnable(){
			public void run() {
				isSaved = bisSaved;
				save.setEnabled(bisSaved);
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

}//EOF