/*
 * Created on Nov 28, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.plugin.gui;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.shared.UserNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

public class GUILogin {



    private static Label status;



    /**
     * Make the main composite show the login screen
     *
     * @param composite
     */
    public static void openLogin(Composite composite){


        // Main Composite for the login layout
        Composite loginComp = new Composite(composite, SWT.BORDER);

        // Set the comp on its parent
        GridData gridData = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        //gridData.widthHint = 200;
        gridData.verticalSpan = 5;
        loginComp.setLayoutData(gridData);



        // Set the layout of the comp
        loginComp.setLayout(new GridLayout(2,false));


        //Label and Text for the User Name
        Label userNameLabel = new Label(loginComp,SWT.NULL);
        userNameLabel.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUILogin.openLogin.userNameLabel"));

        final Text userName = new Text(loginComp,SWT.BORDER | SWT.LEFT | SWT.SINGLE);
        userName.setEditable(true);
        gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 200;
        userName.setLayoutData(gridData);

        //Label and Text for the Password
        Label passwordLabel = new Label(loginComp,SWT.NULL);
        passwordLabel.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUILogin.openLogin.passwordLabel"));

        final Text password = new Text(loginComp,SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
        password.setEditable(true);
        gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 200;
        password.setLayoutData(gridData);

        //Listener on the password to remove error message
        password.addListener (SWT.Verify, new Listener () {
            public void handleEvent (Event e) {
                status.setVisible(false);
            }
        });

        //Button for Login
        Button loginButton = new Button(loginComp, SWT.PUSH);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
        loginButton.setLayoutData(gridData);
        loginButton.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUILogin.openLogin.loginButton"));

        // Text for failure
        status = new Label(loginComp, SWT.NULL);
        status.setText(Plugin.getLocaleUtilities().getLocalisedMessageText("GUILogin.openLogin.status"));
        status.setForeground(Plugin.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 1;
        gridData.verticalSpan = 1;
        status.setLayoutData(gridData);
        status.setVisible(false);

        //Listener for Login Button
        loginButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if(userName.getText().length() != 0 || password.getText().length() != 0){

                    try {
						//Verify User and Password
						User tempUser = Plugin.getXMLConfig().getUser(userName.getText());
						if(tempUser.verifyPassword(password.getText())){
						    // Verify is true, so proceed and login
                            login(userName);
						}else{
						    // Verify is false, so reject login

                            password.setText("");
                            status.setVisible(true);

						}
					} catch (UserNotFoundException e1) {
						userName.setText("");
                        password.setText("");
                        status.setVisible(true);
					}



                }else{
                    //Something is empty, so show error
                    status.setVisible(true);
                }





            }
        });


        password.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                //Empty
            }

            public void keyReleased (KeyEvent e) {
                switch (e.character){
                case SWT.CR:
                    if(userName.getText().length() != 0 || password.getText().length() != 0){

                        try {
                            //Verify User and Password
                            User tempUser = Plugin.getXMLConfig().getUser(userName.getText());
                            if(tempUser.verifyPassword(password.getText())){
                                // Verify is true, so proceed and login
                                login(userName);

                            }else{
                                // Verify is false, so reject login
                                status.setVisible(true);
                                password.setText("");

                            }
                        } catch (UserNotFoundException e1) {
                            e1.printStackTrace();
                            Plugin.addToLog(e1.toString());

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

                        }



                    }else{
                        //  Something is empty, so show error
                        status.setVisible(true);
                    }



                    break;

                }
            }
        });

        //redraw the main comp
        View.composite.layout();
    }

    private static void login(final Text userName){
        Plugin.getDisplay().asyncExec(new Runnable (){
            public void run () {
                Plugin.LOGGED_IN_USER = userName.getText();
                if(status != null || !status.isDisposed())
                    status.setText("");
                //Destroy the login on the main composite
                Control[] controls = View.composite.getChildren();
                for(int i = 0; i < controls.length; i++){
                    controls[i].dispose();
                }

                //Redraw the Composite
                GUIMain.open(View.composite);

                return;
            }
        });
    }
}
