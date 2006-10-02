/*
 * Created on Nov 19, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.plugin.gui;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class GUIMain {



	//The userTable
	private static Table userTable;

	/**
	 * Static method to put all the graphics on the composite
	 * @param composite
	 */
	public static void open(Composite composite){
		final User currentUser;
		try {
			currentUser = Plugin.getXMLConfig().getUser(Plugin.LOGGED_IN_USER);
		} catch (UserNotFoundException e2) {

			Plugin.addToLog(e2.toString());
			e2.printStackTrace();

			MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
			mb.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("General.UserNotFoundError.MessageBox.title"));
			mb.setMessage(Plugin.getLocaleUtilities().getLocalisedMessageText("General.UserNotFoundError.MessageBox.message"));
			mb.open();
			//Destroy the login on the main composite
			Control[] controls = View.composite.getChildren();
			for(int i = 0; i < controls.length; i++){
				controls[i].dispose();
			}

			//Redraw the Composite
			GUILogin.openLogin(View.composite);
			return;
		}
		final boolean isAdmin = currentUser.checkAccess(RemoteConstants.RIGHTS_ADMIN);

		//------------UserTable and it's toolbar-----------\\

		//Group for both the toolbar and the usertable
		Group userTable_group = new Group(composite, SWT.NULL);
		userTable_group.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = 5;
		userTable_group.setLayoutData(gridData);


		//Set the text of the Group to the current logged in user and their level
		String rights;
		if(currentUser.checkAccess(RemoteConstants.RIGHTS_ADMIN)){
			rights = "Administrator: ";
		}else{
			rights = "User: ";
		}

		userTable_group.setText("  " + Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable_group.title") + " " + rights + currentUser.getUsername() + "  ");
		//Toolbar for the usertable
		ToolBar userTable_toolbar = new ToolBar(userTable_group,SWT.FLAT | SWT.HORIZONTAL);

		if(isAdmin){

//          Add User ToolItem
			ToolItem addUser = new ToolItem(userTable_toolbar,SWT.PUSH);
			addUser.setImage(ImageRepository.getImage("add"));
			addUser.setToolTipText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable_toolbar.addUser"));

			//Delete User ToolItem
			final ToolItem deleteUser = new ToolItem(userTable_toolbar, SWT.PUSH);
			deleteUser.setImage(ImageRepository.getImage("delete"));
			deleteUser.setToolTipText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable_toolbar.deleteUser"));

			//Listener for add user
			final Listener addNew_listener = new Listener() {
				public void handleEvent(Event e) {
					Plugin.getDisplay().asyncExec(new Runnable (){
						public void run () {

						   GUIUserUtils utils = new GUIUserUtils();
						   utils.addNewUser();


						}
					});
				}
			};

			//Add listener to the toolitem
			addUser.addListener(SWT.Selection,addNew_listener);

			//Listener for delete user
			final Listener deleteUser_listener = new Listener() {
				public void handleEvent(Event e) {
					Plugin.getDisplay().asyncExec(new Runnable (){
						public void run () {
							//Pull the selected items
							TableItem[] items = userTable.getSelection();

							//Check if only one
							if(items.length > 0 ){
								if(items[0].getText(0).equalsIgnoreCase(currentUser.getUsername())){
									MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
									mb.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("General.CannotDeleteYourselfError.MessageBox.title"));
									mb.setMessage(Plugin.getLocaleUtilities().getLocalisedMessageText("General.CannotDeleteYourselfError.MessageBox.message"));
									mb.open();
									return;
								}else{
									GUIUserUtils utils = new GUIUserUtils();
									utils.deleteUser(items[0].getText(0));
								}

							}else return;



						}
					});
				}
			};

			//Add listener to the toolitem
			deleteUser.addListener(SWT.Selection,deleteUser_listener);


			//Admin Settings Item
			ToolItem admin = new ToolItem(userTable_toolbar, SWT.PUSH);
			admin.setImage(ImageRepository.getImage("settings"));
			admin.setToolTipText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable_toolbar.admin"));
			admin.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					Plugin.getDisplay().asyncExec(new Runnable (){
						public void run () {
							GUIAdminShells.openConfigDialog();

						}
					});


				}
			});

			// Separator
			new ToolItem(userTable_toolbar, SWT.SEPARATOR);


		}

		//Logout Item
		ToolItem logout = new ToolItem(userTable_toolbar, SWT.PUSH);
		logout.setImage(ImageRepository.getImage("logout"));
		logout.setToolTipText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable_toolbar.logout"));
		logout.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Plugin.LOGGED_IN_USER = null;
				Plugin.getDisplay().asyncExec(new Runnable (){
					public void run () {

						//Destroy the login on the main composite
						Control[] controls = View.composite.getChildren();
						for(int i = 0; i < controls.length; i++){
							controls[i].dispose();
						}

						//Redraw the Composite
						GUILogin.openLogin(View.composite);


					}
				});


			}
		});

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
		userName.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.userName"));
		userName.setWidth(150);

		TableColumn userType = new TableColumn(userTable,SWT.CENTER);
		userType.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.userType"));
		userType.setWidth(100);

		TableColumn downloadSlots = new TableColumn(userTable,SWT.CENTER);
		downloadSlots.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.downloadSlots"));
		//downloadSlots.setWidth(100);
		downloadSlots.pack();

		TableColumn downloadCount = new TableColumn(userTable,SWT.CENTER);
		downloadCount.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.downloadCount"));
		//downloadCount.setWidth(100);
		downloadCount.pack();

		TableColumn outputDir = new TableColumn(userTable,SWT.LEFT);
		outputDir.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.outputDir"));
		outputDir.setWidth(300);

		TableColumn inputDir = new TableColumn(userTable,SWT.LEFT);
		inputDir.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.inputDir"));
		inputDir.setWidth(300);

		redrawTable();


		//SetData listener for the userTable
		userTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event e) {
				//pull the item
				TableItem item = (TableItem)e.item;

				//get the index of the item
				int index = userTable.indexOf(item);

				//set the data of the item based on the array
				String[] users = Plugin.getXMLConfig().getUserList();

				if(users == null || users.length == 0) return;
				UserTableItemAdapter utia;
				try {
					utia = new UserTableItemAdapter(users[index],Plugin.getXMLConfig().getUser(Plugin.LOGGED_IN_USER));
					item = utia.getTableItem(item);

					//gray if needed
					if(index%2!=0){
						item.setBackground(ColorUtilities.getBackgroundColor());
					}

				} catch (UserNotFoundException e1) {
					 e1.printStackTrace();
				}

			}
		});

		//popup menu for userTable
		Menu popupmenu_table = new Menu(userTable);

		final MenuItem changePassword = new MenuItem(popupmenu_table, SWT.PUSH);
		changePassword.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.popupmenu.changePassword"));
		changePassword.setEnabled(false);
		changePassword.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e){
				TableItem[] items = userTable.getSelection();
				if(items.length == 1){
					GUIUserUtils guiUtils = new GUIUserUtils();
					guiUtils.changePassword(items[0].getText(0));
				}
			}
		});

		final MenuItem editUser = new MenuItem(popupmenu_table, SWT.PUSH);
		editUser.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUIMain.userTable.popupmenu.editUser"));
		editUser.setEnabled(false);
		editUser.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e){
				TableItem[] items = userTable.getSelection();
				if(items.length == 1){
					/*GUIUserUtils guiUtils = new GUIUserUtils();
					guiUtils.editUserInfo(items[0].getText(0),isAdmin);*/

					try {
						User user = Plugin.getXMLConfig().getUser(items[0].getText(0));
						if(currentUser.checkRight(RemoteConstants.RIGHTS_ADMIN) ||
								user.getUsername().equalsIgnoreCase(currentUser.getUsername())){
							GUIEditUser guieu = new GUIEditUser();
							guieu.open(user);
						}

					} catch (UserNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
		});


		popupmenu_table.addMenuListener(new MenuListener(){
			public void menuHidden(MenuEvent arg0) {


			}

			public void menuShown(MenuEvent arg0) {
				changePassword.setEnabled(false);

				TableItem[] item = userTable.getSelection();
				if(item.length == 1){
					if(!isAdmin && item[0].getText(0).equals(currentUser.getUsername())){
						changePassword.setEnabled(true);
						editUser.setEnabled(true);
					}else if(isAdmin){
						changePassword.setEnabled(true);
						editUser.setEnabled(true);
					}else{
						changePassword.setEnabled(false);
						editUser.setEnabled(false);
					}



				}


			}
		});

		userTable.setMenu(popupmenu_table);


		View.composite.layout();
	}






	/**
	 * Redraws the userTable.. since it is virtual, we need to repopulate it
	 * each time the user array is modified
	 *
	 */
	public static void redrawTable(){
		// Reset the data so that the SWT.Virtual picks up the array
		Plugin.getDisplay().syncExec(new Runnable() {
			public void run() {
				if (userTable == null || userTable.isDisposed())
					return;

				try{
					userTable.setItemCount(Plugin.getXMLConfig().getUserList().length);
				}catch (Exception e){
					userTable.setItemCount(0);
				}

				userTable.clearAll();



			}
		});
	}
}
