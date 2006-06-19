package lbms.azsmrc.remote.client.swtgui.tabs;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

public class MyShares {

	//  I18N prefix
	public static final String PFX = "tab.myshares.";



	private MyShares(CTabFolder parentTab){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText(I18N.translate(PFX + "tab.text"));

		
		
		

	}


	
	//Static public open ... call this to open
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
				new MyShares(parentTab);

			}

		});
	}

}
