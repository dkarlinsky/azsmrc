/*
 * Created on Feb 18, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.dialogs.AmazonDialog;
import lbms.azsmrc.remote.client.util.DisplayFormatters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;




public class ReadmeTab {

    
    
    private String paypalURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=omschaub%40users%2esourceforge%2enet&item_name=AzMultiUser%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=USD&bn=PP%2dDonationsBF&charset=UTF%2d8";
    private String amazonComURL = "http://www.amazon.com/exec/obidos/redirect?tag=azsmrc-20&amp;creative=374005&amp;camp=211041&amp;link_code=qs1&amp;adid=0TY7KZ926FVJDA9X0AQ9&amp;path=subst/home/home.html";
    private String amazonDeURL = "http://www.amazon.de/exec/obidos/redirect-home?tag=azsmrc-21&site=home";
    private String homepageURL = "http://azsmrc.sourceforge.net";
    private String projectpageURL = "http://www.sourceforge.net/projects/azsmrc";
    private String downloadsURL = "http://sourceforge.net/project/showfiles.php?group_id=163110";
    private String forumsURL = "http://sourceforge.net/forum/?group_id=163110";
    
    
    
    public ReadmeTab(CTabFolder parentTab){
        final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
        detailsTab.setText("Information");


        
        final Composite parent = new Composite(parentTab, SWT.NONE);
        parent.setLayout(new GridLayout(3,false));
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 1;
        parent.setLayoutData(gridData);

        Composite donateC = new Composite(parent, SWT.NONE);
        donateC.setLayout(new GridLayout(3,false));
        gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        donateC.setLayoutData(gridData);
        
        Button donate = new Button(donateC,SWT.PUSH);
        donate.setImage(ImageRepository.getImage("paypal"));
        donate.setToolTipText("If you like AzSMRC, please consider donating. Thanks!");
        donate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch(paypalURL);
            }
        });

        
        Button amazonDonate = new Button(donateC,SWT.PUSH);
        amazonDonate.setImage(ImageRepository.getImage("amazon.com"));
        amazonDonate.setToolTipText("By making your Amazon.com purchases through our referral link, they pay us a donation! So, start your Amazon shopping from here");
        amazonDonate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch(amazonComURL);
                //new AmazonDialog(RCMain.getRCMain().getDisplay());
            }
        });

        
        Button amazonDeDonate = new Button(donateC,SWT.PUSH);
        amazonDeDonate.setImage(ImageRepository.getImage("amazon.de"));
        amazonDeDonate.setToolTipText("Wenn du bei Amazon.de einkaufst und dabei unseren Link benutzt, zahlt Amazon uns eine Provision! Also starte deinen Amazon Einkauf bei uns.");
        amazonDeDonate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch(amazonDeURL);
                new AmazonDialog(RCMain.getRCMain().getDisplay());
            }
        });
        
        Label label = new Label(parent, SWT.NULL);
        label.setText("A lot of time and effort went into creating this remote system for Azureus.\nPlease, if you use it, consider donating or at least shopping through these sponser links.\nThank you!");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 3;
        label.setLayoutData(gridData);
        
        Composite infoC = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.verticalSpacing = 20;        
        infoC.setLayout(gridLayout);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);        
        gridData.horizontalSpan = 3;
        infoC.setLayoutData(gridData);
        
        
        Group devs = new Group(infoC,SWT.NULL);
        devs.setText("AzSMRC Developers");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 75;
        gridLayout.marginHeight = 15;
        devs.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);        
        gridData.horizontalSpan = 3;        
        devs.setLayoutData(gridData);

        
        Label leonard = new Label(devs,SWT.NULL);                
        leonard.setText("Leonard Br\u00FCnings");
        
        Label marc = new Label(devs,SWT.NULL);
        marc.setText("Marc Schaubach");
        
        
        
        Group info = new Group(infoC,SWT.NONE);
        info.setText("AzSMRC Information");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 10;
        gridLayout.horizontalSpacing = 10;
        info.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        info.setLayoutData(gridData);


        Label versionL = new Label(info,SWT.NULL);
        versionL.setText("Running AzSMRC Version:");
        
        Label version = new Label(info, SWT.NULL);
        version.setText(RCMain.getRCMain().getAzsmrcProperties().getProperty("version", "Error Reading Properties"));
        
        
        Label azsmrcUptimeL = new Label(info, SWT.NULL);
        azsmrcUptimeL.setText("AzSMRC Uptime:");
        
		Label azsmrcUptime = new Label(info, SWT.NULL);
		azsmrcUptime.setText(DisplayFormatters.formatTime((RCMain.getRCMain().getRunTime())));

		Label lastUpdateL = new Label(info, SWT.NULL);
		lastUpdateL.setText("Last Update Check:");
				
		Label lastUpdate = new Label(info, SWT.NULL);
		lastUpdate.setText(DisplayFormatters.formatDate(Long.parseLong(RCMain.getRCMain().getProperties().getProperty("update.lastcheck","0"))));

		Label nextUpdateL = new Label(info,SWT.NULL);
		nextUpdateL.setText("Next Update Check:");		
		
		Label nextUpdate = new Label(info, SWT.NULL);
		nextUpdate.setText(DisplayFormatters.formatDate(Long.parseLong(RCMain.getRCMain().getProperties().getProperty("update.lastcheck","0"))+ (1000 * 60 * 60 * 24)));


        
        Group inet = new Group(infoC,SWT.NONE);
        inet.setText("Internet");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 10;
        gridLayout.horizontalSpacing = 30;
        inet.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);        
        gridData.horizontalSpan = 3;
        inet.setLayoutData(gridData);
		
		Label homepage = new Label(inet,SWT.NULL);
		homepage.setText("AzSMRC Homepage");
		homepage.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		homepage.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		homepage.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {
				Program.launch(homepageURL);				
			}			
		});
		
		Label projectpage = new Label(inet,SWT.NULL);
		projectpage.setText("SourceForge Project Page for AzSMRC");
		projectpage.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		projectpage.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		projectpage.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {
				Program.launch(projectpageURL);				
			}			
		});

		
		Label downloads = new Label(inet,SWT.NULL);
		downloads.setText("SourceForge Downloads");
		downloads.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		downloads.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		downloads.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {
				Program.launch(downloadsURL);				
			}			
		});

		Label forums = new Label(inet,SWT.NULL);
		forums.setText("Forums");
		forums.setForeground(RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		forums.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		forums.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {
				Program.launch(forumsURL);				
			}			
		});
		
        detailsTab.setControl(parent);
        parentTab.setSelection(detailsTab);
    }

}
