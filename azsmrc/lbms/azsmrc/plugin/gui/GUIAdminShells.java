/*
 * Created on Nov 30, 2005 Created by omschaub
 * 
 */
package lbms.azsmrc.plugin.gui;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.Timers;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
import lbms.azsmrc.shared.UserNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class GUIAdminShells {

	private static Table	users_table;
	private static Shell	shell	= null;
	private static int		OLD_INTERVAL;

	/**
	 * Open the main Config Dialog
	 * 
	 */
	public static void openConfigDialog () {
		final Thread openConfig_thread = new Thread() {
			@Override
			public void run () {
				if (SWTUtil.getDisplay() == null
						&& SWTUtil.getDisplay().isDisposed()) {
					return;
				}
				SWTUtil.getDisplay().asyncExec(new Runnable() {
					public void run () {

						if (shell != null && !shell.isDisposed()) {
							shell.setActive();
							return;
						}

						//Set up the old interval to see if we need to reset the timer
						OLD_INTERVAL = Plugin.getPluginInterface()
								.getPluginconfig().getPluginIntParameter(
										"user_dir_scan_time", 60);

						//Shell Initialize
						shell = new Shell();
						shell.setImage(ImageRepository.getImage("settings"));

						//Grid Layout   
						shell.setLayout(new GridLayout(2, false));

						//composite for shell
						final Composite config_composite = new Composite(shell,
								SWT.NULL);

						//Grid Layout   
						config_composite.setLayout(new GridLayout(2, false));

						GridData gridData = new GridData(GridData.FILL_BOTH);
						gridData.horizontalSpan = 2;
						config_composite.setLayoutData(gridData);

						//shell title                        
						shell.setText(Plugin.getLocaleUtilities()
								.getLocalisedMessageText(
										"GUIAdminShell.configDialog.title"));

						final SashForm form = new SashForm(config_composite,
								SWT.HORIZONTAL);
						form.setLayout(new GridLayout());
						gridData = new GridData(GridData.FILL_BOTH);
						gridData.horizontalSpan = 2;
						form.setLayoutData(gridData);

						final Composite child1 = new Composite(form, SWT.NONE);
						child1.setLayout(new GridLayout());
						gridData = new GridData(GridData.FILL_BOTH);
						gridData.heightHint = 400;
						child1.setLayoutData(gridData);

						final Composite child2 = new Composite(form, SWT.NONE);
						child2.setLayout(new GridLayout());
						gridData = new GridData(GridData.FILL_BOTH);
						gridData.heightHint = 400;
						child2.setLayoutData(gridData);

						form.setWeights(new int[] { 30, 60 });

						//Tree form on left (child1)
						Tree tree = new Tree(child1, SWT.BORDER | SWT.V_SCROLL
								| SWT.H_SCROLL);
						gridData = new GridData(GridData.FILL_BOTH);
						gridData.heightHint = 300;
						tree.setLayoutData(gridData);

						//General Settings TreeItem
						TreeItem item1 = new TreeItem(tree, SWT.NONE);
						item1
								.setText(Plugin
										.getLocaleUtilities()
										.getLocalisedMessageText(
												"GUIAdminShell.configDialog.treeItem.generalSettings"));

						//Download Slots TreeItem
						TreeItem item2 = new TreeItem(tree, SWT.NONE);
						item2
								.setText(Plugin
										.getLocaleUtilities()
										.getLocalisedMessageText(
												"GUIAdminShell.configDialog.treeItem.slotSettings"));

						openGeneralSettingsDialog(child2);
						child2.layout();
						config_composite.layout();

						tree.addListener(SWT.Selection, new Listener() {
							public void handleEvent (Event event) {

								TreeItem tempItem = (TreeItem) event.item;
								if (tempItem
										.getText()
										.equalsIgnoreCase(
												Plugin
														.getLocaleUtilities()
														.getLocalisedMessageText(
																"GUIAdminShell.configDialog.treeItem.generalSettings"))) {
									Control[] controls = child2.getChildren();
									for (Control control : controls) {
										control.dispose();
									}

									openGeneralSettingsDialog(child2);
									child2.layout();
								} else {
									Control[] controls = child2.getChildren();
									for (Control control : controls) {
										control.dispose();
									}

									openSlotShell(child2);
									child2.layout();
									config_composite.layout();

								}
							}
						});

						// Button for Close
						Button cancel = new Button(config_composite, SWT.PUSH);
						gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
						gridData.horizontalSpan = 2;
						cancel.setLayoutData(gridData);
						cancel
								.setText(Plugin
										.getLocaleUtilities()
										.getLocalisedMessageText(
												"GUIAdminShell.configDialog.button.close"));
						cancel.addListener(SWT.Selection, new Listener() {
							public void handleEvent (Event e) {
								shell.dispose();
							}
						});

						shell.setDefaultButton(cancel);

						//Resize Listener
						shell.addListener(SWT.Resize, new Listener() {
							public void handleEvent (Event event) {
								child1.layout();
								child2.layout();
								form.layout();
								config_composite.layout();
								shell.layout();
							}
						});

						//Listener for the shell being disposed 
						//so that we can assure all settings are saved
						shell.addListener(SWT.Dispose, new Listener() {
							public void handleEvent (Event event) {
								//Redraw the main table incase anything changed
								GUIMain.redrawTable();

								//reset timer if necessary
								if (OLD_INTERVAL != Plugin.getPluginInterface()
										.getPluginconfig()
										.getPluginIntParameter(
												"user_dir_scan_time", 60)) {
									//System.out.println("Reset Timer");
									Timers.restartCheckDirsTimer();
								}
							}
						});

						//Listen for ESC
						tree.addKeyListener(new KeyListener() {
							public void keyPressed (KeyEvent e) {
							}

							public void keyReleased (KeyEvent e) {
								switch (e.character) {
								case SWT.ESC:
									shell.dispose();
									break;
								}

							}
						});

						//open shell
						Utilities.centerShellandOpen(shell);
					}
				});
			}
		};
		openConfig_thread.setDaemon(true);
		openConfig_thread.start();

	}

	public static void openGeneralSettingsDialog (Composite parent) {

		Composite grayLabel = new Composite(parent, SWT.BORDER);
		grayLabel.setBackground(SWTUtil.getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		grayLabel.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		grayLabel.setLayoutData(gridData);

		Label title = new Label(grayLabel, SWT.NONE);
		title.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.configDialog.treeItem.generalSettings"));
		title.setBackground(grayLabel.getBackground());

		//Set it bold
		Font initialFont = title.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(SWTUtil.getDisplay(), fontData);
		title.setFont(newFont);
		newFont.dispose();

		grayLabel.pack();

		//composite for shell
		Composite config_composite = new Composite(parent, SWT.BORDER);

		//Grid Layout   
		config_composite.setLayout(new GridLayout(2, false));
		gridData = new GridData(GridData.FILL_BOTH);
		config_composite.setLayoutData(gridData);

		// Label for timer
		Label timerLabel = new Label(config_composite, SWT.NULL);
		timerLabel.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.generalSettings.timerLabel"));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		timerLabel.setLayoutData(gridData);

		// Text for timer
		final Text timerText = new Text(config_composite, SWT.BORDER);
		timerText.setText(String.valueOf(Plugin.getPluginInterface()
				.getPluginconfig().getPluginIntParameter("user_dir_scan_time",
						60) / 60));
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = 20;
		gridData.horizontalSpan = 1;
		timerText.setLayoutData(gridData);

		timerText.addListener(SWT.Verify, new Listener() {
			public void handleEvent (Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (chars[i] != '.') {
						if (!('0' <= chars[i] && chars[i] <= '9')) {

							e.doit = false;
							return;
						}
					}

				}
			}
		});

		timerText.addListener(SWT.Modify, new Listener() {
			public void handleEvent (Event e) {
				try {
					float time = Float.valueOf(timerText.getText());

					if (time > 0) {
						Plugin.getPluginInterface().getPluginconfig()
								.setPluginParameter("user_dir_scan_time",
										(int) time * 60);
						System.out.println("Interval set to: " + time * 60
								+ " seconds");
					}

				} catch (Exception e1) {
				}

			}
		});

	}

	private static void openSlotShell (Composite parent) {
		//title
		Composite grayLabel = new Composite(parent, SWT.BORDER);
		grayLabel.setBackground(SWTUtil.getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		grayLabel.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		grayLabel.setLayoutData(gridData);

		Label title = new Label(grayLabel, SWT.NONE);
		title.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.configDialog.treeItem.slotSettings"));
		title.setBackground(grayLabel.getBackground());

		//Set it bold
		Font initialFont = title.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(SWTUtil.getDisplay(), fontData);
		title.setFont(newFont);
		newFont.dispose();

		grayLabel.pack();

		//main composite
		Composite slot_composite = new Composite(parent, SWT.NULL);

		//Grid Layout   
		//slot_composite.setLayout(new GridLayout(3,false));
		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.numColumns = 3;
		gl.makeColumnsEqualWidth = false;
		slot_composite.setLayout(gl);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		slot_composite.setLayoutData(gridData);

		//Main download slots Label
		Label nameLabel = new Label(slot_composite, SWT.WRAP);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		nameLabel.setLayoutData(gridData);
		nameLabel.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.slotShell.nameLabel"));

		//Grab the total slots from Azureus
		int azureus_slots = Plugin.getPluginInterface().getPluginconfig()
				.getUnsafeIntParameter("Max Downloads");
		if (azureus_slots > 25) {
			azureus_slots = 25;
			Plugin.getPluginInterface().getPluginconfig()
					.setUnsafeIntParameter("Max Downloads", azureus_slots);
		}

		final CustomSpinner totalSlots = new CustomSpinner(slot_composite,
				SWT.NULL);
		totalSlots.setMaximum(25);
		totalSlots.setMinimum(1);
		totalSlots.setSelection(azureus_slots);
		totalSlots.pack();

		totalSlots.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event event) {
				totalSlots.setUpEnabled(true);
				totalSlots.setDownEnabled(true);
				if (totalSlots.getSelection() == totalSlots.getMaximum()) {
					totalSlots.setUpEnabled(false);
				} else if (totalSlots.getSelection() == totalSlots.getMinimum()) {
					totalSlots.setDownEnabled(false);
				}
				if (totalSlots.getSelection() > 25) {
					totalSlots.setSelection(25);
				}

				int totalCounts = getTotalCount(users_table);
				if (totalSlots.getSelection() < totalCounts) {
					totalSlots.setSelection(totalCounts);

				}

				Plugin.getPluginInterface().getPluginconfig()
						.setUnsafeIntParameter("Max Downloads",
								totalSlots.getSelection());

			}
		});

		//Warning download slots Label
		Label warningLabel = new Label(slot_composite, SWT.WRAP);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		warningLabel.setLayoutData(gridData);
		warningLabel
				.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
						"GUIAdminShell.slotShell.warningLabel"));
		warningLabel.setForeground(SWTUtil.getDisplay().getSystemColor(
				SWT.COLOR_DARK_RED));

		//-------------Table for users-------------\\
		users_table = new Table(slot_composite, SWT.H_SCROLL | SWT.BORDER
				| SWT.HIDE_SELECTION);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		gridData.verticalSpan = 5;
		gridData.heightHint = 200;
		users_table.setLayoutData(gridData);
		//users_table.setLinesVisible(true);

		TableColumn userName = new TableColumn(users_table, SWT.LEFT);
		userName.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.slotShell.userName"));
		userName.setWidth(200);

		TableColumn userSlots = new TableColumn(users_table, SWT.CENTER);
		userSlots.setText(Plugin.getLocaleUtilities().getLocalisedMessageText(
				"GUIAdminShell.slotShell.userSlots"));
		userSlots.setWidth(100);

		//add in data
		String[] users = Plugin.getXMLConfig().getUserList();
		for (int i = 0; i < users.length; i++) {
			TableItem item = new TableItem(users_table, SWT.NULL);

			try {
				final User tempUser = Plugin.getXMLConfig().getUser(users[i]);
				item.setText(0, tempUser.getUsername());

				//gray if needed
				if (i % 2 != 0) {
					item.setBackground(ColorUtilities.getBackgroundColor());
				}

				TableEditor editor = new TableEditor(users_table);

				final CustomSpinner spinner = new CustomSpinner(users_table,
						SWT.NULL);
				spinner.setMaximum(25);
				spinner.setMinimum(0);
				spinner.setSelection(tempUser.getDownloadSlots());
				spinner.setBackground(item.getBackground());
				spinner.pack();
				spinner.addListener(SWT.Selection, new Listener() {
					public void handleEvent (Event event) {
						tempUser.setDownloadSlots(spinner.getSelection());

						spinner.setUpEnabled(true);
						spinner.setDownEnabled(true);
						if (spinner.getSelection() == spinner.getMaximum()) {
							spinner.setUpEnabled(false);
						} else if (spinner.getSelection() == spinner
								.getMinimum()) {
							spinner.setDownEnabled(false);
						}

						int totalCount = getTotalCount(users_table);

						//bump back if totalCount is greater than 25!
						if (totalCount > 25) {
							spinner.setSelection(spinner.getSelection() - 1);
							tempUser.setDownloadSlots(spinner.getSelection());
						}

						if (totalSlots.getSelection() < totalCount) {

							totalSlots.setSelection(totalCount);
							Plugin.getPluginInterface().getPluginconfig()
									.setUnsafeIntParameter("Max Downloads",
											totalCount);
							writeUsersTotals(users_table);
						} else {
							writeUsersTotals(users_table);
						}

					}
				});

				editor.grabHorizontal = true;
				editor.setEditor(spinner, item, 1);

			} catch (UserNotFoundException e1) {
				Plugin.addToLog(e1.getMessage());
				e1.printStackTrace();
			}

		}

		users_table.pack();

		slot_composite.layout();

	}

	private static int getTotalCount (Table table) {
		int total = 0;

		Control[] controls = table.getChildren();
		for (int i = 0; i < controls.length; i++) {
			try {
				CustomSpinner spinner = (CustomSpinner) controls[i];
				total = total + spinner.getSelection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return total;
	}

	private static void writeUsersTotals (Table table) {
		String[] names = Plugin.getXMLConfig().getUserList();

		Control[] controls = table.getChildren();
		for (int i = 0; i < controls.length; i++) {
			try {
				User tempUser = Plugin.getXMLConfig().getUser(names[i]);
				CustomSpinner spinner = (CustomSpinner) controls[i];
				tempUser.setDownloadSlots(spinner.getSelection());
				Plugin.getXMLConfig().saveConfigFile();
			} catch (UserNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
