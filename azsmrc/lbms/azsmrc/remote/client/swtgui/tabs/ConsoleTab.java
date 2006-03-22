/*
 * Created on Feb 18, 2006
 * Created by omschaub
 * Adaptation of LoggerViewer.java from core
 */
package lbms.azsmrc.remote.client.swtgui.tabs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;




public class ConsoleTab {


	private int PREFERRED_LINES = 256;

	private int MAX_LINES = 512 + PREFERRED_LINES;

	private static final SimpleDateFormat dateFormatter;

	private static final FieldPosition formatPos;

	private String defaultPath;

	private StyledText consoleText = null;

	private Button buttonAutoScroll = null;

	private boolean bPaused = false;

	private boolean bAutoScroll = true;

	private boolean bShowDebug = true;
	private boolean bShowNormal = true;

	private Level debugLevel = Level.FINE;
	private Level normalLevel = Level.FINE;

	private Logger normalLogger;
	private Logger debugLogger;

	static {
		dateFormatter = new SimpleDateFormat("[h:mm:ss.SSS] ");
		formatPos = new FieldPosition(0);
	}

	private final Handler normalConsoleHandler = new Handler() {
		@Override
		public void close() throws SecurityException {}
		@Override
		public void flush() {}
		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= normalLevel.intValue())
				addText(record);
		}
	};

	private final Handler debugConsoleHandler = new Handler() {
		@Override
		public void close() throws SecurityException {}
		@Override
		public void flush() {}
		@Override
		public void publish(LogRecord record) {
			if (record.getLevel().intValue() >= debugLevel.intValue())
				addText(record);

		}
	};


	public ConsoleTab(CTabFolder parentTab){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText("Console");
		normalLogger = RCMain.getRCMain().getNormalLogger();
		debugLogger = RCMain.getRCMain().getDebugLogger();

		final Properties props = RCMain.getRCMain().getProperties();

		bShowDebug = Boolean.parseBoolean(props.getProperty("logger.debug.show", "true"));
		bShowNormal = Boolean.parseBoolean(props.getProperty("logger.normal.show", "true"));
		normalLevel = Level.parse(props.getProperty("logger.normal.level", "FINE"));
		debugLevel = Level.parse(props.getProperty("logger.debug.level", "FINE"));
        PREFERRED_LINES = Integer.parseInt(props.getProperty("console_lines","250"));
        MAX_LINES = 512 + PREFERRED_LINES;

		if (bShowNormal)	normalLogger.addHandler(normalConsoleHandler);
		if (bShowDebug) 	debugLogger.addHandler(debugConsoleHandler);

		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		System.out.println("normal level:" + debugLevel.intValue());

		Composite panel = new Composite(parent,SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		panel.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		panel.setLayoutData(gridData);

		GridData gd;

		consoleText = new StyledText(panel, SWT.READ_ONLY | SWT.V_SCROLL
				| SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		consoleText.setLayoutData(gd);


		consoleText.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				GC gc = new GC(consoleText);
				int charWidth = gc.getFontMetrics().getAverageCharWidth();
				gc.dispose();

				int areaWidth = consoleText.getBounds().width;
				consoleText.setTabs(areaWidth / 6 / charWidth);
			}
		});

		ScrollBar sb = consoleText.getVerticalBar();
		sb.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				bAutoScroll = false;
				if (buttonAutoScroll != null && !buttonAutoScroll.isDisposed())
					buttonAutoScroll.setSelection(false);
			}
		});

		final Composite cLeft = new Composite(panel, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
        layout.numColumns = 2;
		cLeft.setLayout(layout);
		gd = new GridData(SWT.TOP, SWT.LEAD, false, false);
		cLeft.setLayoutData(gd);

		Button buttonPause = new Button(cLeft, SWT.CHECK);
		buttonPause.setText("Disable Logging");
		gd = new GridData();
        gd.horizontalSpan = 2;
		buttonPause.setLayoutData(gd);
		buttonPause.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == null || !(e.widget instanceof Button))
					return;
				Button btn = (Button) e.widget;
				bPaused = btn.getSelection();
				if (bPaused) {
                    try{
                        normalLogger.removeHandler(normalConsoleHandler);
                        debugLogger.removeHandler(debugConsoleHandler);
                    }catch (Exception notToUse){}
				}else{
                    if (bShowNormal)    normalLogger.addHandler(normalConsoleHandler);
                    if (bShowDebug)     debugLogger.addHandler(debugConsoleHandler);
                }
			}
		});

		buttonAutoScroll = new Button(cLeft, SWT.CHECK);
		buttonAutoScroll.setText("Auto Scroll");
		gd = new GridData();
        gd.horizontalSpan = 2;
		buttonAutoScroll.setLayoutData(gd);
		buttonAutoScroll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == null || !(e.widget instanceof Button))
					return;
				Button btn = (Button) e.widget;
				bAutoScroll = btn.getSelection();
			}
		});
		buttonAutoScroll.setSelection(true);


        //preferred lines in history of console
        Composite cLines = new Composite(cLeft,SWT.NULL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        cLines.setLayout(gridLayout);
        gd = new GridData();
        gd.horizontalSpan = 2;
        cLines.setLayoutData(gd);


        Label Lhistory = new Label(cLines,SWT.NULL);
        Lhistory.setText("History Size");

        final Spinner spin = new Spinner(cLines,SWT.BORDER);
        spin.setMaximum(10000);
        spin.setMinimum(250);
        spin.setIncrement(10);
        spin.setPageIncrement(100);
        spin.setSelection(PREFERRED_LINES);
        spin.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent arg0) {
                int size = spin.getSelection();
                if(size > 10000)
                    size = 10000;
                else if(size < 250)
                    size = 250;

                props.setProperty("console_lines", String.valueOf(size));
                RCMain.getRCMain().saveConfig();

                PREFERRED_LINES = size;
                MAX_LINES = 512 + PREFERRED_LINES;
            }
        });



        //Button clear
		Button buttonClear = new Button(cLeft, SWT.PUSH);
		buttonClear.setText("Clear");
		gd = new GridData();
		buttonClear.setLayoutData(gd);
		buttonClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				consoleText.setText("");
			}
		});

        Button writeToFile = new Button(cLeft, SWT.PUSH);
        writeToFile.setText("Write Log to file");
        gd = new GridData();
        writeToFile.setLayoutData(gd);
        writeToFile.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e){
                try{
                    if(consoleText.getCharCount() == 0){
                        MessageBox messageBox = new MessageBox(cLeft.getShell(), SWT.ICON_ERROR | SWT.OK);
                        messageBox.setText("Log is Empty");
                        messageBox.setMessage("The Log is empty, therefore nothing can be written to a file.");
                        messageBox.open();
                        return;
                    }

                    FileDialog fileDialog = new FileDialog(cLeft.getShell(), SWT.SAVE);
                    fileDialog.setText("Please choose a file to save the information to");
                    String[] filterExtensions = {"*.txt","*.log","*.*"};
                    fileDialog.setFilterExtensions(filterExtensions);
                    if(defaultPath != null)
                        fileDialog.setFilterPath(defaultPath);
                    String selectedFile = fileDialog.open();
                    if(selectedFile != null){
                        final File fileToSave = new File(selectedFile);

                        defaultPath = fileToSave.getParent();
                        if(fileToSave.exists()){
                            if(!fileToSave.canWrite()){
                                MessageBox messageBox = new MessageBox(cLeft.getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBox.setText("Error writing to file");
                                messageBox.setMessage("Your computer is reporting that the selected file cannot be written to, please retry this operation and select a different file");
                                messageBox.open();
                                return;
                            }

                            final Shell shell = new Shell(SWT.DIALOG_TRIM);
                            shell.setLayout(new GridLayout(3,false));
                            shell.setText("File Exists");
                            Label message = new Label(shell,SWT.NULL);
                            message.setText("Your selected file already exists. \n" +
                                                        "Choose 'Overwrite' to overwrite it, deleting the original contents \n" +
                                                        "Choose 'Append' to append the information to the existing file \n" +
                                                        "Choose 'Cancel' to abort this action all together\n\n");
                            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                            gridData.horizontalSpan = 3;
                            message.setLayoutData(gridData);

                            Button overwrite = new Button(shell,SWT.PUSH);
                            overwrite.setText("Overwrite");
                            overwrite.addListener(SWT.Selection, new Listener(){
                                public void handleEvent(Event e)
                                {
                                    shell.close();
                                    shell.dispose();
                                    writeToLog(consoleText,fileToSave,false);
                                }
                            });

                            gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                            overwrite.setLayoutData(gridData);


                            Button append = new Button(shell,SWT.PUSH);
                            append.setText("Append");
                            append.addListener(SWT.Selection, new Listener(){
                                public void handleEvent(Event e)
                                {
                                    shell.close();
                                    shell.dispose();
                                    writeToLog(consoleText,fileToSave,true);
                                }
                            });

                            Button cancel = new Button(shell,SWT.PUSH);
                            cancel.setText("Cancel");
                            cancel.addListener(SWT.Selection, new Listener(){
                                public void handleEvent(Event e)
                                {
                                    shell.close();
                                    shell.dispose();
                                }
                            });

                            gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
                            cancel.setLayoutData(gridData);
                            overwrite.addKeyListener(new KeyListener() {
                                public void keyPressed(KeyEvent e) {
                                }
                                public void keyReleased (KeyEvent e) {
                                    if (e.character == SWT.ESC){
                                        shell.close();
                                        shell.dispose();
                                    }

                                }
                            });


                            GUI_Utilities.centerShellandOpen(shell);
                        }else{
                            fileToSave.createNewFile();
                            writeToLog(consoleText,fileToSave,true);
                        }


                    }


                }catch (Exception f){
                    f.printStackTrace();
                    MessageBox messageBox = new MessageBox(cLeft.getShell(), SWT.ICON_ERROR | SWT.OK);
                    messageBox.setText("Error writing to file");
                    messageBox.setMessage("Your computer is reporting that the selected file cannot be written to, please retry this operation and select a different file");
                    messageBox.open();
                }
            }
        });





		Group cMiddle = new Group(panel, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		cMiddle.setLayout(layout);
		gd = new GridData(SWT.TOP, SWT.LEAD, false, false);
		cMiddle.setLayoutData(gd);
		cMiddle.setText("Log Filters");

		Button buttonShowDebug = new Button(cMiddle, SWT.CHECK);
		buttonShowDebug.setText("Show Debug Log");
		gd = new GridData();
		buttonShowDebug.setLayoutData(gd);
		buttonShowDebug.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == null || !(e.widget instanceof Button))
					return;
				Button btn = (Button) e.widget;
				bShowDebug = btn.getSelection();
                props.setProperty("logger.debug.show", bShowDebug ? "true" : "false");
                RCMain.getRCMain().saveConfig();
			}
		});
		buttonShowDebug.setSelection(bShowDebug);

		Button buttonShowNormal = new Button(cMiddle, SWT.CHECK);
		buttonShowNormal.setText("Show Normal Log");
		gd = new GridData();
		buttonShowNormal.setLayoutData(gd);
		buttonShowNormal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget == null || !(e.widget instanceof Button))
					return;
				Button btn = (Button) e.widget;
				bShowNormal = btn.getSelection();
                props.setProperty("logger.normal.show", bShowNormal ? "true" : "false");
                RCMain.getRCMain().saveConfig();
			}
		});
		buttonShowNormal.setSelection(bShowNormal);


		Group cEnd = new Group(panel, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		cEnd.setLayout(layout);
		gd = new GridData(SWT.TOP, SWT.LEAD, false, false);
		cEnd.setLayoutData(gd);
		cEnd.setText("Filter amount shown in the console");

		Label debuglabel = new Label(cEnd,SWT.NULL);
        debuglabel.setText("Debug Log Level:");

        final Combo debugCombo = new Combo(cEnd, SWT.DROP_DOWN | SWT.READ_ONLY);
        debugCombo.add("7 - Show Finest and above (shows most)");
        debugCombo.add("6 - Show Finer and above");
        debugCombo.add("5 - Show Fine and above");
        debugCombo.add("4 - Show Config and above");
        debugCombo.add("3 - Show Info and above");
        debugCombo.add("2 - Show Warnings and above");
        debugCombo.add("1 - Show only SEVERE (shows least)");
        debugCombo.select(levelToInteger(debugLevel));
        debugCombo.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent arg0) {
                debugLevel = integerToLevel(debugCombo.getSelectionIndex());
                props.setProperty("logger.debug.level", debugLevel.toString());
                RCMain.getRCMain().saveConfig();
            }
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

        });

        Label normallabel = new Label(cEnd,SWT.NULL);
        normallabel.setText("Normal Log Level:");
        final Combo normalCombo = new Combo(cEnd, SWT.DROP_DOWN | SWT.READ_ONLY);
        normalCombo.add("7 - Show Finest and above (shows most)");
        normalCombo.add("6 - Show Finer and above");
        normalCombo.add("5 - Show Fine and above");
        normalCombo.add("4 - Show Config and above");
        normalCombo.add("3 - Show Info and above");
        normalCombo.add("2 - Show Warnings and above");
        normalCombo.add("1 - Show only SEVERE (shows least)");
        normalCombo.select(levelToInteger(normalLevel));
        normalCombo.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent arg0) {
                normalLevel = integerToLevel(normalCombo.getSelectionIndex());
                props.setProperty("logger.normal.level", normalLevel.toString());
                RCMain.getRCMain().saveConfig();
            }
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }

        });



		detailsTab.addDisposeListener(new DisposeListener (){

			public void widgetDisposed(DisposeEvent arg0) {
				normalLogger.removeHandler(normalConsoleHandler);
				debugLogger.removeHandler(debugConsoleHandler);

			}

		});

		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	public void addText(final LogRecord record){
		if (bPaused)
			return;

		try {
			final Display display = RCMain.getRCMain().getDisplay();
			if(display == null || display.isDisposed()) return;
			display.asyncExec(new Runnable(){
				public void run() {

					try{
						if(consoleText == null || consoleText.isDisposed()) return;
                        int nbLinesBefore = consoleText.getLineCount();
						if (nbLinesBefore > MAX_LINES)
							consoleText.replaceTextRange(0, consoleText
									.getOffsetAtLine(PREFERRED_LINES), "");

						final StringBuffer buf = new StringBuffer();
						dateFormatter.format(record.getMillis(),buf,formatPos);
						buf.append("{").append(record.getLoggerName()).append(" : ").append(record.getLevel()).append("} ");
						buf.append(record.getMessage());
						buf.append('\n');

						if(buf.length() <=0 || buf.length() > 1000) return;
						System.out.println("To Console: "+buf);
						consoleText.append(buf.toString());

						int nbLinesNow = consoleText.getLineCount();
						int colorIdx = -1;
						//Color Based On Log
                        if (record.getLoggerName().equalsIgnoreCase("lbms.azsmrc.ff.debug"))
							colorIdx = SWT.COLOR_GRAY;
						else
							colorIdx = SWT.COLOR_GREEN;

                        //Change the color if severe or warning
                        if(levelToInteger(record.getLevel()) == 5)
                            colorIdx = SWT.COLOR_YELLOW;
                        else if(levelToInteger(record.getLevel()) == 6)
                            colorIdx = SWT.COLOR_RED;

						if (colorIdx >= 0)
							consoleText.setLineBackground(nbLinesBefore - 1, nbLinesNow
									- nbLinesBefore, display.getSystemColor(colorIdx));

						if (bAutoScroll)
							consoleText.setSelection(consoleText.getText().length());

					}catch (SWTException e){
						e.printStackTrace();
					}
				}
			});


		} catch (Exception e){
			e.printStackTrace();
		}


    }

    private int levelToInteger(Level level){
        int value = 3;
        int test = level.intValue();
        if(test == Level.SEVERE.intValue())
            value = 6;
        else if(test == Level.WARNING.intValue())
            value = 5;
        else if(test == Level.INFO.intValue())
            value = 4;
        else if(test == Level.CONFIG.intValue())
            value = 3;
        else if(test == Level.FINE.intValue())
            value = 2;
        else if(test == Level.FINER.intValue())
            value = 1;
        else if(test == Level.FINEST.intValue())
            value = 0;
        return value;
    }

    private Level integerToLevel(int test){
        Level level = Level.FINE;

        if(test == 6)
            level = Level.SEVERE;
        else if(test == 5)
            level = Level.WARNING;
        else if(test == 4)
            level = Level.INFO;
        else if(test == 3)
            level = Level.CONFIG;
        else if(test == 2)
            level = Level.FINE;
        else if(test == 1)
            level = Level.FINER;
        else if(test == 0)
            level = Level.FINEST;
        return level;
    }

    /**
     * Utility to write the log file
     * @param StyledText
     * @param File logFile
     * @param boolean append (true to append to the file, false to overwrite it)
     */
    private void writeToLog(final StyledText styledText, final File logFile, final boolean append){
        if(styledText == null)
            return;

        //set up the writer
        try{

            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(logFile, append));
            bufWriter.write(styledText.getText());
            bufWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }


}//EOF
