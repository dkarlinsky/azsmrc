/*
 * Created on Jan 25, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AmazonDialog {

    

    public AmazonDialog(Display display){
        
        //Shell
        final Shell shell = new Shell(display);
        shell.setLayout(new GridLayout(1,false));
        shell.setText("Amazon Direct Link System");

        //Comp on shell
        Composite comp = new Composite(shell,SWT.NULL);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
        comp.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 2;
        comp.setLayout(gridLayout);


        //first line
        Label infoLabel = new Label(comp,SWT.BORDER | SWT.CENTER);
        infoLabel.setText("Your browser should have opened up Amazon with our referral information in place.\n" +
        		"In addition to that, if you purchase an Amazon product using this 'direct link' system\n" +
        		"Amazon will donate back to us a whopping 2.5% of your purchase price");
        
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        infoLabel.setLayoutData(gd);
        
        Text info1 = new Text(comp,SWT.BORDER | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        info1.setLayoutData(gd);
        info1.setText("Here is how it works:\n\n" +
        		"Visit Amazon and find a product you want to buy.  Here is an example URL:\n\n" +
        		"http://www.amazon.com/gp/product/B0000643Q8/qid=1144059482/sr=1-3/ \n\n" +
        		"Simply find the number after the /product/ -- here it is B0000643Q8 \n" +
        		"Put this into the box below and then click to view the product page from either\n" +
        		"Amazon.com or Amazon.de and Amazon will donate back to us the money just for\n" +
        		"your buying through this link. It is that easy!");
        

        Label spacer = new Label(comp, SWT.NULL);
        spacer.setText("");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        spacer.setLayoutData(gd);
        
        
        
        Label productNumL = new Label(comp, SWT.NULL);
        productNumL.setText("Enter Product Number Here:");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 1;
        productNumL.setLayoutData(gd);
        
        
        final Text productNum = new Text(comp,SWT.BORDER | SWT.SINGLE | SWT.LEFT);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        //gridData.widthHint = 228;
        productNum.setLayoutData(gd);

        
        //Bottom Buttons
        Composite button_comp = new Composite(shell, SWT.NULL);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        button_comp.setLayoutData(gridData);

        gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginWidth = 0;
        button_comp.setLayout(gridLayout);

        Button amazonCom = new Button(button_comp,SWT.PUSH);
        amazonCom.setText("Amazon.com");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        amazonCom.setLayoutData(gd);
        amazonCom.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
        		Program.launch("http://www.amazon.com/exec/obidos/redirect?link_code=as2&path=ASIN/"+
        				productNum.getText()+
        				"&tag=azsmrc-20&camp=1789&creative=9325");
        		shell.close();
            }
         });

        Button amazonDe = new Button(button_comp,SWT.PUSH);
        amazonDe.setText("Amazon.de");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        amazonDe.setLayoutData(gd);
        amazonDe.addListener(SWT.Selection, new Listener(){
        	public void handleEvent(Event e) {

        		Program.launch("http://www.amazon.de/exec/obidos/redirect?link_code=as2&path=ASIN/"+
        				productNum.getText()+
        				"&tag=azsmrc-21&camp=1638&creative=6742");    
        		shell.close();
                
        	}
        });
        
        
        Button cancel = new Button(button_comp,SWT.PUSH);
        cancel.setText("Cancel");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gd.grabExcessHorizontalSpace = true;
        cancel.setLayoutData(gd);
        cancel.addListener(SWT.Selection, new Listener(){
            public void handleEvent(Event e) {
                shell.close();
             }
         });



        //Center and open shell
        GUI_Utilities.centerShellandOpen(shell);
    }
}
