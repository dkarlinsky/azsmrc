/*
 * Created on Apr 24, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import lbms.azsmrc.remote.client.Client;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.events.ParameterListener;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.UpdateDialog;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.updater.Update;
import lbms.tools.updater.UpdateListener;
import lbms.tools.updater.Updater;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class AzPreferencesTab {



	private Composite cOptions;
	private Properties properties;

	private ParameterListener pl;

	private boolean bModified = false;

	public AzPreferencesTab(final CTabFolder parentTab){


		//Open properties for reading and saving
		properties = RCMain.getRCMain().getProperties();


		final CTabItem prefsTab = new CTabItem(parentTab, SWT.CLOSE);
		prefsTab.setText("Preferences");



		final Composite parent = new Composite(parentTab, SWT.NULL);
		parent.setLayout(new GridLayout(2,false));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);


		prefsTab.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {
				if(bModified){
					MessageBox box = new MessageBox(parent.getShell(),SWT.OK);
					box.setText("Usaved Changes");
					box.setMessage("You have made modifications to the preferences and " +
							"closed the tab before you saved.  All changes have been discarded");
					box.open();
				}
				
				//be sure to remove any listeners you set up here
				
				if(pl != null) RCMain.getRCMain().getClient().removeParameterListener(pl);
			}

		});



		//top label
		Composite grayLabel = new Composite(parent,SWT.BORDER);
		grayLabel.setBackground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_GRAY));
		grayLabel.setLayout(new GridLayout(1,false));
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		grayLabel.setLayoutData(gridData);

		Label title = new Label(grayLabel,SWT.NONE);
		title.setText("No changes are actually made until saved");
		title.setBackground(grayLabel.getBackground());

		//Set it bold
		Font initialFont = title.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
			fontData[i].setHeight(fontData[i].getHeight() + 2);
		}
		Font newFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
		title.setFont(newFont);
		newFont.dispose();

		grayLabel.pack();




		final SashForm sash = new SashForm(parent,SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		sash.setLayoutData(gridData);
		sash.setLayout(new GridLayout(1,false));


		//Tree on left side
		Tree tree = new Tree(sash,SWT.BORDER | SWT.H_SCROLL);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		tree.setLayoutData(gridData);

		final TreeItem ti = new TreeItem(tree,SWT.NULL);
		ti.setText("Tree Item");

		
		tree.addListener(SWT.Selection, new Listener(){

			public void handleEvent(Event event) {
				//any selection stuff for the tree goes here
				
				
			}

		});

		cOptions = new Composite(sash,SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		cOptions.setLayoutData(gridData);
		cOptions.setLayout(new GridLayout(2,false));

		//default selection
		tree.setSelection(new TreeItem[] {ti});
		
		
		
		
		//Drawy any composite stuff here
		
		
		
		
		

		//set the sash weight
		sash.setWeights(new int[] {80,320});

		//Buttons
		Composite button_comp = new Composite(parent, SWT.NULL);
		gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		button_comp.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 0;
		button_comp.setLayout(gridLayout);


		Button apply = new Button (button_comp,SWT.PUSH);
		apply.setText("Save Changes");
		apply.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event arg0) {
			   
				//Apply stuff here
				
			}
		});

		Button commit = new Button(button_comp,SWT.PUSH);
		commit.setText("Save Changes and Close");
		commit.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				//Commit stuff here

				
				
			}
		});


		Button cancel = new Button(button_comp,SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event e) {
				
				//be sure to remove any listeners here as well as in the tab dispose listener
				
				prefsTab.dispose();
			}
		});


		prefsTab.setControl(parent);
		parentTab.setSelection(prefsTab);
	}




}
