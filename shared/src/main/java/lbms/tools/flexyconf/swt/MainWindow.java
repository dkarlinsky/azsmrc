/*
 * Created on Apr 25, 2006
 * Created by omschaub
 *
 */

package lbms.tools.flexyconf.swt;

import java.io.File;
import java.io.IOException;

import lbms.tools.flexyconf.FlexyConfiguration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


public class MainWindow {

	//global SWT stuff
	private Shell shell;
	private Tree tree;
	private Composite mainComp;
	private SWTMenu fcMenu;
	private FlexyConfiguration fc;


	private MainWindow(FlexyConfiguration fc){
		this.fc = fc;
		shell = new Shell (FlexyConfigMain.getFlexyConfigMain().getDisplay());
		shell.setLayout(new GridLayout(1,false));

		//listener for shell disposal
		shell.addShellListener(new ShellListener(){

			public void shellActivated(ShellEvent arg0) {}

			public void shellClosed(ShellEvent arg0) {
				FlexyConfigMain.getFlexyConfigMain().close();

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




		//Separator
		new MenuItem(fileSubmenu,SWT.SEPARATOR);

		//Exit
		MenuItem menuExit = new MenuItem (fileSubmenu, SWT.PUSH);
		menuExit.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				FlexyConfigMain.getFlexyConfigMain().close();
			}
		});
		menuExit.setText ("&Exit\tCtrl+Q");
		menuExit.setAccelerator (SWT.MOD1 + 'Q');




		//---------------------Main Composite---------------------------\\
		Composite parent = new Composite(shell,SWT.NULL);
		parent.setLayout(new GridLayout(3,false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		parent.setLayoutData(gd);


		//First we need a sash
		SashForm sash = new SashForm(parent,SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);

		//TODO main application size is here!

		gd.heightHint = 300;
		gd.widthHint = 600;
		sash.setLayoutData(gd);
		sash.setLayout(new GridLayout(1,false));


		//----Tree on left

		//TODO I made the tree only single select.. change to SWT.MULTI if you want that
		tree = new Tree(sash,SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);

		tree.setLayoutData(gd);


		/*/Add in any tree items here
		TreeItem item1 = new TreeItem(tree, SWT.NULL);
		item1.setText("Tree Item 1 -- Change ME!");

		TreeItem item2 = new TreeItem(tree, SWT.NULL);
		item2.setText("Tree Item 2 -- Change ME!");


		TreeItem item3 = new TreeItem(item2, SWT.NULL);
		item3.setText("Tree Item 3 -- Change ME!");
		 */


		//Selection Listener for tree
		tree.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				//TODO this is the main selection event
				//you can pull a treeItem by using event.item

			}
		});


		//---Composite on right for options
		mainComp = new Composite(sash,SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		mainComp.setLayoutData(gd);
		mainComp.setLayout(new GridLayout(1,false));






		//---------------  Buttons
		Composite buttonComp = new Composite(parent,SWT.NULL);
		buttonComp.setLayout(new GridLayout(3,false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		buttonComp.setLayoutData(gd);

		//Add New
		Button save = new Button(buttonComp,SWT.PUSH);
		save.setText("Save or some other feature");
		save.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event arg0) {

				//TODO Listener for Save Button

			}

		});




		//Open the main shell

		shell.pack();

		//Center Shell
		Monitor primary = FlexyConfigMain.getFlexyConfigMain().getDisplay().getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		Rectangle rect = shell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y +(bounds.height - rect.height) / 2;
		shell.setLocation (x, y);


			fcMenu = new SWTMenu(fc,tree,mainComp);
			fcMenu.addAsRoot();


		//open shell
		shell.open();



	}





	public static MainWindow open(FlexyConfiguration fc){
		return new MainWindow(fc);
	}



}
