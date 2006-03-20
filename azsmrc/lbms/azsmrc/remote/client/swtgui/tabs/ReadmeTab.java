/*
 * Created on Feb 18, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;



import lbms.azsmrc.remote.client.swtgui.ImageRepository;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;



public class ReadmeTab {

    private String sourceFile = "Readme.txt";
    private String sourceDir = System.getProperty("user.dir")+System.getProperty("file.separator");
    private String paypalURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=omschaub%40users%2esourceforge%2enet&item_name=AzMultiUser%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=USD&bn=PP%2dDonationsBF&charset=UTF%2d8";

    public ReadmeTab(CTabFolder parentTab){
        final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
        detailsTab.setText("Information");


        final Composite parent = new Composite(parentTab, SWT.NONE);
        parent.setLayout(new GridLayout(1,false));
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 1;
        parent.setLayoutData(gridData);

        Button donate = new Button(parent,SWT.PUSH);
        donate.setImage(ImageRepository.getImage("paypal"));
        donate.setToolTipText("If you like AzMultiUser, please consider donating. Thanks!");
        donate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Program.launch(paypalURL);
            }
        });


        Group info = new Group(parent,SWT.NONE);
        info.setText("AzMultiUser Information (Readme.txt)");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        info.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        info.setLayoutData(gridData);


        List readme = new List(info, SWT.BORDER | SWT.V_SCROLL );
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan  = 3;
        readme.setLayoutData(gridData);


        //populate the readme
        try{
            File infile = new File(sourceDir + sourceFile);
            readme.removeAll();
            BufferedReader in = new BufferedReader(new FileReader(infile));
            String line_temp;
            while((line_temp = in.readLine()) != null)
            {
                readme.add(line_temp);
            }
        }catch(Exception e){
            e.printStackTrace();
            readme.removeAll();
            readme.add("Error Loading File: " + sourceDir + sourceFile);
        }



        detailsTab.setControl(parent);
        parentTab.setSelection(detailsTab);
    }

}
