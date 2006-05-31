/*
 * Created on Feb 18, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;


import lbms.azsmrc.remote.client.Constants;
import lbms.azsmrc.remote.client.User;
import lbms.azsmrc.remote.client.UserManager;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ClientUpdateListener;
import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.ColorUtilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;



import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;


public class ManageUsersTab {
	private Table userTable;
	private UserManager userManager;

	//I18N prefix
	public static final String PFX = "tab.manageuserstab.";

	private ManageUsersTab(CTabFolder parentTab){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText(I18N.translate(PFX + "tab.text"));

		userManager = RCMain.getRCMain().getClient().getUserManager();
		userManager.update();


		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);



		open(parent);



		//The Client update listener
		final ClientUpdateListener cul = new ClientUpdateListener(){

			public void update(long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_USERS) != 0){
					redrawTable();
				}

				if((updateSwitches & Constants.UPDATE_ADVANCED_STATS) != 0){


				}

				if((updateSwitches & Constants.UPDATE_LIST_TRANSFERS) != 0){

				}


			}
		};

		//Add the CUL to the Client
		RCMain.getRCMain().getClient().addClientUpdateListener(cul);

		//Listen for when tab is closed and make sure to remove the client update listener
		detailsTab.addDisposeListener(new DisposeListener(){
			public void widgetDisposed(DisposeEvent arg0) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(cul);
			}
		});



		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	public void open(Composite composite){
		final boolean isAdmin = true;
		//------------UserTable and it's toolbar-----------\\

		//Group for both the toolbar and the usertable
		Group userTable_group = new Group(composite, SWT.NULL);
		userTable_group.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = 5;
		userTable_group.setLayoutData(gridData);


		userTable_group.setText(I18N.translate(PFX + "userTable_group.text") + "  " + RCMain.getRCMain().getClient().getUsername());
		//Toolbar for the usertable
		ToolBar userTable_toolbar = new ToolBar(userTable_group,SWT.FLAT | SWT.HORIZONTAL);


		//Add User ToolItem
		ToolItem addUser = new ToolItem(userTable_toolbar,SWT.PUSH);
		addUser.setImage(ImageRepository.getImage("add"));
		addUser.setToolTipText(I18N.translate(PFX + "toolbar.add.tooltip"));



		//Listener for add user
		final Listener addNew_listener = new Listener() {
			public void handleEvent(Event e) {
				RCMain.getRCMain().getDisplay().asyncExec(new Runnable (){
					public void run () {
						addNewUser();
					}
				});
			}
		};

		//Add listener to the toolitem
		addUser.addListener(SWT.Selection,addNew_listener);

		//Listener for delete user
		final Listener deleteUser_listener = new Listener() {
			public void handleEvent(Event e) {
				RCMain.getRCMain().getDisplay().asyncExec(new Runnable (){
					public void run () {
						//Pull the selected items
						TableItem[] items = userTable.getSelection();

						//Check if only one
						if(items.length > 0 ){
							if(items[0].getText(0).equalsIgnoreCase(RCMain.getRCMain().getClient().getUsername())){
								MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
								mb.setText(I18N.translate("global.error"));
								mb.setMessage(I18N.translate(PFX + "toolbar.deleteuser.messabox.message"));
								mb.open();
								return;
							}else{
								deleteUser(items[0].getText(0));
							}

						}else return;



					}
				});
			}
		};





		userTable_toolbar.pack();


		//--userTable
		userTable = new Table(userTable_group, SWT.BORDER |  SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL| SWT.FULL_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalSpan = 5;
		userTable.setLayoutData(gridData);

		//Columns for the userTable
		userTable.setHeaderVisible(true);

		TableColumn userName = new TableColumn(userTable,SWT.LEFT);
		userName.setText(I18N.translate(PFX + "usertable.column.name.text"));
		userName.setWidth(150);

		TableColumn userType = new TableColumn(userTable,SWT.CENTER);
		userType.setText(I18N.translate(PFX + "usertable.column.type.text"));
		userType.setWidth(100);

		TableColumn downloadSlots = new TableColumn(userTable,SWT.CENTER);
		downloadSlots.setText(I18N.translate(PFX + "usertable.column.download_slots.text"));
		//downloadSlots.setWidth(100);
		downloadSlots.pack();


		TableColumn outputDir = new TableColumn(userTable,SWT.LEFT);
		outputDir.setText(I18N.translate(PFX + "usertable.column.outputdir.text"));
		outputDir.setWidth(300);

		TableColumn inputDir = new TableColumn(userTable,SWT.LEFT);
		inputDir.setText(I18N.translate(PFX + "usertable.column.inputdir.text"));
		inputDir.setWidth(300);


		//SetData listener for the userTable
		userTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event e) {
				//pull the item
				TableItem item = (TableItem)e.item;

				//get the index of the item
				int index = userTable.indexOf(item);

				try {

					User[] users= userManager.getUsers();
					item.setText(0, users[index].getUsername());

					if(users[index].getRights() == RemoteConstants.RIGHTS_ADMIN)
						item.setText(1,I18N.translate(PFX + "usertable.administrator"));
					else
						item.setText(1,I18N.translate(PFX + "usertable.user"));

					item.setText(2,Integer.toString(users[index].getDownloadSlots()));
					item.setText(3,users[index].getOutputDir());
					item.setText(4,users[index].getAutoImportDir());




					//gray if needed
					if(index%2!=0){
						item.setBackground(ColorUtilities.getBackgroundColor());
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});

		//popup menu for userTable
		Menu popupmenu_table = new Menu(userTable);

		final MenuItem changePassword = new MenuItem(popupmenu_table, SWT.PUSH);
		changePassword.setText(I18N.translate(PFX + "usertable.menu.changepassword.text"));
		changePassword.setEnabled(false);
		changePassword.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e){
				TableItem[] items = userTable.getSelection();
				if(items.length == 1){
					changePassword(items[0].getText(0));
				}
			}
		});

		final MenuItem editUser = new MenuItem(popupmenu_table, SWT.PUSH);
		editUser.setText(I18N.translate(PFX + "usertable.menu.edituser.text"));
		editUser.setEnabled(false);
		editUser.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e){
				TableItem[] items = userTable.getSelection();
				if(items.length == 1){
					editUserInfo(items[0].getText(0),isAdmin);
				}
			}
		});

		new MenuItem(popupmenu_table, SWT.SEPARATOR);

		final MenuItem deleteUser = new MenuItem(popupmenu_table, SWT.PUSH);
		deleteUser.setText(I18N.translate(PFX + "usertable.menu.deleteuser.text"));
		deleteUser.setImage(ImageRepository.getImage("delete"));
		deleteUser.addListener(SWT.Selection, deleteUser_listener);

		popupmenu_table.addMenuListener(new MenuListener(){
			public void menuHidden(MenuEvent arg0) {


			}

			public void menuShown(MenuEvent arg0) {
				changePassword.setEnabled(false);

				TableItem[] item = userTable.getSelection();
				if(item.length == 1){
					if(!isAdmin && item[0].getText(0).equalsIgnoreCase(RCMain.getRCMain().getClient().getUsername())){
						changePassword.setEnabled(true);
						editUser.setEnabled(true);
					}else if(isAdmin){
						changePassword.setEnabled(true);
						editUser.setEnabled(true);
					}



				}


			}
		});




		userTable.setMenu(popupmenu_table);
	}






	/**
	 * Redraws the userTable.. since it is virtual, we need to repopulate it
	 * each time the user array is modified
	 *
	 */
	public void redrawTable(){
		// Reset the data so that the SWT.Virtual picks up the array
		RCMain.getRCMain().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (userTable == null || userTable.isDisposed())
					return;

				try{
					userTable.setItemCount(userManager.getUsers().length);
				}catch (Exception e){
					userTable.setItemCount(0);
				}

				userTable.clearAll();



			}
		});
	}



	/**
	 * Shell for adding in a New User
	 *
	 */
	public void addNewUser(){
		if(RCMain.getRCMain().getDisplay()==null && RCMain.getRCMain().getDisplay().isDisposed())
			return;
		RCMain.getRCMain().getDisplay().asyncExec( new Runnable() {
			public void run() {
				//Shell Initialize

				final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				if(!Utilities.isOSX)
					shell.setImage(ImageRepository.getImage("add"));

				//Grid Layout
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				shell.setLayout(layout);

				//composite for shell
				Composite backup_composite = new Composite(shell,SWT.NULL);

				//Grid Layout
				layout = new GridLayout();
				layout.numColumns = 3;
				backup_composite.setLayout(layout);

				//shell title
				shell.setText(I18N.translate(PFX + "addnewuserShell.shell.text"));


				//User Name Label
				Label nameLabel = new Label(backup_composite, SWT.NONE);
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				nameLabel.setLayoutData( gridData );
				nameLabel.setText(I18N.translate(PFX + "addnewuserShell.newuser.text"));


				//User Name Input field
				final Text userName = new Text(backup_composite,SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				userName.setLayoutData( gridData);

				//Password Label
				Label passwordLabel = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				passwordLabel.setLayoutData( gridData );
				passwordLabel.setText(I18N.translate(PFX + "addnewuserShell.password.text"));


				//User Name Input field
				final Text password = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				password.setLayoutData( gridData);

				//verify password Label
				Label verify_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				verify_text.setLayoutData( gridData );
				verify_text.setText(I18N.translate(PFX + "addnewuserShell.verify_password.text"));


				//verify password field
				final Text verify = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				verify.setLayoutData( gridData);


				//Combo Stuff

				//combo Label
				Label combo_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				combo_text.setLayoutData( gridData );
				combo_text.setText(I18N.translate(PFX + "addnewuserShell.userType.text"));

				final Combo combo = new Combo(backup_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
				combo.add(PFX + "usertable.user");
				combo.add(I18N.translate(PFX + "usertable.administrator"));

				combo.select(0);



				//---------Directory stuff ------------\\
				Label blank_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				blank_text.setLayoutData( gridData );
				blank_text.setText("");



				Label dir_text = new Label(backup_composite, SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				dir_text.setLayoutData( gridData );
				dir_text.setText(" " + I18N.translate(PFX + "addnewuserShell.dir_reminder.text") + " ");
				dir_text.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_GRAY));

				//output directory
				Label outputDir_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				outputDir_text.setLayoutData( gridData );
				outputDir_text.setText(I18N.translate(PFX + "addnewuserShell.outputDir.text"));

				//comp for directory input
				Composite output_comp = new Composite(backup_composite,SWT.NONE);
				output_comp.setLayout(new GridLayout(3,false));

				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				output_comp.setLayoutData(gridData);



				//output directory input field
				final Text outputDir = new Text(output_comp,SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 250;
				outputDir.setLayoutData(gridData);


				//auto import directory
				Label importDir_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				importDir_text.setLayoutData( gridData );
				importDir_text.setText(I18N.translate(PFX + "addnewuserShell.inputDir.text"));

				//comp for directory input
				Composite importDir_comp = new Composite(backup_composite,SWT.NONE);
				importDir_comp.setLayout(new GridLayout(3,false));

				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				importDir_comp.setLayoutData(gridData);



				//output directory input field
				final Text importDir = new Text(importDir_comp,SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 250;
				importDir.setLayoutData(gridData);

				//Button for Accept
				Button commit = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.CENTER);
				gridData.horizontalSpan = 1;
				commit.setLayoutData( gridData);
				commit.setText(I18N.translate("global.accept"));
				commit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						if(userName.getText().equalsIgnoreCase("")      ||
								password.getText().equalsIgnoreCase("") ||
								verify.getText().equalsIgnoreCase("")   ||
								outputDir.getText().equalsIgnoreCase("")||
								importDir.getText().equalsIgnoreCase("")){
							MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX + "addnewuserShell.messagebox.message"));
							mb.open();
							return;
						}


						if(password.getText().equalsIgnoreCase(verify.getText())){
							try {
								//System.out.println("Password to send: "+ password.getText());
								userManager.addUserUser(userName.getText(),
										password.getText(),
										importDir.getText(),
										outputDir.getText(),
										1,
										combo.getSelectionIndex());

							} catch (DuplicatedUserException e1) {
								MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
								mb.setText(I18N.translate("global.error"));
								mb.setMessage(I18N.translate(PFX + "addnewuserShell.duplicateError.messagebox.message"));
								mb.open();
								return;
							}



							//destroy the shell
							shell.dispose();

							//redraw the userTable
							redrawTable();

						}else{
							MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX + "addnewuserShell.pwIdenticalError.messagebox.message"));
							mb.open();
							password.setText("");
							verify.setText("");
							return;
						}

					}
				});


				//Button for Cancel
				Button cancel = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gridData.horizontalSpan = 2;
				cancel.setLayoutData( gridData);
				cancel.setText(I18N.translate("global.cancel"));
				cancel.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						shell.dispose();
					}
				});


				//Key Listener so that the user can just use ESC to cancel
				//in the beginning if they did not want to do this
				userName.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
						//Empty
					}

					public void keyReleased (KeyEvent e) {
						switch (e.character){
						case SWT.ESC:

							shell.dispose();
							break;

						}
					}
				});

				//pack and open shell
				GUI_Utilities.centerShellandOpen(shell);

			}
		});

	}

	/**
	 * GUI code to delete a user
	 * @param userName
	 * @return
	 */
	public boolean deleteUser(String userName){
		MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.YES| SWT.NO |SWT.ICON_QUESTION);
		mb.setText(I18N.translate(PFX + "deleteUser.title.text"));
		mb.setMessage(I18N.translate(PFX + "deleteUser.message.text") + " " + userName );
		int response = mb.open();
		switch (response){

		case SWT.YES:
			try {
				userManager.removeUser(userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//redraw the userTable
			redrawTable();
			return true;

		case SWT.NO:
			break;

		}
		return false;
	}


	public void changePassword(final String user){
		final Thread addNew_thread = new Thread() {
			public void run() {
				if(RCMain.getRCMain().getDisplay()==null && RCMain.getRCMain().getDisplay().isDisposed())
					return;
				RCMain.getRCMain().getDisplay().asyncExec( new Runnable() {
					public void run() {
						//Shell Initialize

						final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
						if(!Utilities.isOSX)
							shell.setImage(ImageRepository.getImage("plus"));

						//Grid Layout
						GridLayout layout = new GridLayout();
						layout.numColumns = 1;
						shell.setLayout(layout);

						//composite for shell
						Composite backup_composite = new Composite(shell,SWT.NULL);

						//Grid Layout
						layout = new GridLayout();
						layout.numColumns = 3;
						backup_composite.setLayout(layout);

						//shell title
						shell.setText(I18N.translate(PFX + "changePasswordShell.shell.text"));


						//User Name Label
						Label nameLabel = new Label(backup_composite, SWT.NONE);
						GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						nameLabel.setLayoutData( gridData );
						nameLabel.setText(I18N.translate(PFX + "changePasswordShell.usertochange.text") + " ");


						//User Name Input field
						final Label userName = new Label(backup_composite,SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						userName.setLayoutData( gridData);
						userName.setText(user);


						//Password Label
						Label oldPasswordLabel = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						oldPasswordLabel.setLayoutData( gridData );
						oldPasswordLabel.setText(I18N.translate(PFX + "changePasswordShell.oldPassword.text"));


						//User Name Input field
						final Text oldPassword = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 100;
						oldPassword.setLayoutData( gridData);



						//Password Label
						Label passwordLabel = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						passwordLabel.setLayoutData( gridData );
						passwordLabel.setText(I18N.translate(PFX + "changePasswordShell.newPassword.text"));


						//User Name Input field
						final Text password = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 100;
						password.setLayoutData( gridData);

						//User Name Label
						Label verify_text = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						verify_text.setLayoutData( gridData );
						verify_text.setText(I18N.translate(PFX + "changePasswordShell.verify_newPassword.text"));


						//User Name Input field
						final Text verify = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 100;
						verify.setLayoutData( gridData);

						//Button for Accept
						Button commit = new Button(backup_composite, SWT.PUSH);
						gridData = new GridData(GridData.CENTER);
						gridData.horizontalSpan = 1;
						commit.setLayoutData( gridData);
						commit.setText(I18N.translate("global.accept"));
						commit.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								if(password.getText().equalsIgnoreCase("") ||
										verify.getText().equalsIgnoreCase("") ||
										oldPassword.getText().equalsIgnoreCase("")){

									MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText(I18N.translate("global.error"));
									mb.setMessage(I18N.translate(PFX + "changePasswordShell.error1.message"));
									mb.open();
									return;
								}

								//TODO Veryify password

								/*

								try {
									if(!Plugin.getXMLConfig().getUser(user).verifyPassword(oldPassword.getText())){
										MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
										mb.setText("Error");
										mb.setMessage("Old password is not correct.");
										mb.open();
										oldPassword.setText("");
										return;
									}
								} catch (UserNotFoundException e2) {
									e2.printStackTrace();
									MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText("Error");
									mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
									mb.open();
									return;
								}
								 */

								if(password.getText().equalsIgnoreCase(verify.getText())){
									//TODO add the user to the XMLConfig file
									/*try {
										Plugin.getXMLConfig().getUser(user).setPassword(password.getText());
										Plugin.getXMLConfig().saveConfigFile();
									} catch (IOException e1) {
										e1.printStackTrace();
									} catch (UserNotFoundException e2) {
										MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
										mb.setText("Error");
										mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
										mb.open();

									}*/

									//destroy the shell
									shell.dispose();

									//redraw the userTable
									//GUIMain.redrawTable();

								}else{
									MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText(I18N.translate("global.error"));
									mb.setMessage(I18N.translate(PFX + "changePasswordShell.error2.message"));
									mb.open();
									password.setText("");
									verify.setText("");
									return;
								}

							}
						});


						//Button for Cancel
						Button cancel = new Button(backup_composite, SWT.PUSH);
						gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
						gridData.horizontalSpan = 2;
						cancel.setLayoutData( gridData);
						cancel.setText(I18N.translate("global.cancel"));
						cancel.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								shell.dispose();
							}
						});


						//Key Listener so that the user can just use ESC to cancel
						//in the beginning if they did not want to do this
						userName.addKeyListener(new KeyListener() {
							public void keyPressed(KeyEvent e) {
								//Empty
							}

							public void keyReleased (KeyEvent e) {
								switch (e.character){
								case SWT.ESC:

									shell.dispose();
									break;

								}
							}
						});

						//pack and open shell
						GUI_Utilities.centerShellandOpen(shell);

					}
				});
			}
		};

		addNew_thread.setDaemon(true);
		addNew_thread.run();
	}

	public void editUserInfo(final String user, final boolean isAdmin){
		final Thread editUser_thread = new Thread() {
			public void run() {
				if(RCMain.getRCMain().getDisplay()==null && RCMain.getRCMain().getDisplay().isDisposed())
					return;
				RCMain.getRCMain().getDisplay().asyncExec( new Runnable() {
					public void run() {
						//Shell Initialize

						final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
						if(!Utilities.isOSX)
							shell.setImage(ImageRepository.getImage("plus"));

						//Grid Layout
						GridLayout layout = new GridLayout();
						layout.numColumns = 1;
						shell.setLayout(layout);

						//composite for shell
						Composite backup_composite = new Composite(shell,SWT.NULL);

						//Grid Layout
						layout = new GridLayout();
						layout.numColumns = 3;
						backup_composite.setLayout(layout);

						//shell title
						shell.setText(I18N.translate(PFX + "editUserShell.shell.text"));


						//User Name Label
						Label nameLabel = new Label(backup_composite, SWT.NONE);
						GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						nameLabel.setLayoutData( gridData );
						nameLabel.setText(I18N.translate(PFX + "editUserShell.userName.text"));


						//User Name Input field
						final Text userName = new Text(backup_composite,SWT.BORDER);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 100;
						userName.setLayoutData( gridData);
						userName.setText(user);





						//Combo Stuff

						//combo Label
						Label combo_text = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 1;
						combo_text.setLayoutData( gridData );
						combo_text.setText(I18N.translate(PFX + "editUserShell.userType.text"));

						final Combo combo = new Combo(backup_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
						combo.add(I18N.translate(PFX + "tab.manageuserstab.usertable.user"));
						combo.add(I18N.translate(PFX + "tab.manageuserstab.usertable.administrator"));


						/*                      try {
							if(Plugin.getXMLConfig().getUser(user).checkAccess(User.RIGHTS_ADMIN))
								combo.select(1);
							else
								combo.select(0);
						} catch (UserNotFoundException e2) {
							MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
							mb.setText("Error");
							mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
							mb.open();
							Plugin.addToLog(e2.getMessage());
							e2.printStackTrace();
						}*/

						if(!isAdmin){

							combo.setEnabled(false);
						}


						//---------Directory stuff ------------\\


						//output directory
						Label outputDir_text = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 3;
						outputDir_text.setLayoutData( gridData );
						outputDir_text.setText(I18N.translate(PFX + "editUserShell.outputDir.text"));

						//comp for directory input
						Composite output_comp = new Composite(backup_composite,SWT.NONE);
						output_comp.setLayout(new GridLayout(3,false));

						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 3;
						output_comp.setLayoutData(gridData);



						//output directory input field
						final Text outputDir = new Text(output_comp,SWT.BORDER);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 250;
						outputDir.setLayoutData(gridData);
						/*try {
							outputDir.setText(Plugin.getXMLConfig().getUser(user).getOutputDir());
						} catch (UserNotFoundException e2) {
							MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
							mb.setText("Error");
							mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
							mb.open();
							Plugin.addToLog(e2.getMessage());
							e2.printStackTrace();
						}
						 */
						/*                      //icon for output directory
						Label outputDir_icon = new Label(output_comp, SWT.NONE);
						gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
						gridData.horizontalSpan = 1;
						outputDir_icon.setLayoutData( gridData );
						outputDir_icon.setToolTipText("Click to choose directory");
						outputDir_icon.setImage(ImageRepository.getImage("folder"));
						outputDir_icon.addListener(SWT.MouseDown, new Listener() {
							public void handleEvent(Event e) {
								DirectoryDialog dirDialog = new DirectoryDialog(shell);
								dirDialog.setText("Please Choose Output Directory");
								dirDialog.setFilterPath(System.getProperty("user.dir"));
								String selectedDir = dirDialog.open();

								//need to check if selected dir has files and if so, does it have a comments dir
								if(selectedDir == null){
									return;
								}else{
									outputDir.setText(selectedDir);
								}


							}
						});*/

						//auto import directory
						Label importDir_text = new Label(backup_composite, SWT.NONE);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 3;
						importDir_text.setLayoutData( gridData );
						importDir_text.setText(I18N.translate(PFX + "editUserShell.inputDir.text"));

						//comp for directory input
						Composite importDir_comp = new Composite(backup_composite,SWT.NONE);
						importDir_comp.setLayout(new GridLayout(3,false));

						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 3;
						importDir_comp.setLayoutData(gridData);



						//output directory input field
						final Text importDir = new Text(importDir_comp,SWT.BORDER);
						gridData = new GridData(GridData.FILL_HORIZONTAL);
						gridData.horizontalSpan = 2;
						gridData.widthHint = 250;
						importDir.setLayoutData(gridData);
						/*                      try {
							importDir.setText(Plugin.getXMLConfig().getUser(user).getAutoImportDir());
						} catch (UserNotFoundException e2) {
							MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
							mb.setText("Error");
							mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
							mb.open();
							Plugin.addToLog(e2.getMessage());
							e2.printStackTrace();
						}*/

						/*                      //icon for output directory
						Label importDir_icon = new Label(importDir_comp, SWT.NONE);
						gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
						gridData.horizontalSpan = 1;
						importDir_icon.setLayoutData( gridData );
						importDir_icon.setToolTipText("Click to choose directory");
						importDir_icon.setImage(ImageRepository.getImage("folder"));
						importDir_icon.addListener(SWT.MouseDown, new Listener() {
							public void handleEvent(Event e) {
								DirectoryDialog dirDialog = new DirectoryDialog(shell);
								dirDialog.setText("Please Choose Automatic Import Directory");
								dirDialog.setFilterPath(System.getProperty("user.dir"));
								String selectedDir = dirDialog.open();
								File selectedDir_file = new File(selectedDir);
								//need to check if selected dir has files and if so, does it have a comments dir
								if(selectedDir == null){
									return;
								}else if(!selectedDir_file.exists() || !selectedDir_file.isDirectory()){
									MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText("Error");
									mb.setMessage("Selected Directory does not exist, please choose a valid directory.");
									mb.open();
								}else{
									importDir.setText(selectedDir);
								}


							}
						});*/

						//Button for Accept
						Button commit = new Button(backup_composite, SWT.PUSH);
						gridData = new GridData(GridData.CENTER);
						gridData.horizontalSpan = 1;
						commit.setLayoutData( gridData);
						commit.setText(I18N.translate("global.accept"));
						commit.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								if(userName.getText().equalsIgnoreCase("")      ||
										outputDir.getText().equalsIgnoreCase("")||
										importDir.getText().equalsIgnoreCase("")){
									MessageBox mb = new MessageBox(RCMain.getRCMain().getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText(I18N.translate("global.error"));
									mb.setMessage(I18N.translate(PFX + "editUserShell.error1.message"));
									mb.open();
									return;
								}



								//add the user to the XMLConfig file
								/*try {
										if(!user.equalsIgnoreCase(userName.getText())){
											Plugin.getXMLConfig().changeUsername(user,userName.getText());
										}

										Plugin.getXMLConfig().saveConfigFile();

										User currentUser = Plugin.getXMLConfig().getUser(userName.getText());


										currentUser.setOutputDir(outputDir.getText());
										currentUser.setAutoImportDir(importDir.getText());

										if(combo.getSelectionIndex() != 0)
											currentUser.setRight(User.RIGHTS_ADMIN);


										Plugin.getXMLConfig().saveConfigFile();
									} catch (IOException e1) {
										Plugin.addToLog(e1.toString());
									} catch (UserNotFoundException e2) {
										MessageBox mb = new MessageBox(FireFrogMain.getFFM().getDisplay().getActiveShell(),SWT.ICON_ERROR);
										mb.setText("Error");
										mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
										mb.open();
										Plugin.addToLog(e2.getMessage());
										e2.printStackTrace();
									}*/

								//destroy the shell
								shell.dispose();

								//redraw the userTable
								//GUIMain.redrawTable();

							}

						});


						//Button for Cancel
						Button cancel = new Button(backup_composite, SWT.PUSH);
						gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
						gridData.horizontalSpan = 2;
						cancel.setLayoutData( gridData);
						cancel.setText(I18N.translate("global.cancel"));
						cancel.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								shell.dispose();
							}
						});


						//Key Listener so that the user can just use ESC to cancel
						//in the beginning if they did not want to do this
						userName.addKeyListener(new KeyListener() {
							public void keyPressed(KeyEvent e) {
								//Empty
							}

							public void keyReleased (KeyEvent e) {
								switch (e.character){
								case SWT.ESC:

									shell.dispose();
									break;

								}
							}
						});

						//pack and open shell
						GUI_Utilities.centerShellandOpen(shell);

					}
				});
			}
		};

		editUser_thread.setDaemon(true);
		editUser_thread.run();
	}


	public static void open(final CTabFolder parentTab){
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.asyncExec(new Runnable(){
			public void run() {
				CTabItem[] tabs = parentTab.getItems();
				for(CTabItem tab:tabs){
					if(tab.getText().equalsIgnoreCase(I18N.translate(PFX + "tab.text"))){
						parentTab.setSelection(tab);
						return;
					}
				}
				new ManageUsersTab(parentTab);

			}

		});
	}

}//EOF
