/*
 * Created on Nov 18, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.plugin.gui;


import java.io.File;
import java.io.IOException;

import lbms.azsmrc.plugin.main.Plugin;
import lbms.azsmrc.plugin.main.User;
import lbms.azsmrc.plugin.main.Utilities;
import lbms.azsmrc.shared.DuplicatedUserException;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GUIUserUtils {

    /**
     * New shell for adding in a New User
     *
     */
    public void addNewUser(){
        if(Plugin.getDisplay()==null && Plugin.getDisplay().isDisposed())
            return;
        Plugin.getDisplay().asyncExec( new Runnable() {
            public void run() {
                //Shell Initialize

                final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                shell.setImage(ImageRepository.getImage("plus"));

                //Grid Layout
                GridLayout layout = new GridLayout();
                layout.numColumns = 1;
                shell.setLayout(layout);

                //composite for shell
                Composite backup_composite = new Composite(shell,SWT.NULL);

                //Grid Layout
                layout = new GridLayout();
                layout.numColumns = 3;
                backup_composite.setLayout(layout);

                //shell title
                shell.setText("Add New User");


                //User Name Label
                Label nameLabel = new Label(backup_composite, SWT.NONE);
                GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 1;
                nameLabel.setLayoutData( gridData );
                nameLabel.setText("New User Name:");


                //User Name Input field
                final Text userName = new Text(backup_composite,SWT.BORDER);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 2;
                gridData.widthHint = 100;
                userName.setLayoutData( gridData);

                //Password Label
                Label passwordLabel = new Label(backup_composite, SWT.NONE);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 1;
                passwordLabel.setLayoutData( gridData );
                passwordLabel.setText("Password:");


                //User Name Input field
                final Text password = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 2;
                gridData.widthHint = 100;
                password.setLayoutData( gridData);

                //verify password Label
                Label verify_text = new Label(backup_composite, SWT.NONE);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 1;
                verify_text.setLayoutData( gridData );
                verify_text.setText("Verify Password:");


                //verify password field
                final Text verify = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 2;
                gridData.widthHint = 100;
                verify.setLayoutData( gridData);


                //Combo Stuff

                //combo Label
                Label combo_text = new Label(backup_composite, SWT.NONE);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 1;
                combo_text.setLayoutData( gridData );
                combo_text.setText("Select User Type:");

                final Combo combo = new Combo(backup_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
                combo.add("Normal User");
                combo.add("Administrator");

                combo.select(0);



                //---------Directory stuff ------------\\


                //output directory
                Label outputDir_text = new Label(backup_composite, SWT.NONE);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 3;
                outputDir_text.setLayoutData( gridData );
                outputDir_text.setText("Output Directory for User:");

                //comp for directory input
                Composite output_comp = new Composite(backup_composite,SWT.NONE);
                output_comp.setLayout(new GridLayout(3,false));

                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 3;
                output_comp.setLayoutData(gridData);



                //output directory input field
                final Text outputDir = new Text(output_comp,SWT.BORDER);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 2;
                gridData.widthHint = 250;
                outputDir.setLayoutData(gridData);


                //icon for output directory
                Label outputDir_icon = new Label(output_comp, SWT.NONE);
                gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                gridData.horizontalSpan = 1;
                outputDir_icon.setLayoutData( gridData );
                outputDir_icon.setToolTipText("Click to choose directory");
                outputDir_icon.setImage(ImageRepository.getImage("folder"));
                outputDir_icon.addListener(SWT.MouseDown, new Listener() {
                    public void handleEvent(Event e) {
                        DirectoryDialog dirDialog = new DirectoryDialog(shell);
                        dirDialog.setText("Please Choose Output Directory");
                        dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
                        String selectedDir = dirDialog.open();
                        File selectedDir_file = new File(selectedDir);
                        //need to check if selected dir has files and if so, does it have a comments dir
                        if(selectedDir == null){
                            return;
                        }else if(!selectedDir_file.exists() || !selectedDir_file.isDirectory()){
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Selected Directory does not exist, please choose a valid directory.");
                            mb.open();
                        }else{
                            outputDir.setText(selectedDir);
                        }


                    }
                });

                //auto import directory
                Label importDir_text = new Label(backup_composite, SWT.NONE);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 3;
                importDir_text.setLayoutData( gridData );
                importDir_text.setText("Automatic Import Directory for User:");

                //comp for directory input
                Composite importDir_comp = new Composite(backup_composite,SWT.NONE);
                importDir_comp.setLayout(new GridLayout(3,false));

                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 3;
                importDir_comp.setLayoutData(gridData);



                //output directory input field
                final Text importDir = new Text(importDir_comp,SWT.BORDER);
                gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 2;
                gridData.widthHint = 250;
                importDir.setLayoutData(gridData);


                //icon for output directory
                Label importDir_icon = new Label(importDir_comp, SWT.NONE);
                gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                gridData.horizontalSpan = 1;
                importDir_icon.setLayoutData( gridData );
                importDir_icon.setToolTipText("Click to choose directory");
                importDir_icon.setImage(ImageRepository.getImage("folder"));
                importDir_icon.addListener(SWT.MouseDown, new Listener() {
                    public void handleEvent(Event e) {
                        DirectoryDialog dirDialog = new DirectoryDialog(shell);
                        dirDialog.setText("Please Choose Automatic Import Directory");
                        dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
                        String selectedDir = dirDialog.open();

                        //need to check if selected dir has files and if so, does it have a comments dir
                        if(selectedDir == null){
                            return;
                        }else{
                            importDir.setText(selectedDir);
                        }


                    }
                });

                //Button for Accept
                Button commit = new Button(backup_composite, SWT.PUSH);
                gridData = new GridData(GridData.CENTER);
                gridData.horizontalSpan = 1;
                commit.setLayoutData( gridData);
                commit.setText( "Accept");
                commit.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        if(userName.getText().equalsIgnoreCase("")      ||
                                password.getText().equalsIgnoreCase("") ||
                                verify.getText().equalsIgnoreCase("")   ){
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Please fill out all of the information.");
                            mb.open();
                            return;
                        }


                        if(password.getText().equalsIgnoreCase(verify.getText())){
                            //add the user to the XMLConfig file
                            try {
                                Plugin.getXMLConfig().addUser(userName.getText(),password.getText());
                                User currentUser = Plugin.getXMLConfig().getUser(userName.getText());
                                currentUser.setOutputDir(outputDir.getText());
                                currentUser.setAutoImportDir(importDir.getText());

                                if(combo.getSelectionIndex() != 0)
                                    currentUser.setRight(RemoteConstants.RIGHTS_ADMIN);

                                Plugin.getXMLConfig().saveConfigFile();
                            } catch (IOException e1) {
                                Plugin.addToLog(e1.toString());
                            } catch (DuplicatedUserException e2) {
                                MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                mb.setText("Error");
                                mb.setMessage("User name already exists.");
                                mb.open();
                                return;
                            } catch (UserNotFoundException e3) {
                                e3.printStackTrace();
                                Plugin.addToLog(e3.getMessage());
                                MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                mb.setText("Error");
                                mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                                mb.open();
                            }

                            //destroy the shell
                            shell.dispose();

                            //redraw the userTable
                            GUIMain.redrawTable();

                        }else{
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Passwords are not the same.");
                            mb.open();
                            password.setText("");
                            verify.setText("");
                            return;
                        }

                    }
                });


                //Button for Cancel
                Button cancel = new Button(backup_composite, SWT.PUSH);
                gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                gridData.horizontalSpan = 2;
                cancel.setLayoutData( gridData);
                cancel.setText( "Cancel");
                cancel.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event e) {
                        shell.dispose();
                    }
                });


                //Key Listener so that the user can just use ESC to cancel
                //in the beginning if they did not want to do this
                userName.addKeyListener(new KeyListener() {
                    public void keyPressed(KeyEvent e) {
                        //Empty
                    }

                    public void keyReleased (KeyEvent e) {
                        switch (e.character){
                        case SWT.ESC:

                            shell.dispose();
                            break;

                        }
                    }
                });

                //pack and open shell
                Utilities.centerShellandOpen(shell);

            }
        });

    }


    /**
     * GUI code to delete a user
     * @param userName
     * @return
     */
    public boolean deleteUser(String userName){
        MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.YES| SWT.NO |SWT.ICON_QUESTION);
        mb.setText("Delete User");
        mb.setMessage("Are you sure you wish to remove the selected user?");
        int response = mb.open();
        switch (response){

        case SWT.YES:
            try {
                Plugin.getXMLConfig().removeUser(userName);
                Plugin.getXMLConfig().saveConfigFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //redraw the userTable
            GUIMain.redrawTable();
            return true;

        case SWT.NO:
            break;

        }
        return false;
    }




    public void changePassword(final String user){
        final Thread addNew_thread = new Thread() {
            public void run() {
                if(Plugin.getDisplay()==null && Plugin.getDisplay().isDisposed())
                    return;
                Plugin.getDisplay().asyncExec( new Runnable() {
                    public void run() {
                        //Shell Initialize

                        final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                        shell.setImage(ImageRepository.getImage("plus"));

                        //Grid Layout
                        GridLayout layout = new GridLayout();
                        layout.numColumns = 1;
                        shell.setLayout(layout);

                        //composite for shell
                        Composite backup_composite = new Composite(shell,SWT.NULL);

                        //Grid Layout
                        layout = new GridLayout();
                        layout.numColumns = 3;
                        backup_composite.setLayout(layout);

                        //shell title
                        shell.setText("Change Password");


                        //User Name Label
                        Label nameLabel = new Label(backup_composite, SWT.NONE);
                        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        nameLabel.setLayoutData( gridData );
                        nameLabel.setText("Changing Password for User: ");


                        //User Name Input field
                        final Label userName = new Label(backup_composite,SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        userName.setLayoutData( gridData);
                        userName.setText(user);


                        //Password Label
                        Label oldPasswordLabel = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        oldPasswordLabel.setLayoutData( gridData );
                        oldPasswordLabel.setText("Old Password:");


                        //User Name Input field
                        final Text oldPassword = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 100;
                        oldPassword.setLayoutData( gridData);



                        //Password Label
                        Label passwordLabel = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        passwordLabel.setLayoutData( gridData );
                        passwordLabel.setText("New Password:");


                        //User Name Input field
                        final Text password = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 100;
                        password.setLayoutData( gridData);

                        //User Name Label
                        Label verify_text = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        verify_text.setLayoutData( gridData );
                        verify_text.setText("Verify New Password:");


                        //User Name Input field
                        final Text verify = new Text(backup_composite,SWT.BORDER | SWT.PASSWORD);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 100;
                        verify.setLayoutData( gridData);

                        //Button for Accept
                        Button commit = new Button(backup_composite, SWT.PUSH);
                        gridData = new GridData(GridData.CENTER);
                        gridData.horizontalSpan = 1;
                        commit.setLayoutData( gridData);
                        commit.setText( "Accept");
                        commit.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event e) {
                                if(password.getText().equalsIgnoreCase("") ||
                                        verify.getText().equalsIgnoreCase("") ||
                                        oldPassword.getText().equalsIgnoreCase("")){

                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("Please fill out all of the information.");
                                    mb.open();
                                    return;
                                }


                                try {
                                    if(!Plugin.getXMLConfig().getUser(user).verifyPassword(oldPassword.getText())){
                                        MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                        mb.setText("Error");
                                        mb.setMessage("Old password is not correct.");
                                        mb.open();
                                        oldPassword.setText("");
                                        return;
                                    }
                                } catch (UserNotFoundException e2) {
                                    e2.printStackTrace();
                                    Plugin.addToLog(e2.getMessage());
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                                    mb.open();
                                    return;
                                }


                                if(password.getText().equalsIgnoreCase(verify.getText())){
                                    //add the user to the XMLConfig file
                                    try {
                                        Plugin.getXMLConfig().getUser(user).setPassword(password.getText());
                                        Plugin.getXMLConfig().saveConfigFile();
                                    } catch (IOException e1) {
                                        Plugin.addToLog(e1.toString());
                                    } catch (UserNotFoundException e2) {
                                        MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                        mb.setText("Error");
                                        mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                                        mb.open();
                                        Plugin.addToLog(e2.toString());
                                    }

                                    //destroy the shell
                                    shell.dispose();

                                    //redraw the userTable
                                    GUIMain.redrawTable();

                                }else{
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("New passwords are not the same.");
                                    mb.open();
                                    password.setText("");
                                    verify.setText("");
                                    return;
                                }

                            }
                        });


                        //Button for Cancel
                        Button cancel = new Button(backup_composite, SWT.PUSH);
                        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                        gridData.horizontalSpan = 2;
                        cancel.setLayoutData( gridData);
                        cancel.setText( "Cancel");
                        cancel.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event e) {
                                shell.dispose();
                            }
                        });


                        //Key Listener so that the user can just use ESC to cancel
                        //in the beginning if they did not want to do this
                        userName.addKeyListener(new KeyListener() {
                            public void keyPressed(KeyEvent e) {
                                //Empty
                            }

                            public void keyReleased (KeyEvent e) {
                                switch (e.character){
                                case SWT.ESC:

                                    shell.dispose();
                                    break;

                                }
                            }
                        });

                        //pack and open shell
                        Utilities.centerShellandOpen(shell);

                    }
                });
            }
        };

        addNew_thread.setDaemon(true);
        addNew_thread.run();
    }


    public void editUserInfo(final String user, final boolean isAdmin){
        final Thread editUser_thread = new Thread() {
            public void run() {
                if(Plugin.getDisplay()==null && Plugin.getDisplay().isDisposed())
                    return;
                Plugin.getDisplay().asyncExec( new Runnable() {
                    public void run() {
                        //Shell Initialize

                        final Shell shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                        shell.setImage(ImageRepository.getImage("plus"));

                        //Grid Layout
                        GridLayout layout = new GridLayout();
                        layout.numColumns = 1;
                        shell.setLayout(layout);

                        //composite for shell
                        Composite backup_composite = new Composite(shell,SWT.NULL);

                        //Grid Layout
                        layout = new GridLayout();
                        layout.numColumns = 3;
                        backup_composite.setLayout(layout);

                        //shell title
                        shell.setText("Edit User Information");


                        //User Name Label
                        Label nameLabel = new Label(backup_composite, SWT.NONE);
                        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        nameLabel.setLayoutData( gridData );
                        nameLabel.setText("User Name:");


                        //User Name Input field
                        final Text userName = new Text(backup_composite,SWT.BORDER);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 100;
                        userName.setLayoutData( gridData);
                        userName.setText(user);





                        //Combo Stuff

                        //combo Label
                        Label combo_text = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 1;
                        combo_text.setLayoutData( gridData );
                        combo_text.setText("Select User Type:");

                        final Combo combo = new Combo(backup_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
                        combo.add("Normal User");
                        combo.add("Administrator");


                        try {
                            if(Plugin.getXMLConfig().getUser(user).checkAccess(RemoteConstants.RIGHTS_ADMIN))
                                combo.select(1);
                            else
                                combo.select(0);
                        } catch (UserNotFoundException e2) {
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                            mb.open();
                            Plugin.addToLog(e2.getMessage());
                            e2.printStackTrace();
                        }

                        if(!isAdmin){

                            combo.setEnabled(false);
                        }


                        //---------Directory stuff ------------\\


                        //output directory
                        Label outputDir_text = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 3;
                        outputDir_text.setLayoutData( gridData );
                        outputDir_text.setText("Output Directory for User:");

                        //comp for directory input
                        Composite output_comp = new Composite(backup_composite,SWT.NONE);
                        output_comp.setLayout(new GridLayout(3,false));

                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 3;
                        output_comp.setLayoutData(gridData);



                        //output directory input field
                        final Text outputDir = new Text(output_comp,SWT.BORDER);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 250;
                        outputDir.setLayoutData(gridData);
                        try {
                            outputDir.setText(Plugin.getXMLConfig().getUser(user).getOutputDir());
                        } catch (UserNotFoundException e2) {
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                            mb.open();
                            Plugin.addToLog(e2.getMessage());
                            e2.printStackTrace();
                        }

                        //icon for output directory
                        Label outputDir_icon = new Label(output_comp, SWT.NONE);
                        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                        gridData.horizontalSpan = 1;
                        outputDir_icon.setLayoutData( gridData );
                        outputDir_icon.setToolTipText("Click to choose directory");
                        outputDir_icon.setImage(ImageRepository.getImage("folder"));
                        outputDir_icon.addListener(SWT.MouseDown, new Listener() {
                            public void handleEvent(Event e) {
                                DirectoryDialog dirDialog = new DirectoryDialog(shell);
                                dirDialog.setText("Please Choose Output Directory");
                                dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
                                String selectedDir = dirDialog.open();

                                //need to check if selected dir has files and if so, does it have a comments dir
                                if(selectedDir == null){
                                    return;
                                }else{
                                    outputDir.setText(selectedDir);
                                }


                            }
                        });

                        //auto import directory
                        Label importDir_text = new Label(backup_composite, SWT.NONE);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 3;
                        importDir_text.setLayoutData( gridData );
                        importDir_text.setText("Automatic Import Directory for User:");

                        //comp for directory input
                        Composite importDir_comp = new Composite(backup_composite,SWT.NONE);
                        importDir_comp.setLayout(new GridLayout(3,false));

                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 3;
                        importDir_comp.setLayoutData(gridData);



                        //output directory input field
                        final Text importDir = new Text(importDir_comp,SWT.BORDER);
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        gridData.widthHint = 250;
                        importDir.setLayoutData(gridData);
                        try {
                            importDir.setText(Plugin.getXMLConfig().getUser(user).getAutoImportDir());
                        } catch (UserNotFoundException e2) {
                            MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                            mb.setText("Error");
                            mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                            mb.open();
                            Plugin.addToLog(e2.getMessage());
                            e2.printStackTrace();
                        }

                        //icon for output directory
                        Label importDir_icon = new Label(importDir_comp, SWT.NONE);
                        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                        gridData.horizontalSpan = 1;
                        importDir_icon.setLayoutData( gridData );
                        importDir_icon.setToolTipText("Click to choose directory");
                        importDir_icon.setImage(ImageRepository.getImage("folder"));
                        importDir_icon.addListener(SWT.MouseDown, new Listener() {
                            public void handleEvent(Event e) {
                                DirectoryDialog dirDialog = new DirectoryDialog(shell);
                                dirDialog.setText("Please Choose Automatic Import Directory");
                                dirDialog.setFilterPath(Plugin.getPluginInterface().getPluginDirectoryName());
                                String selectedDir = dirDialog.open();
                                File selectedDir_file = new File(selectedDir);
                                //need to check if selected dir has files and if so, does it have a comments dir
                                if(selectedDir == null){
                                    return;
                                }else if(!selectedDir_file.exists() || !selectedDir_file.isDirectory()){
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("Selected Directory does not exist, please choose a valid directory.");
                                    mb.open();
                                }else{
                                    importDir.setText(selectedDir);
                                }


                            }
                        });

                        //Button for Accept
                        Button commit = new Button(backup_composite, SWT.PUSH);
                        gridData = new GridData(GridData.CENTER);
                        gridData.horizontalSpan = 1;
                        commit.setLayoutData( gridData);
                        commit.setText( "Accept");
                        commit.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event e) {
                                if(userName.getText().equalsIgnoreCase("")      ||
                                        outputDir.getText().equalsIgnoreCase("")||
                                        importDir.getText().equalsIgnoreCase("")){
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("Please fill out all of the information.");
                                    mb.open();
                                    return;
                                }



                                //add the user to the XMLConfig file
                                try {
                                    if(!user.equalsIgnoreCase(userName.getText())){
                                        Plugin.getXMLConfig().renameUser(user,userName.getText());
                                    }

                                    Plugin.getXMLConfig().saveConfigFile();

                                    User currentUser = Plugin.getXMLConfig().getUser(userName.getText());


                                    currentUser.setOutputDir(outputDir.getText());
                                    currentUser.setAutoImportDir(importDir.getText());

                                    if(combo.getSelectionIndex() != 0)
                                        currentUser.setRight(RemoteConstants.RIGHTS_ADMIN);


                                    Plugin.getXMLConfig().saveConfigFile();
                                } catch (IOException e1) {
                                    Plugin.addToLog(e1.toString());
                                } catch (UserNotFoundException e2) {
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("Plugin is reporting a 'User Not Found' error.  \n Possible error in your plugin config file. \nPlease Check your settings and try again.");
                                    mb.open();
                                    Plugin.addToLog(e2.getMessage());
                                    e2.printStackTrace();
                                }catch (DuplicatedUserException e2) {
                                    MessageBox mb = new MessageBox(Plugin.getDisplay().getActiveShell(),SWT.ICON_ERROR);
                                    mb.setText("Error");
                                    mb.setMessage("User name already exists.");
                                    mb.open();
                                    return;
                                }
                                //destroy the shell
                                shell.dispose();

                                //redraw the userTable
                                GUIMain.redrawTable();

                            }

                        });


                        //Button for Cancel
                        Button cancel = new Button(backup_composite, SWT.PUSH);
                        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                        gridData.horizontalSpan = 2;
                        cancel.setLayoutData( gridData);
                        cancel.setText( "Cancel");
                        cancel.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event e) {
                                shell.dispose();
                            }
                        });


                        //Key Listener so that the user can just use ESC to cancel
                        //in the beginning if they did not want to do this
                        userName.addKeyListener(new KeyListener() {
                            public void keyPressed(KeyEvent e) {
                                //Empty
                            }

                            public void keyReleased (KeyEvent e) {
                                switch (e.character){
                                case SWT.ESC:

                                    shell.dispose();
                                    break;

                                }
                            }
                        });

                        //pack and open shell
                        Utilities.centerShellandOpen(shell);

                    }
                });
            }
        };

        editUser_thread.setDaemon(true);
        editUser_thread.run();
    }
}//EOF

