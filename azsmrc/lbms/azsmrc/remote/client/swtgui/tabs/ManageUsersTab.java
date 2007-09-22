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
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.azsmrc.shared.UserNotFoundException;
import lbms.tools.flexyconf.ContentProvider;
import lbms.tools.flexyconf.FCInterface;
import lbms.tools.flexyconf.FlexyConfiguration;
import lbms.tools.flexyconf.I18NProvider;
import lbms.tools.flexyconf.swt.SWTMenu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom.Element;

public class ManageUsersTab {
	private Table				userTable;
	private UserManager			userManager;
	private CTabItem			manageUsersTab;

	// I18N prefix
	public static final String	PFX	= "tab.manageuserstab.";

	private ManageUsersTab(CTabFolder parentTab) {
		manageUsersTab = new CTabItem(parentTab, SWT.CLOSE);
		manageUsersTab.setText(I18N.translate(PFX + "tab.text"));

		userManager = RCMain.getRCMain().getClient().getUserManager();
		userManager.update();

		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		open(parent);

		// The Client update listener
		final ClientUpdateListener cul = new ClientUpdateListener() {

			public void update(long updateSwitches) {
				if ((updateSwitches & Constants.UPDATE_USERS) != 0) {
					redrawTable();
				}

				if ((updateSwitches & Constants.UPDATE_ADVANCED_STATS) != 0) {

				}

				if ((updateSwitches & Constants.UPDATE_LIST_TRANSFERS) != 0) {

				}

			}
		};

		// Add the CUL to the Client
		RCMain.getRCMain().getClient().addClientUpdateListener(cul);

		// Listen for when tab is closed and make sure to remove the client
		// update listener
		manageUsersTab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				RCMain.getRCMain().getClient().removeClientUpdateListener(cul);
			}
		});

		manageUsersTab.setControl(parent);
		parentTab.setSelection(manageUsersTab);
	}

	public void open(Composite composite) {

		// ------------UserTable and it's toolbar-----------\\

		// Group for both the toolbar and the usertable
		Group userTable_group = new Group(composite, SWT.NULL);
		userTable_group.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalSpan = 5;
		userTable_group.setLayoutData(gridData);

		userTable_group.setText(I18N.translate(PFX + "userTable_group.text")
				+ "  " + RCMain.getRCMain().getClient().getUsername());
		// Toolbar for the usertable
		ToolBar userTable_toolbar = new ToolBar(userTable_group, SWT.FLAT
				| SWT.HORIZONTAL);

		// Add User ToolItem
		ToolItem addUser = new ToolItem(userTable_toolbar, SWT.PUSH);
		addUser.setImage(ImageRepository.getImage("add"));
		addUser.setToolTipText(I18N.translate(PFX + "toolbar.add.tooltip"));

		// Listener for add user
		final Listener addNew_listener = new Listener() {
			public void handleEvent(Event e) {
				RCMain.getRCMain().getDisplay().asyncExec(
						new SWTSafeRunnable() {
							@Override
							public void runSafe() {
								addNewUser();
							}
						});
			}
		};

		// Add listener to the toolitem
		addUser.addListener(SWT.Selection, addNew_listener);

		// Listener for delete user
		final Listener deleteUser_listener = new Listener() {
			public void handleEvent(Event e) {
				RCMain.getRCMain().getDisplay().asyncExec(
						new SWTSafeRunnable() {
							@Override
							public void runSafe() {
								// Pull the selected items
								TableItem[] items = userTable.getSelection();

								// Check if only one
								if (items.length > 0) {
									if (items[0].getText(0).equalsIgnoreCase(
											RCMain.getRCMain().getClient().getUsername())) {
										MessageBox mb = new MessageBox(
												RCMain.getRCMain().getDisplay().getActiveShell(),
												SWT.ICON_ERROR);
										mb.setText(I18N.translate("global.error"));
										mb.setMessage(I18N.translate(PFX
												+ "toolbar.deleteuser.messabox.message"));
										mb.open();
										return;
									} else {
										deleteUser(items[0].getText(0));
									}

								} else {
									return;
								}

							}
						});
			}
		};

		userTable_toolbar.pack();

		// --userTable
		userTable = new Table(userTable_group, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalSpan = 5;
		userTable.setLayoutData(gridData);

		// Columns for the userTable
		userTable.setHeaderVisible(true);

		TableColumn userName = new TableColumn(userTable, SWT.LEFT);
		userName.setText(I18N.translate(PFX + "usertable.column.name.text"));
		userName.setWidth(150);

		TableColumn userType = new TableColumn(userTable, SWT.CENTER);
		userType.setText(I18N.translate(PFX + "usertable.column.type.text"));
		userType.setWidth(100);

		TableColumn downloadSlots = new TableColumn(userTable, SWT.CENTER);
		downloadSlots.setText(I18N.translate(PFX
				+ "usertable.column.download_slots.text"));
		// downloadSlots.setWidth(100);
		downloadSlots.pack();

		TableColumn outputDir = new TableColumn(userTable, SWT.LEFT);
		outputDir.setText(I18N.translate(PFX
				+ "usertable.column.outputdir.text"));
		outputDir.setWidth(300);

		TableColumn inputDir = new TableColumn(userTable, SWT.LEFT);
		inputDir.setText(I18N.translate(PFX + "usertable.column.inputdir.text"));
		inputDir.setWidth(300);

		// SetData listener for the userTable
		userTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event e) {
				// pull the item
				TableItem item = (TableItem) e.item;

				// get the index of the item
				int index = userTable.indexOf(item);

				try {

					User[] users = userManager.getUsers();
					item.setText(0, users[index].getUsername());

					if (users[index].getRights() == RemoteConstants.RIGHTS_ADMIN) {
						item.setText(1, I18N.translate(PFX
								+ "usertable.administrator"));
					} else {
						item.setText(1, I18N.translate(PFX + "usertable.user"));
					}

					item.setText(2,
							Integer.toString(users[index].getDownloadSlots()));
					item.setText(3, users[index].getOutputDir());
					item.setText(4, users[index].getAutoImportDir());

					// gray if needed
					if (index % 2 != 0) {
						item.setBackground(ColorUtilities.getBackgroundColor());
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});

		userTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1) {
					if (userTable.getItem(new Point(e.x, e.y)) == null) {
						userTable.deselectAll();
					} else {
						TableItem[] items = userTable.getSelection();
						if (items.length == 1) {
							EditUserDialog.open(items[0].getText(0));
						}
					}
				}
			}
		});

		userTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					if (userTable.getItem(new Point(e.x, e.y)) == null) {
						userTable.deselectAll();
					}
				}
			}
		});

		// popup menu for userTable
		Menu popupmenu_table = new Menu(userTable);

		final MenuItem changePassword = new MenuItem(popupmenu_table, SWT.PUSH);
		changePassword.setText(I18N.translate(PFX
				+ "usertable.menu.changepassword.text"));
		changePassword.setEnabled(false);
		changePassword.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TableItem[] items = userTable.getSelection();
				if (items.length == 1) {
					changePassword(items[0].getText(0));
				}
			}
		});

		final MenuItem editUser = new MenuItem(popupmenu_table, SWT.PUSH);
		editUser.setText(I18N.translate(PFX + "usertable.menu.edituser.text"));
		editUser.setEnabled(false);
		editUser.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TableItem[] items = userTable.getSelection();
				if (items.length == 1) {
					EditUserDialog.open(items[0].getText(0));
				}
			}
		});

		new MenuItem(popupmenu_table, SWT.SEPARATOR);

		final MenuItem deleteUser = new MenuItem(popupmenu_table, SWT.PUSH);
		deleteUser.setText(I18N.translate(PFX
				+ "usertable.menu.deleteuser.text"));
		deleteUser.setImage(ImageRepository.getImage("delete"));
		deleteUser.addListener(SWT.Selection, deleteUser_listener);

		popupmenu_table.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent arg0) {

			}

			public void menuShown(MenuEvent arg0) {
				changePassword.setEnabled(false);

				TableItem[] item = userTable.getSelection();
				if (item.length == 1) {
					changePassword.setEnabled(true);
					editUser.setEnabled(true);
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
	public void redrawTable() {
		// Reset the data so that the SWT.Virtual picks up the array
		RCMain.getRCMain().getDisplay().syncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe() {
				if (userTable == null || userTable.isDisposed()) {
					return;
				}

				try {
					userTable.setItemCount(userManager.getUsers().length);
				} catch (Exception e) {
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
	public void addNewUser() {
		if (RCMain.getRCMain().getDisplay() == null
				&& RCMain.getRCMain().getDisplay().isDisposed()) {
			return;
		}
		RCMain.getRCMain().getDisplay().asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe() {
				// Shell Initialize

				final Shell shell = new Shell(SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				if (!Utilities.isOSX) {
					shell.setImage(ImageRepository.getImage("add"));
				}

				// Grid Layout
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				shell.setLayout(layout);

				// composite for shell
				Composite backup_composite = new Composite(shell, SWT.NULL);

				// Grid Layout
				layout = new GridLayout();
				layout.numColumns = 3;
				backup_composite.setLayout(layout);

				// shell title
				shell.setText(I18N.translate(PFX + "addnewuserShell.shell.text"));

				// User Name Label
				Label nameLabel = new Label(backup_composite, SWT.NONE);
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				nameLabel.setLayoutData(gridData);
				nameLabel.setText(I18N.translate(PFX
						+ "addnewuserShell.newuser.text"));

				// User Name Input field
				final Text userName = new Text(backup_composite, SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				userName.setLayoutData(gridData);

				// Password Label
				Label passwordLabel = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				passwordLabel.setLayoutData(gridData);
				passwordLabel.setText(I18N.translate(PFX
						+ "addnewuserShell.password.text"));

				// User Name Input field
				final Text password = new Text(backup_composite, SWT.BORDER
						| SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				password.setLayoutData(gridData);

				// verify password Label
				Label verify_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				verify_text.setLayoutData(gridData);
				verify_text.setText(I18N.translate(PFX
						+ "addnewuserShell.verify_password.text"));

				// verify password field
				final Text verify = new Text(backup_composite, SWT.BORDER
						| SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				verify.setLayoutData(gridData);

				// Combo Stuff

				// combo Label
				Label combo_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				combo_text.setLayoutData(gridData);
				combo_text.setText(I18N.translate(PFX
						+ "addnewuserShell.userType.text"));

				final Combo combo = new Combo(backup_composite, SWT.DROP_DOWN
						| SWT.READ_ONLY);
				combo.add(I18N.translate(PFX + "usertable.user"));
				combo.add(I18N.translate(PFX + "usertable.administrator"));

				combo.select(0);

				// ---------Directory stuff ------------\\
				Label blank_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				blank_text.setLayoutData(gridData);
				blank_text.setText("");

				Label dir_text = new Label(backup_composite, SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				dir_text.setLayoutData(gridData);
				dir_text.setText(" "
						+ I18N.translate(PFX
								+ "addnewuserShell.dir_reminder.text") + " ");
				dir_text.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(
						SWT.COLOR_GRAY));

				// output directory
				Label outputDir_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				outputDir_text.setLayoutData(gridData);
				outputDir_text.setText(I18N.translate(PFX
						+ "addnewuserShell.outputDir.text"));

				// comp for directory input
				Composite output_comp = new Composite(backup_composite,
						SWT.NONE);
				output_comp.setLayout(new GridLayout(3, false));

				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				output_comp.setLayoutData(gridData);

				// output directory input field
				final Text outputDir = new Text(output_comp, SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 250;
				outputDir.setLayoutData(gridData);

				// auto import directory
				Label importDir_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				importDir_text.setLayoutData(gridData);
				importDir_text.setText(I18N.translate(PFX
						+ "addnewuserShell.inputDir.text"));

				// comp for directory input
				Composite importDir_comp = new Composite(backup_composite,
						SWT.NONE);
				importDir_comp.setLayout(new GridLayout(3, false));

				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 3;
				importDir_comp.setLayoutData(gridData);

				// output directory input field
				final Text importDir = new Text(importDir_comp, SWT.BORDER);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 250;
				importDir.setLayoutData(gridData);

				// Button for Accept
				Button commit = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.CENTER);
				gridData.horizontalSpan = 1;
				commit.setLayoutData(gridData);
				commit.setText(I18N.translate("global.accept"));
				commit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						if (userName.getText().equalsIgnoreCase("")
								|| password.getText().equalsIgnoreCase("")
								|| verify.getText().equalsIgnoreCase("")) {
							MessageBox mb = new MessageBox(
									RCMain.getRCMain().getDisplay().getActiveShell(),
									SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX
									+ "addnewuserShell.messagebox.message"));
							mb.open();
							return;
						}

						if (password.getText().equalsIgnoreCase(
								verify.getText())) {
							try {
								// System.out.println("Password to send: "+
								// password.getText());
								userManager.addUserUser(userName.getText(),
										password.getText(),
										importDir.getText(),
										outputDir.getText(), 1,
										combo.getSelectionIndex());

							} catch (DuplicatedUserException e1) {
								MessageBox mb = new MessageBox(
										RCMain.getRCMain().getDisplay().getActiveShell(),
										SWT.ICON_ERROR);
								mb.setText(I18N.translate("global.error"));
								mb.setMessage(I18N.translate(PFX
										+ "addnewuserShell.duplicateError.messagebox.message"));
								mb.open();
								return;
							}

							// destroy the shell
							shell.dispose();

							// redraw the userTable
							redrawTable();

						} else {
							MessageBox mb = new MessageBox(
									RCMain.getRCMain().getDisplay().getActiveShell(),
									SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX
									+ "addnewuserShell.pwIdenticalError.messagebox.message"));
							mb.open();
							password.setText("");
							verify.setText("");
							return;
						}

					}
				});

				// Button for Cancel
				Button cancel = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gridData.horizontalSpan = 2;
				cancel.setLayoutData(gridData);
				cancel.setText(I18N.translate("global.cancel"));
				cancel.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						shell.dispose();
					}
				});

				// Key Listener so that the user can just use ESC to cancel
				// in the beginning if they did not want to do this
				userName.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
						// Empty
					}

					public void keyReleased(KeyEvent e) {
						switch (e.character) {
						case SWT.ESC:

							shell.dispose();
							break;

						}
					}
				});

				// pack and open shell
				GUI_Utilities.centerShellandOpen(shell);

			}
		});

	}

	/**
	 * GUI code to delete a user
	 * 
	 * @param userName
	 * @return
	 */
	public boolean deleteUser(String userName) {
		MessageBox mb = new MessageBox(
				RCMain.getRCMain().getDisplay().getActiveShell(), SWT.YES
						| SWT.NO | SWT.ICON_QUESTION);
		mb.setText(I18N.translate(PFX + "deleteUser.title.text"));
		mb.setMessage(I18N.translate(PFX + "deleteUser.message.text") + " "
				+ userName);
		int response = mb.open();
		switch (response) {

		case SWT.YES:
			try {
				userManager.removeUser(userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// redraw the userTable
			redrawTable();
			return true;

		case SWT.NO:
			break;

		}
		return false;
	}

	public void changePassword(final String user) {
		if (RCMain.getRCMain().getDisplay() == null
				&& RCMain.getRCMain().getDisplay().isDisposed()) {
			return;
		}
		RCMain.getRCMain().getDisplay().asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe() {
				// Shell Initialize

				final Shell shell = new Shell(SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				if (!Utilities.isOSX) {
					shell.setImage(ImageRepository.getImage("plus"));
				}

				// Grid Layout
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				shell.setLayout(layout);

				// composite for shell
				Composite backup_composite = new Composite(shell, SWT.NULL);

				// Grid Layout
				layout = new GridLayout();
				layout.numColumns = 3;
				backup_composite.setLayout(layout);

				// shell title
				shell.setText(I18N.translate(PFX
						+ "changePasswordShell.shell.text"));

				// User Name Label
				Label nameLabel = new Label(backup_composite, SWT.NONE);
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				nameLabel.setLayoutData(gridData);
				nameLabel.setText(I18N.translate(PFX
						+ "changePasswordShell.usertochange.text")
						+ " ");

				// User Name Input field
				final Label userName = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				userName.setLayoutData(gridData);
				userName.setText(user);

				// Password Label
				Label passwordLabel = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				passwordLabel.setLayoutData(gridData);
				passwordLabel.setText(I18N.translate(PFX
						+ "changePasswordShell.newPassword.text"));

				// User Name Input field
				final Text password = new Text(backup_composite, SWT.BORDER
						| SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				password.setLayoutData(gridData);

				// User Name Label
				Label verify_text = new Label(backup_composite, SWT.NONE);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 1;
				verify_text.setLayoutData(gridData);
				verify_text.setText(I18N.translate(PFX
						+ "changePasswordShell.verify_newPassword.text"));

				// User Name Input field
				final Text verify = new Text(backup_composite, SWT.BORDER
						| SWT.PASSWORD);
				gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				gridData.widthHint = 100;
				verify.setLayoutData(gridData);

				// Button for Accept
				Button commit = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.CENTER);
				gridData.horizontalSpan = 1;
				commit.setLayoutData(gridData);
				commit.setText(I18N.translate("global.accept"));
				commit.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						if (password.getText().equalsIgnoreCase("")
								|| verify.getText().equalsIgnoreCase("")) {

							MessageBox mb = new MessageBox(
									RCMain.getRCMain().getDisplay().getActiveShell(),
									SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX
									+ "changePasswordShell.error1.message"));
							mb.open();
							return;
						}

						if (password.getText().equalsIgnoreCase(
								verify.getText())) {
							// Everything is a go, so commit the change in the
							// passwords
							try {
								userManager.getUser(user).setPassword(
										password.getText());

								if (userManager.getUser(user).equals(
										userManager.getActiveUser())) {
									MessageBox mb = new MessageBox(
											RCMain.getRCMain().getDisplay().getActiveShell(),
											SWT.ICON_INFORMATION);
									mb.setText(I18N.translate(PFX
											+ "changePasswordShell.activeuser_conflict.title"));
									mb.setMessage(I18N.translate(PFX
											+ "changePasswordShell.activeuser_conflict.message"));
									mb.open();

									RCMain.getRCMain().disconnect();
									RCMain.getRCMain().getMainWindow().setGUItoLoggedOut();
									shell.dispose();
									manageUsersTab.dispose();
								}
							} catch (UserNotFoundException e2) {
								e2.printStackTrace();
								MessageBox mb = new MessageBox(
										RCMain.getRCMain().getDisplay().getActiveShell(),
										SWT.ICON_ERROR);
								mb.setText(I18N.translate("global.error"));
								mb.setMessage(I18N.translate(PFX
										+ "changePasswordShell.error4.message"));
								mb.open();
								shell.dispose();
								redrawTable();
							}

							// destroy the shell
							shell.dispose();

							// redraw the userTable
							redrawTable();

						} else {
							MessageBox mb = new MessageBox(
									RCMain.getRCMain().getDisplay().getActiveShell(),
									SWT.ICON_ERROR);
							mb.setText(I18N.translate("global.error"));
							mb.setMessage(I18N.translate(PFX
									+ "changePasswordShell.error2.message"));
							mb.open();
							password.setText("");
							verify.setText("");
							return;
						}

					}
				});

				// Button for Cancel
				Button cancel = new Button(backup_composite, SWT.PUSH);
				gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gridData.horizontalSpan = 2;
				cancel.setLayoutData(gridData);
				cancel.setText(I18N.translate("global.cancel"));
				cancel.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						shell.dispose();
					}
				});

				// Key Listener so that the user can just use ESC to cancel
				// in the beginning if they did not want to do this
				userName.addKeyListener(new KeyListener() {
					public void keyPressed(KeyEvent e) {
						// Empty
					}

					public void keyReleased(KeyEvent e) {
						switch (e.character) {
						case SWT.ESC:

							shell.dispose();
							break;

						}
					}
				});

				// pack and open shell
				GUI_Utilities.centerShellandOpen(shell);

			}
		});

	}

	public static void open(final CTabFolder parentTab) {
		Display display = RCMain.getRCMain().getDisplay();
		if (display == null) {
			return;
		}
		display.asyncExec(new SWTSafeRunnable() {
			@Override
			public void runSafe() {
				CTabItem[] tabs = parentTab.getItems();
				for (CTabItem tab : tabs) {
					if (tab.getText().equalsIgnoreCase(
							I18N.translate(PFX + "tab.text"))) {
						parentTab.setSelection(tab);
						return;
					}
				}
				new ManageUsersTab(parentTab);

			}

		});
	}

	private static class EditUserDialog {

		private boolean	isDirty	= false;
		private String	username;
		private String	importDir;
		private String	outDir;
		private int		rights;
		private User	user;

		private EditUserDialog(String userN) {
			editUserInfo(userN);
		}

		public void editUserInfo(final String userN) {
			if (RCMain.getRCMain().getDisplay() == null
					&& RCMain.getRCMain().getDisplay().isDisposed()) {
				return;
			}
			RCMain.getRCMain().getDisplay().asyncExec(new SWTSafeRunnable() {
				@Override
				public void runSafe() {
					// Shell Initialize
					User utemp = null;
					try {
						utemp = RCMain.getRCMain().getClient().getUserManager().getUser(
								userN);
					} catch (UserNotFoundException e1) {
						MessageBox mb = new MessageBox(
								RCMain.getRCMain().getDisplay().getActiveShell(),
								SWT.ICON_ERROR);
						mb.setText(I18N.translate("global.error"));
						mb.setMessage(I18N.translate(PFX
								+ "editUserShell.error2.message"));
						mb.open();
						e1.printStackTrace();
						return;
					}
					user = utemp;
					final Shell shell = new Shell(SWT.DIALOG_TRIM
							| SWT.APPLICATION_MODAL);
					if (!Utilities.isOSX) {
						shell.setImage(ImageRepository.getImage("plus"));
					}

					// shell title
					shell.setText(I18N.translate(PFX
							+ "editUserShell.shell.text"));

					// Grid Layout
					GridLayout layout = new GridLayout();
					layout.numColumns = 1;
					shell.setLayout(layout);

					final SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
					GridData gridData = new GridData(GridData.FILL_BOTH);
					gridData.widthHint = 600;
					gridData.heightHint = 400;
					sash.setLayoutData(gridData);
					sash.setLayout(new GridLayout(1, false));

					// Tree on left side
					final Tree tree = new Tree(sash, SWT.BORDER | SWT.H_SCROLL);
					gridData = new GridData(GridData.FILL_BOTH);
					gridData.horizontalSpan = 1;
					tree.setLayoutData(gridData);

					final ScrolledComposite sc = new ScrolledComposite(sash,
							SWT.V_SCROLL);

					final Composite cOptions = new Composite(sc, SWT.BORDER);
					gridData = new GridData(GridData.FILL_BOTH);
					gridData.horizontalSpan = 1;
					cOptions.setLayoutData(gridData);
					cOptions.setLayout(new GridLayout(2, false));

					sc.setContent(cOptions);
					sc.setExpandVertical(true);
					sc.setExpandHorizontal(true);
					sc.addControlListener(new ControlAdapter() {
						@Override
						public void controlResized(ControlEvent e) {
							sc.setMinSize(cOptions.computeSize(SWT.DEFAULT,
									SWT.DEFAULT));
						}
					});

					final TreeItem item1 = new TreeItem(tree, SWT.NULL);
					item1.setText("General Settings");

					tree.setSelection(item1);
					addGeneralItems(cOptions);

					tree.addSelectionListener(new SelectionListener() {

						public void widgetDefaultSelected(SelectionEvent arg0) {
						}

						public void widgetSelected(SelectionEvent arg0) {
							TreeItem[] selections = tree.getSelection();
							if (selections.length != 1) {
								return;
							}
							if (selections[0].equals(item1)) {
								// If true then this is the General Settings

								// First remove all from cOptions
								Control[] controls = cOptions.getChildren();
								for (Control control : controls) {
									control.dispose();
								}

								// populate cOptions
								addGeneralItems(cOptions);

							} else {
								// Flexyconfig stuff..
							}
						}

					});

					Composite backup_composite = new Composite(shell, SWT.None);
					gridData = new GridData(GridData.FILL_HORIZONTAL);
					backup_composite.setLayoutData(gridData);
					layout = new GridLayout(2, false);
					backup_composite.setLayout(layout);

					// Button for Accept
					Button commit = new Button(backup_composite, SWT.PUSH);
					gridData = new GridData(GridData.CENTER);
					gridData.horizontalSpan = 1;
					commit.setLayoutData(gridData);
					commit.setText(I18N.translate("global.accept"));
					final User u = user;
					commit.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							if (!isDirty) {
								shell.dispose();
							} else {
								RCMain.getRCMain().getClient().transactionStart();
								if (importDir != null) {
									u.setAutoImportDir(importDir);
								}
								if (outDir != null) {
									u.setOutputDir(outDir);
								}
								if (username != null) {
									u.setUsername(username);
								}
								switch (rights) {
								case (0):
									u.unsetRight(RemoteConstants.RIGHTS_ADMIN);
								case (1):
									u.setRight(RemoteConstants.RIGHTS_ADMIN);
								}
								RCMain.getRCMain().getClient().transactionCommit();
							}
							shell.dispose();
						}

					});

					// Button for Cancel
					Button cancel = new Button(backup_composite, SWT.PUSH);
					gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
					cancel.setLayoutData(gridData);
					cancel.setText(I18N.translate("global.cancel"));
					cancel.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							MessageBox mb = new MessageBox(
									RCMain.getRCMain().getDisplay().getActiveShell(),
									SWT.ICON_WARNING);
							mb.setText(I18N.translate("global.warning"));
							mb.setMessage(I18N.translate(PFX
									+ "editUserShell.warning.uncommitedchanges.message"));
							shell.dispose();
						}
					});
					Element fce = RCMain.getRCMain().getClient().getRemoteInfo().getAzSMRCPluginSupportFlexyConf();
					if (fce != null) {
						FlexyConfiguration fc = new FlexyConfiguration(fce);
						FCInterface fci = fc.getFCInterface();
						fci.setContentProvider(new ContentProvider() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see lbms.tools.flexyconf.ContentProvider#getDefaultValue(java.lang.String,
							 *      int)
							 */
							public String getDefaultValue(String key, int type) {
								return user.getProperty(key);
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see lbms.tools.flexyconf.ContentProvider#getValue(java.lang.String,
							 *      int)
							 */
							public String getValue(String key, int type) {
								String v = user.getProperty(key);
								return (v == null) ? "" : v;
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see lbms.tools.flexyconf.ContentProvider#setValue(java.lang.String,
							 *      java.lang.String, int)
							 */
							public void setValue(String key, String value,
									int type) {
								System.out.println("Set Property for user ["
										+ user + "]: " + key + " -> " + value);
								user.setProperty(key, value);
								isDirty = true;
							}
						});
						fci.setI18NProvider(new I18NProvider() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see lbms.tools.flexyconf.I18NProvider#translate(java.lang.String)
							 */
							public String translate(String key) {
								return I18N.translate(key);
							}
						});
						fc.reset(); // reset all values before creating menu
						SWTMenu fcm = new SWTMenu(fc, tree, cOptions);
						fcm.addAsRoot();

					}

					// pack and open shell
					GUI_Utilities.centerShellandOpen(shell);

				}
			});

		}

		private void addGeneralItems(Composite composite) {

			// Grid Layout
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			composite.setLayout(layout);

			// User Name Label
			Label nameLabel = new Label(composite, SWT.NONE);
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 1;
			nameLabel.setLayoutData(gridData);
			nameLabel.setText(I18N.translate(PFX
					+ "editUserShell.userName.text"));

			// User Name Input field
			final Text userName = new Text(composite, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 100;
			userName.setLayoutData(gridData);
			userName.setText(user.getUsername());

			// Combo Stuff

			// combo Label
			Label combo_text = new Label(composite, SWT.NONE);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 1;
			combo_text.setLayoutData(gridData);
			combo_text.setText(I18N.translate(PFX
					+ "editUserShell.userType.text"));

			final Combo combo = new Combo(composite, SWT.DROP_DOWN
					| SWT.READ_ONLY);
			combo.add(I18N.translate(PFX + "usertable.user"));
			combo.add(I18N.translate(PFX + "usertable.administrator"));

			if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
				combo.select(1);
			} else {
				combo.select(0);
			}

			combo.addModifyListener(new ModifyListener() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
				 */
				public void modifyText(ModifyEvent e) {
					rights = combo.getSelectionIndex();
					isDirty = true;
				}
			});

			final Button rights1 = new Button(composite, SWT.CHECK);
			rights1.setText("User has right to Force Start torrents");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			rights1.setLayoutData(gd);
			rights1.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
						isDirty = true;
						if (rights1.getSelection()) {
							user.setRight(RemoteConstants.RIGHTS_FORCESTART);
						} else {
							user.unsetRight(RemoteConstants.RIGHTS_FORCESTART);
						}
					}
				}
			});

			final Button rights2 = new Button(composite, SWT.CHECK);
			rights2.setText("User can see public downloads");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			rights2.setLayoutData(gd);
			rights2.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
						isDirty = true;
						if (rights2.getSelection()) {
							user.setRight(RemoteConstants.RIGHTS_SEE_PUBLICDL);
						} else {
							user.unsetRight(RemoteConstants.RIGHTS_SEE_PUBLICDL);
						}
					}
				}
			});

			final Button rights3 = new Button(composite, SWT.CHECK);
			rights3.setText("User has right to set download directory");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			rights3.setLayoutData(gd);
			rights3.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
						isDirty = true;
						if (rights3.getSelection()) {
							user.setRight(RemoteConstants.RIGHTS_SET_DL_DIR);
						} else {
							user.unsetRight(RemoteConstants.RIGHTS_SET_DL_DIR);
						}
					}
				}
			});

			final Button rights4 = new Button(composite, SWT.CHECK);
			rights4.setText("User has right to add Downloads as public Downloads");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			rights4.setLayoutData(gd);
			rights4.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					if (!user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
						isDirty = true;
						if (rights4.getSelection()) {
							user.setRight(RemoteConstants.RIGHTS_ADD_PUBLICDL);
						} else {
							user.unsetRight(RemoteConstants.RIGHTS_ADD_PUBLICDL);
						}
					}
				}
			});

			User currentUser = RCMain.getRCMain().getClient().getUserManager().getActiveUser();
			if (currentUser != null
					&& currentUser.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
				if (user.checkAccess(RemoteConstants.RIGHTS_ADMIN)) {
					combo.select(1);
					rights1.setEnabled(false);
					rights2.setEnabled(false);
					rights3.setEnabled(false);
					rights4.setEnabled(false);
				} else {
					combo.select(0);
					combo.setEnabled(true);
					rights1.setEnabled(true);
					rights2.setEnabled(true);
					rights3.setEnabled(true);
					rights4.setEnabled(true);
				}
			} else {
				combo.setEnabled(false);
				rights1.setEnabled(false);
				rights2.setEnabled(false);
				rights3.setEnabled(false);
				rights4.setEnabled(false);
			}

			rights1.setSelection(user.checkRight(RemoteConstants.RIGHTS_FORCESTART));
			rights2.setSelection(user.checkRight(RemoteConstants.RIGHTS_SEE_PUBLICDL));
			rights3.setSelection(user.checkRight(RemoteConstants.RIGHTS_SET_DL_DIR));
			rights4.setSelection(user.checkRight(RemoteConstants.RIGHTS_ADD_PUBLICDL));

			final Button downloadHistory = new Button(composite, SWT.CHECK);
			downloadHistory.setText("Enable Download History for this user.");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			downloadHistory.setLayoutData(gd);
			downloadHistory.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event arg0) {
					isDirty = true;
					user.setProperty("DownloadHistory",
							Boolean.toString(downloadHistory.getSelection()));
				}
			});
			downloadHistory.setSelection(Boolean.parseBoolean(user.getProperty("DownloadHistory")));
			// ---------Directory stuff ------------\\

			// output directory
			Label outputDir_text = new Label(composite, SWT.NONE);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			outputDir_text.setLayoutData(gridData);
			outputDir_text.setText(I18N.translate(PFX
					+ "editUserShell.outputDir.text"));

			// comp for directory input
			Composite output_comp = new Composite(composite, SWT.NONE);
			output_comp.setLayout(new GridLayout(3, false));

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			output_comp.setLayoutData(gridData);

			// output directory input field
			final Text outputDir = new Text(output_comp, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 250;
			outputDir.setLayoutData(gridData);
			outputDir.setText(user.getOutputDir());
			outputDir.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					if (!outputDir.getText().equals(user.getOutputDir())) {
						isDirty = true;
						outDir = outputDir.getText();
					}
				}
			});

			// auto import directory
			Label importDir_text = new Label(composite, SWT.NONE);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			importDir_text.setLayoutData(gridData);
			importDir_text.setText(I18N.translate(PFX
					+ "editUserShell.inputDir.text"));

			// comp for directory input
			Composite importDir_comp = new Composite(composite, SWT.NONE);
			importDir_comp.setLayout(new GridLayout(3, false));

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;
			importDir_comp.setLayoutData(gridData);

			// output directory input field
			final Text importDir = new Text(importDir_comp, SWT.BORDER);
			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 250;
			importDir.setLayoutData(gridData);
			importDir.setText(user.getAutoImportDir());
			importDir.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					if (!importDir.getText().equals(user.getAutoImportDir())) {
						isDirty = true;
						EditUserDialog.this.importDir = importDir.getText();
					}
				}
			});
			composite.layout();
		}

		public static void open(final String username) {
			Display display = RCMain.getRCMain().getDisplay();
			if (display == null) {
				return;
			}
			display.asyncExec(new SWTSafeRunnable() {
				@Override
				public void runSafe() {
					new EditUserDialog(username);

				}

			});
		}
	}

}// EOF
