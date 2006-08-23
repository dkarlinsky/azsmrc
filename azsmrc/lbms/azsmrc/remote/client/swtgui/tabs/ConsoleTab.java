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

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.shared.SWTSafeRunnable;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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

	private Level normalLevel = Level.DEBUG;

	private Logger rootLogger;


	//I18N prefix
	public static final String PFX = "tab.consoletab.";

	static {
		dateFormatter = new SimpleDateFormat("[h:mm:ss.SSS] ");
		formatPos = new FieldPosition(0);
	}

	private final Appender consoleAppender = new Appender() {
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#addFilter(org.apache.log4j.spi.Filter)
		 */
		public void addFilter(Filter arg0) {
			// TODO Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#clearFilters()
		 */
		public void clearFilters() {
			// TODO Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#close()
		 */
		public void close() {
			// TODO Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#doAppend(org.apache.log4j.spi.LoggingEvent)
		 */
		public void doAppend(LoggingEvent arg0) {
			if (arg0.getLevel().toInt() >= normalLevel.toInt())
				addText(arg0);

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#getErrorHandler()
		 */
		public ErrorHandler getErrorHandler() {
			// TODO Auto-generated method stub
			return null;
		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#getFilter()
		 */
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#getLayout()
		 */
		public Layout getLayout() {
			// TODO Auto-generated method stub
			return null;
		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#getName()
		 */
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#requiresLayout()
		 */
		public boolean requiresLayout() {
			// TODO Auto-generated method stub
			return false;
		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#setErrorHandler(org.apache.log4j.spi.ErrorHandler)
		 */
		public void setErrorHandler(ErrorHandler arg0) {
			// TODO Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#setLayout(org.apache.log4j.Layout)
		 */
		public void setLayout(Layout arg0) {
			// TODO Auto-generated method stub

		}
		/* (non-Javadoc)
		 * @see org.apache.log4j.Appender#setName(java.lang.String)
		 */
		public void setName(String arg0) {
			// TODO Auto-generated method stub

		}
	};


	private ConsoleTab(CTabFolder parentTab){
		final CTabItem detailsTab = new CTabItem(parentTab, SWT.CLOSE);
		detailsTab.setText(I18N.translate(PFX + "tab.text"));
		rootLogger = Logger.getLogger(RCMain.LOGGER_ROOT);

		final Properties props = RCMain.getRCMain().getProperties();

		bShowDebug = Boolean.parseBoolean(props.getProperty("logger.debug.show", "true"));
		bShowNormal = Boolean.parseBoolean(props.getProperty("logger.normal.show", "true"));
		normalLevel = Level.toLevel(props.getProperty("logger.normal.level", "FINE"));
		PREFERRED_LINES = Integer.parseInt(props.getProperty("console_lines","250"));
		MAX_LINES = 512 + PREFERRED_LINES;

		if (bShowNormal)	rootLogger.addAppender(consoleAppender);

		final Composite parent = new Composite(parentTab, SWT.NONE);
		parent.setLayout(new GridLayout(1,false));
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		parent.setLayoutData(gridData);

		Composite panel = new Composite(parent,SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		panel.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		panel.setLayoutData(gridData);

		GridData gd;

		consoleText = new StyledText(panel, SWT.READ_ONLY | SWT.V_SCROLL
				| SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 4;
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
		buttonPause.setText(I18N.translate(PFX + "buttonPause.text"));
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
						rootLogger.removeAppender(consoleAppender);
					}catch (Exception notToUse){}
				}else{
					if (bShowNormal)    rootLogger.addAppender(consoleAppender);
				}
			}
		});

		buttonAutoScroll = new Button(cLeft, SWT.CHECK);
		buttonAutoScroll.setText(I18N.translate(PFX + "buttonAutoScroll.text"));
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
		Lhistory.setText(I18N.translate(PFX + "labelHistory.text"));

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
		buttonClear.setText(I18N.translate("global.clear"));
		gd = new GridData();
		buttonClear.setLayoutData(gd);
		buttonClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				consoleText.setText("");
			}
		});

		Button writeToFile = new Button(cLeft, SWT.PUSH);
		writeToFile.setText(I18N.translate(PFX + "writeToFile_button.text"));
		gd = new GridData();
		writeToFile.setLayoutData(gd);
		writeToFile.addListener(SWT.Selection, new Listener() {
			public void handleEvent (Event e){
				try{
					if(consoleText.getCharCount() == 0){
						MessageBox messageBox = new MessageBox(cLeft.getShell(), SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(I18N.translate(PFX + "writeToFile.messagebox.title"));
						messageBox.setMessage(I18N.translate(PFX + "writeToFile.messagebox.message"));
						messageBox.open();
						return;
					}

					FileDialog fileDialog = new FileDialog(cLeft.getShell(), SWT.SAVE);
					fileDialog.setText(I18N.translate(PFX + "writeToFile.filedialog.title"));
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
								messageBox.setText(I18N.translate("global.error"));
								messageBox.setMessage(I18N.translate(PFX + "writeToFile.error.text"));
								messageBox.open();
								return;
							}

							final Shell shell = new Shell(SWT.DIALOG_TRIM);
							shell.setLayout(new GridLayout(3,false));
							shell.setText(I18N.translate(PFX + "writeToFile.filedialog.duplicatemessage.title"));
							Label message = new Label(shell,SWT.NULL);
							message.setText(I18N.translate(PFX + "writeToFile.filedialog.duplicatemessage.message"));
							GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
							gridData.horizontalSpan = 3;
							message.setLayoutData(gridData);

							Button overwrite = new Button(shell,SWT.PUSH);
							overwrite.setText(I18N.translate(PFX + "writeToFile.filedialog.duplicatemessage.button.overwrite"));
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
							append.setText(I18N.translate(PFX + "writeToFile.filedialog.duplicatemessage.button.append"));
							append.addListener(SWT.Selection, new Listener(){
								public void handleEvent(Event e)
								{
									shell.close();
									shell.dispose();
									writeToLog(consoleText,fileToSave,true);
								}
							});

							Button cancel = new Button(shell,SWT.PUSH);
							cancel.setText(I18N.translate("global.cancel"));
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
					messageBox.setText(I18N.translate("global.error"));
					messageBox.setMessage(I18N.translate(PFX + "writeToFile.filedialog.duplicatemessage.message"));
					messageBox.open();
				}
			}
		});







		Group cEnd = new Group(panel, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		layout.numColumns = 2;
		cEnd.setLayout(layout);
		gd = new GridData(SWT.TOP, SWT.LEAD, false, false);
		cEnd.setLayoutData(gd);
		cEnd.setText(I18N.translate(PFX + "cEnd.group.text"));




		Label normallabel = new Label(cEnd,SWT.NULL);
		normallabel.setText(I18N.translate(PFX + "cEnd.normalLabel.text"));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		normallabel.setLayoutData(gd);

		final Combo normalCombo = new Combo(cEnd, SWT.DROP_DOWN | SWT.READ_ONLY);
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.7"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.6"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.5"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.4"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.3"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.2"));
		normalCombo.add(I18N.translate(PFX + "cEnd.combo.text.1"));
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


		final Label normalColor = new Label(cEnd, SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 1;
		gd.widthHint = 20;
		normalColor.setLayoutData(gd);
		normalColor.setToolTipText(I18N.translate(PFX + "choose.color.label"));
		normalColor.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		normalColor.setBackground(
				new Color(RCMain.getRCMain().getDisplay(),
						GUI_Utilities.getRGB(
								RCMain.getRCMain().getProperties().getProperty("normal.color", "r000g255b000")
						)
				)
		);

		normalColor.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				RCMain.getRCMain().getProperties().setProperty("normal.color",
						GUI_Utilities.colorChooserDialog(
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("normal.color", "r000g255b000")
								)
						)
				);
				RCMain.getRCMain().saveConfig();
				normalColor.setBackground(
						new Color(RCMain.getRCMain().getDisplay(),
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("normal.color", "r000g255b000")
								)
						)
				);

			}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {}
		});


		//new major color group
		Group gAlert = new Group(panel, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 1;
		layout.numColumns = 2;
		gAlert.setLayout(layout);
		gd = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gAlert.setLayoutData(gd);
		gAlert.setText(I18N.translate(PFX + "gAlert.group.text"));

		Label warningL = new Label(gAlert,SWT.NULL);
		warningL.setText(I18N.translate(PFX + "gAlert.warning.text"));

		final Label warning = new Label(gAlert,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 1;
		gd.widthHint = 20;
		warning.setLayoutData(gd);
		warning.setToolTipText(I18N.translate(PFX + "choose.color.label"));
		warning.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		warning.setBackground(
				new Color(RCMain.getRCMain().getDisplay(),
						GUI_Utilities.getRGB(
								RCMain.getRCMain().getProperties().getProperty("warning.color", "r255g255b000")
						)
				)
		);

		warning.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				RCMain.getRCMain().getProperties().setProperty("warning.color",
						GUI_Utilities.colorChooserDialog(
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("warning.color", "r255g255b000")
								)
						)
				);
				RCMain.getRCMain().saveConfig();
				warning.setBackground(
						new Color(RCMain.getRCMain().getDisplay(),
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("warning.color", "r255g255b000")
								)
						)
				);

			}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {}
		});


		Label severeL = new Label(gAlert,SWT.NULL);
		severeL.setText(I18N.translate(PFX + "gAlert.severe.text"));

		final Label severe = new Label(gAlert,SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 1;
		gd.widthHint = 20;
		severe.setLayoutData(gd);
		severe.setToolTipText(I18N.translate(PFX + "choose.color.label"));
		severe.setCursor(RCMain.getRCMain().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		severe.setBackground(
				new Color(RCMain.getRCMain().getDisplay(),
						GUI_Utilities.getRGB(
								RCMain.getRCMain().getProperties().getProperty("severe.color", "r255g000b000")
						)
				)
		);

		severe.addMouseListener(new MouseListener(){

			public void mouseDoubleClick(MouseEvent arg0) {
				RCMain.getRCMain().getProperties().setProperty("severe.color",
						GUI_Utilities.colorChooserDialog(
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("severe.color", "r255g000b000")
								)
						)
				);
				RCMain.getRCMain().saveConfig();
				severe.setBackground(
						new Color(RCMain.getRCMain().getDisplay(),
								GUI_Utilities.getRGB(
										RCMain.getRCMain().getProperties().getProperty("severe.color", "r255g000b000")
								)
						)
				);

			}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {}
		});







		//Dispose Listener for tab
		detailsTab.addDisposeListener(new DisposeListener (){

			public void widgetDisposed(DisposeEvent arg0) {
				rootLogger.removeAppender(consoleAppender);
			}
		});

		detailsTab.setControl(parent);
		parentTab.setSelection(detailsTab);
	}

	public void addText(final LoggingEvent logEvent){
		if (bPaused)
			return;

		try {
			final Display display = RCMain.getRCMain().getDisplay();
			if(display == null || display.isDisposed()) return;
			display.asyncExec(new SWTSafeRunnable(){
				public void runSafe() {

					try{
						if(consoleText == null || consoleText.isDisposed()) return;
						int nbLinesBefore = consoleText.getLineCount();
						if (nbLinesBefore > MAX_LINES)
							consoleText.replaceTextRange(0, consoleText
									.getOffsetAtLine(PREFERRED_LINES), "");

						final StringBuffer buf = new StringBuffer();
						dateFormatter.format(logEvent.timeStamp,buf,formatPos);
						buf.append("{").append(logEvent.getLoggerName()).append(" : ").append(logEvent.getLevel()).append("} ");
						buf.append(logEvent.getMessage());
						buf.append('\n');

						if(buf.length() <=0 || buf.length() > 1000) return;
						//System.out.println("To Console: "+buf);
						consoleText.append(buf.toString());

						int nbLinesNow = consoleText.getLineCount();


						if(logEvent.getLevel().toInt() == Level.WARN_INT)
							consoleText.setLineBackground(nbLinesBefore - 1, nbLinesNow
									- nbLinesBefore, new Color(RCMain.getRCMain().getDisplay(),
											GUI_Utilities.getRGB(
													RCMain.getRCMain().getProperties().getProperty("warning.color", "r255g255b000")
											)
									)
							);
						else if(logEvent.getLevel().toInt() == Level.ERROR_INT)
							consoleText.setLineBackground(nbLinesBefore - 1, nbLinesNow
									- nbLinesBefore, new Color(RCMain.getRCMain().getDisplay(),
											GUI_Utilities.getRGB(
													RCMain.getRCMain().getProperties().getProperty("severe.color", "r255g000b000")
											)
									)
							);
						else
							consoleText.setLineBackground(nbLinesBefore - 1, nbLinesNow
									- nbLinesBefore, new Color(RCMain.getRCMain().getDisplay(),
											GUI_Utilities.getRGB(
													RCMain.getRCMain().getProperties().getProperty("normal.color", "r000g255b000")
											)
									)
							);


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
		int test = level.toInt();
		if(test == Level.FATAL_INT)
			value = 6;
		else if(test == Level.ERROR_INT)
			value = 5;
		else if(test == Level.WARN_INT)
			value = 4;
		else if(test == Level.INFO_INT)
			value = 3;
		else if(test == Level.DEBUG_INT)
			value = 2;
		else if(test == Level.TRACE_INT)
			value = 1;
		else if(test == Level.ALL_INT)
			value = 0;
		return value;
	}

	private Level integerToLevel(int test){
		Level level = Level.ALL;

		if(test == 6)
			level = Level.FATAL;
		else if(test == 5)
			level = Level.ERROR;
		else if(test == 4)
			level = Level.WARN;
		else if(test == 3)
			level = Level.INFO;
		else if(test == 2)
			level = Level.DEBUG;
		else if(test == 1)
			level = Level.TRACE;
		else if(test == 0)
			level = Level.ALL;
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


	public static void open(final CTabFolder parentTab, final boolean bIsFocused){
		Display display = RCMain.getRCMain().getDisplay();
		if(display == null) return;
		display.syncExec(new Runnable(){
			public void run() {
				CTabItem[] tabs = parentTab.getItems();
				for(CTabItem tab:tabs){
					if(tab.getText().equalsIgnoreCase(I18N.translate(PFX + "tab.text"))){
						if(bIsFocused)
							parentTab.setSelection(tab);
						else
							if(tabs.length > 0)
								parentTab.setSelection(tabs[0]);
							else
								parentTab.setSelection(tab);
						return;
					}
				}
				new ConsoleTab(parentTab);

			}

		});
	}

}//EOF
