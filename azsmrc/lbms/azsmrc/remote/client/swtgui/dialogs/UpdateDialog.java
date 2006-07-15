package lbms.azsmrc.remote.client.swtgui.dialogs;


import java.util.List;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.updater.Changelog;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateFile;
import lbms.tools.updater.Updater;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;


public class UpdateDialog{

	private static UpdateDialog instance;

	private Shell dialogShell;
	private Tree ChangelogTree;
	private Button cancel;
	private Button accept;
	private CLabel updateLabel1;

	//I18N prefix
	public static final String PFX = "dialog.updatedialog.";

	private UpdateDialog(final Display parent, final Update update, final Updater updater) {
		instance = this;
		parent.asyncExec(new SWTSafeRunnable(){

			public void runSafe() {

				try {
					List<UpdateFile> files = update.getFileList();
					dialogShell = new Shell(parent);
					dialogShell.setLayout(new GridLayout(2,false));
					dialogShell.setText(I18N.translate(PFX + "shell.text"));


					long totalSize = 0;
					//pull total size
					for(int i = 0; i < files.size(); i++){
						totalSize += files.get(i).getSize();
					}


					updateLabel1 = new CLabel(dialogShell, SWT.BORDER);
					GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
					gridData.horizontalSpan = 2;
					updateLabel1.setLayoutData(gridData);
					updateLabel1.setText(I18N.translate(PFX + "infoLabel.line1") + " " + update.getVersion().toString() +
							" " + I18N.translate(PFX + "infoLabel.line2")+ " " + getImportanceLevelString(update.getImportance_level())+
							I18N.translate(PFX + "infoLabel.line3") + " " + getTypeString(update.getType()) +
							I18N.translate(PFX + "infoLabel.line4") + " " + DisplayFormatters.formatByteCountToBase10KBEtc(totalSize));
					updateLabel1.setAlignment(SWT.CENTER);
					updateLabel1.setBackground(dialogShell.getDisplay().getSystemColor(SWT.COLOR_GRAY));


					Group gfiles = new Group(dialogShell, SWT.NULL);
					gfiles.setText(I18N.translate(PFX + "files.group.text"));
					GridData gd = new GridData(GridData.FILL_BOTH);
					gd.horizontalSpan = 2;
					gfiles.setLayoutData(gd);
					gfiles.setLayout(new GridLayout(1,false));

					Table fileTable = new Table(gfiles,SWT.V_SCROLL | SWT.BORDER);
					gd = new GridData(GridData.FILL_BOTH);
					fileTable.setLayoutData(gd);

					for(int i = 0; i < files.size(); i++){
						TableItem item = new TableItem(fileTable,SWT.NULL);
						item.setText(files.get(i).getName() + "  (" + DisplayFormatters.formatByteCountToBase10KBEtc(files.get(i).getSize()) + ")");
					}


					Group gChangelog = new Group(dialogShell, SWT.NULL);
					gChangelog.setText(I18N.translate(PFX + "changelog.group.text"));
					gd = new GridData(GridData.FILL_BOTH);
					gd.horizontalSpan = 2;
					gChangelog.setLayoutData(gd);
					gChangelog.setLayout(new GridLayout(1,false));


					ChangelogTree = new Tree(gChangelog, SWT.BORDER);
					GridData ChangelogTreeLData = new GridData(GridData.FILL_HORIZONTAL);
					ChangelogTreeLData.verticalSpan = 5;
					ChangelogTreeLData.widthHint = 460;
					ChangelogTreeLData.heightHint = 208;
					ChangelogTree.setLayoutData(ChangelogTreeLData);



					//pull the changelog
					Changelog log = update.getChangeLog();
					//pull all of the lists
					List<String> bugs = log.getBugFixes();
					List<String> changes = log.getChanges();
					List<String> features = log.getFeatures();
					int totalItems = bugs.size() + changes.size() + features.size();
					gChangelog.setText(I18N.translate(PFX + "changelog.group.text.filled.part1") +
							" (" + totalItems + " " +
							I18N.translate(PFX + "changelog.group.text.filled.part2") + ":");


					if(bugs.size() > 0){
						TreeItem mainBugItem = new TreeItem(ChangelogTree,SWT.NULL);
						mainBugItem.setText(I18N.translate(PFX + "changelog.bugfixes") + " ("
								+ bugs.size() +
								" " + I18N.translate(PFX + "changelog.group.text.filled.part2"));

						for(int j = 0; j < bugs.size(); j++){
							TreeItem bugItem = new TreeItem(mainBugItem,SWT.NULL);
							bugItem.setText(bugs.get(j).toString());
						}

					}


					if(changes.size() > 0){
						TreeItem mainChangeItem = new TreeItem(ChangelogTree,SWT.NULL);
						mainChangeItem.setText(I18N.translate(PFX + "changelog.changes") + " ("
								+ changes.size()
								+ " " + I18N.translate(PFX + "changelog.group.text.filled.part2"));


						for(int j = 0; j < changes.size(); j++){
							TreeItem changeItem = new TreeItem(mainChangeItem,SWT.NULL);
							changeItem.setText(changes.get(j).toString());
						}

					}



					if(features.size() > 0){
						TreeItem mainFeatureItem = new TreeItem(ChangelogTree,SWT.NULL);
						mainFeatureItem.setText(I18N.translate(PFX + "changelog.featureadditions") + " ("
								+ features.size() +
								" " + I18N.translate(PFX + "changelog.group.text.filled.part2"));


						for(int j = 0; j < features.size(); j++){
							TreeItem featureItem = new TreeItem(mainFeatureItem,SWT.NULL);
							featureItem.setText(features.get(j).toString());
						}

					}


					accept = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
					GridData acceptLData = new GridData();
					acceptLData.horizontalAlignment = GridData.END;
					acceptLData.grabExcessHorizontalSpace = true;
					accept.setLayoutData(acceptLData);
					accept.setText(I18N.translate(PFX + "accept_button.text"));
					accept.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event arg0) {
							updater.doUpdate();
							dialogShell.close();
						}
					});

					cancel = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
					GridData cancelLData = new GridData();
					cancelLData.horizontalAlignment = GridData.END;
					cancel.setLayoutData(cancelLData);
					cancel.setText(I18N.translate("global.cancel"));
					cancel.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event arg0) {
							dialogShell.close();
						}
					});


					GUI_Utilities.centerShellandOpen(dialogShell);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * Static method to open the update dialog
	 * @param parent
	 * @param update
	 * @param updater
	 */
	public static void open(final Display display, final Update update, final Updater updater){
		if(display == null) return;
		if (instance == null || instance.dialogShell == null || instance.dialogShell.isDisposed()){
			new UpdateDialog(display,update,updater);
		}else
			instance.dialogShell.setActive();

	}





	private String getImportanceLevelString(int level){
		if(level == Update.LV_BUGFIX)
			return I18N.translate(PFX + "changelog.bugfixes");
		else if(level == Update.LV_CHANGE)
			return I18N.translate(PFX + "changelog.changes");
		else if(level == Update.LV_FEATURE)
			return I18N.translate(PFX + "changelog.featureadditions");
		else if(level == Update.LV_LOW)
			return I18N.translate(PFX + "changelog.low");
		else if(level == Update.LV_SEC_RISK)
			return I18N.translate(PFX + "changelog.securityrisk");
		else return I18N.translate("global.error");
	}

	private String getTypeString(int type){
		if(type == Update.TYPE_BETA)
			return I18N.translate(PFX + "releasetype.beta");
		else if(type == Update.TYPE_MAINTENANCE)
			return I18N.translate(PFX + "releasetype.maintenance");
		else if(type == Update.TYPE_STABLE)
			return I18N.translate(PFX + "releasetype.stable");
		else return I18N.translate("global.error");
	}

}//EOF
