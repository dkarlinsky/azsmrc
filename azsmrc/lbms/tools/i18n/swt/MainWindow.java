package lbms.tools.i18n.swt;

import java.io.File;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
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

import lbms.tools.i18n.*;

public class MainWindow {


	//global SWT stuff
	private Shell shell;
	private Label currentlyOpened;
	private Table mainTable;




	//non-swt stuff
	private File currentI18NFile;




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
		menuOpen.setText ("&Open\tCtrl+O");
		menuOpen.setAccelerator (SWT.MOD1 + 'O');
		menuOpen.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FileDialog dialog = new FileDialog (shell, SWT.OPEN);

				String fileString = dialog.open();
				if(fileString != null){
					try{
						currentI18NFile = new File(fileString);
						if(!currentI18NFile.isFile() ||
								!currentI18NFile.canRead() ||
								!currentI18NFile.canWrite()){
							MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
							mb.setText("Open Error");
							mb.setMessage("Problems reading choosen i18n File.  Either File is not a File" +
									"or it cannot be read or written to.");
							mb.open();
						}
					}catch(Exception error){
						error.printStackTrace();
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
		setOpenedi18nFileLabel("None");



		//---------------  Buttons
		Composite buttonComp = new Composite(parent,SWT.NULL);
		gl = new GridLayout();
		gl.numColumns = 3;
		gl.marginHeight = 0;
		buttonComp.setLayout(gl);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		buttonComp.setLayoutData(gd);

		//disable it
		setButtons(false);


		//---------------- Table  Group
		Group tableGroup = new Group(parent,SWT.NULL);
		tableGroup.setText("Questions in Exam Bank");
		tableGroup.setLayout(new GridLayout(2,false));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 55;
		tableGroup.setLayoutData(gd);

		Composite bComp = new Composite(tableGroup, SWT.NULL);
		bComp.setLayout(new GridLayout(1,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		bComp.setLayoutData(gd);

		Button delete = new Button (bComp, SWT.PUSH);
		delete.setText("Delete");
		delete.setToolTipText("Delete selected questions");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		delete.setLayoutData(gd);
		delete.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
				TableItem[] items = mainTable.getSelection();
				if(items.length == 0) return;
				for(int i = 0; i < items.length; i++){
					TableItem item = items[i];

					//TODO fix!
				}
				clearTable();
			}

		});






		//----Main Table
		mainTable = new Table(tableGroup, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.BORDER | SWT.CHECK | SWT.MULTI | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		//gd.verticalSpan = 50;
		gd.horizontalSpan = 2;
		mainTable.setLayoutData(gd);
		mainTable.setHeaderVisible(true);

		TableColumn checkColumn = new TableColumn(mainTable, SWT.NULL);
		checkColumn.setWidth(30);

		TableColumn num = new TableColumn(mainTable,SWT.CENTER);
		num.setText("#");
		num.setWidth(50);

		TableColumn type = new TableColumn(mainTable, SWT.LEFT);
		type.setText("Type");
		type.setWidth(75);

		TableColumn questionColumn = new TableColumn(mainTable,SWT.LEFT);
		questionColumn.setText("Question");
		questionColumn.setToolTipText("Double Click on a question to open it");
		questionColumn.setWidth(600);


		//main setdata listener for table
		mainTable.addListener(SWT.SetData, new Listener(){

			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;

	            int tableIndex = mainTable.indexOf(item);


	            item.setText(1,String.valueOf(tableIndex+1));

	            //item.setData(entry);
	            //item.setChecked(true);
			}

		});

		mainTable.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				TableItem[] items = mainTable.getSelection();
				if(items.length > 1 || items.length == 0) return;


				//TODO fix!
			}

			public void mouseDown(MouseEvent arg0) {}

			public void mouseUp(MouseEvent arg0) {}

		});

		//Open the main shell
		centerShellandOpen(shell);



	}



	/**
	 * Sets the main Label
	 * @param newlyOpenedExam
	 */
	private void setOpenedi18nFileLabel(final String text){
		I18NMe.getI18NMe().getDisplay().syncExec(new Runnable(){
			public void run() {
				if(currentlyOpened != null || !currentlyOpened.isDisposed())
					currentlyOpened.setText("Currently Opened i18n File: " + text);
				if(shell != null || !shell.isDisposed()){
					if(text.equalsIgnoreCase("None"))
						shell.setText("I18NMe");
					else
						shell.setText("I18NMe: " + text);
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
					setOpenedi18nFileLabel(currentI18NFile.getName());

					//mainTable.setItemCount(currentTDB.entries.size());
					mainTable.clearAll();
				}
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