package lbms.azsmrc.remote.client.swtgui;


import lbms.azsmrc.remote.client.events.GlobalStatsListener;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.SWTSafeRunnable;
import lbms.tools.ExtendedProperties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class StatusShell {
	//Constants -- maybe allow the user to change these later on
	private static int fontSize = 8; //height in pixels of the font for the labels

	//Shell
	private Shell shell;

	//Movement
	private boolean moving;
	private int xPressed, yPressed;

	//Labels for torrent
	private CLabel lSpeedDown, lSpeedUp, lSeeds;
	private String sSpeedDown = "xxx kb/s";
	private String sSpeedUp =  "xxx kb/s";
	private String sSeeds = "0(0)S - 0(0)L";
	private Display display;


	//Nice small font for use by text widgets
	//Be sure to dispose of me!
	private Font font;
	private Font boldFont;

	private GlobalStatsListener gsl = new GlobalStatsListener () {
		/* (non-Javadoc)
		 * @see lbms.azsmrc.remote.client.events.GlobalStatsListener#updateStats(int, int, int, int, int, int)
		 */
		public void updateStats(final int d, final int u, final int seeding, final int seedqueue, final int downloading, final int downloadqueue) {
			if(display != null)
				display.syncExec(new SWTSafeRunnable() {
					public void runSafe() {
						instance.setAll(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(u),
								DisplayFormatters.formatByteCountToBase10KBEtcPerSec(d),
								"S: "
								+ seeding + " ("+ seedqueue+") "
								+ "D: "
								+ downloading + " ("+ downloadqueue+") "
								);
					}
				});
		}
	};



	//Instance
	private static StatusShell instance;

	public StatusShell(){
		//Set the instance
		instance = this;

		//set global display
		display = RCMain.getRCMain().getDisplay();
		//Shell -- On Top
		shell = new Shell(display, SWT.ON_TOP);
		//formlayout
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		shell.setLayout(gl);
		shell.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		//Pull the system font and shrink it down to size 8 so that
		//we can cram the most out of this as possible
		FontData[] fontData = display.getSystemFont().getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(fontSize);
		}
		font = new Font(display, fontData);

		//Do the same trick for a bolded version
		FontData[] fontData_bold = font.getFontData();
		for (int i = 0; i < fontData_bold.length; i++) {
			fontData[i].setStyle(SWT.BOLD);
		}

		boldFont = new Font(display, fontData_bold);


		//Dispose listener on the shell -- so we can clean up
		//memory on all exits
		shell.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent arg0) {
				if(font != null || !font.isDisposed())
					font.dispose();
				if(boldFont != null || !boldFont.isDisposed())
					boldFont.dispose();


			}

		});


		Composite parentTop = new Composite(shell, SWT.NULL);
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		parentTop.setLayout(gl);
		parentTop.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 150;
		//gd.heightHint = (16)+5; //height of the icons -- 2*16pixels
		parentTop.setLayoutData(gd);



		//Seeds and Leechers
		lSeeds = new CLabel(parentTop, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lSeeds.setLayoutData(gd);
		lSeeds.setText(sSeeds);
		lSeeds.setFont(font);
		lSeeds.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		//X label -- with pic
		final Label xLabel = new Label(parentTop, SWT.NULL);
		xLabel.setImage(ImageRepository.getImage("statusShell_close"));
		xLabel.addListener(SWT.MouseUp, new Listener (){
			public void handleEvent(Event arg0) {
				if(shell != null || !shell.isDisposed())
					shell.close();
			}
		});


		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		xLabel.setLayoutData(gd);


		Composite parentBottom = new Composite(shell, SWT.NULL);
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.makeColumnsEqualWidth = true;
		parentBottom.setLayout(gl);
		parentBottom.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		parentBottom.setLayoutData(gd);


		//Speed Down
		lSpeedDown = new CLabel(parentBottom, SWT.NULL);
		lSpeedDown.setText(sSpeedDown);
		lSpeedDown.setImage(ImageRepository.getImage("statusbar_down"));
		lSpeedDown.setFont(font);
		lSpeedDown.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		gd = new GridData();
		lSpeedDown.setLayoutData(gd);

		//Speed Up
		lSpeedUp = new CLabel(parentBottom, SWT.NULL);
		lSpeedUp.setText(sSpeedUp);
		lSpeedUp.setImage(ImageRepository.getImage("statusbar_up"));
		lSpeedUp.setFont(font);
		lSpeedUp.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		gd = new GridData();
		lSpeedUp.setLayoutData(gd);



		  MouseListener mListener = new MouseAdapter() {
			  public void mouseDown(MouseEvent e) {
				xPressed = e.x;
				yPressed = e.y;
				moving = true;
				//System.out.println("Position : " + xPressed + " , " + yPressed);
			  }

			  public void mouseUp(MouseEvent e) {
				moving = false;
			  }

			};
			MouseMoveListener mMoveListener = new MouseMoveListener() {
			  public void mouseMove(MouseEvent e) {
				if (moving) {
				  int dX = xPressed - e.x;
				  int dY = yPressed - e.y;
				  //System.out.println("dX,dY : " + dX + " , " + dY);
				  Point currentLoc = shell.getLocation();
				  currentLoc.x -= dX;
				  currentLoc.y -= dY;
				  shell.setLocation(currentLoc);
				  //setSnapLocation(currentLoc);
				  //System.out.println("Position : " + xPressed + " , " + yPressed);
				}
			  }
			};


			shell.addMouseListener(mListener);
			shell.addMouseMoveListener(mMoveListener);

			parentTop.addMouseListener(mListener);
			parentTop.addMouseMoveListener(mMoveListener);
			parentBottom.addMouseListener(mListener);
			parentBottom.addMouseMoveListener(mMoveListener);
			lSeeds.addMouseListener(mListener);
			lSeeds.addMouseMoveListener(mMoveListener);
			lSpeedDown.addMouseListener(mListener);
			lSpeedDown.addMouseMoveListener(mMoveListener);
			lSpeedUp.addMouseListener(mListener);
			lSpeedUp.addMouseMoveListener(mMoveListener);

			//Restore position
			ExtendedProperties props = RCMain.getRCMain().getProperties();
			shell.setLocation(new Point (props.getPropertyAsInt("StatusShell.x"),props.getPropertyAsInt("StatusShell.y")));

			//Add the listener for the Data
			RCMain.getRCMain().getClient().addGlobalStatsListener(gsl);
			shell.addShellListener(new ShellAdapter() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.ShellAdapter#shellClosed(org.eclipse.swt.events.ShellEvent)
				 */
				@Override
				public void shellClosed(ShellEvent e) {
					RCMain.getRCMain().getClient().removeGlobalStatsListener(gsl);
					ExtendedProperties props = RCMain.getRCMain().getProperties();
					Point currentLoc = shell.getLocation();
					props.setProperty("StatusShell.x", currentLoc.x);
					props.setProperty("StatusShell.y", currentLoc.y);
				}
			});

			//Open the shell
			shell.layout();
			shell.pack();
			shell.open();

			//Request an update
			RCMain.getRCMain().getClient().sendGetGlobalStats();

	}


	/**
	 * Public method to set all info and redraw the labels
	 * For Speed be sure it is in the following format:  0kb/s
	 * For seedsLeecerhs:  0(0)S - 0(0)L
	 * @param speedUp
	 * @param speedDown
	 * @param seedsLeechers
	 */
	public void setAll(String speedUp, String speedDown, String seedsLeechers){
		sSpeedUp = speedUp;
		sSpeedDown = speedDown;
		sSeeds = seedsLeechers;
		redrawAll();
	}



	private void redrawAll(){
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){

			@Override
			public void runSafe() {
				//set all the texts
				if(lSpeedDown != null || !lSpeedDown.isDisposed())
					lSpeedDown.setText(sSpeedDown);
				if(lSpeedUp != null || !lSpeedUp.isDisposed())
					lSpeedUp.setText(sSpeedUp);
				if(lSeeds != null || !lSeeds.isDisposed())
					lSeeds.setText(sSeeds);

				//Re-layout the shell
				if(shell != null || !shell.isDisposed())
					shell.layout();
			}

		});
	}

	/**
	 * Static open method
	 */
	public static void open(){
		if (instance == null || instance.shell == null || instance.shell.isDisposed()){
			new StatusShell();
		}else
			instance.shell.setActive();
	}

	/**
	 * Checks to see if the status shell is open or not
	 * 
	 */
	public static boolean isOpen(){
		if (instance == null || instance.shell == null || instance.shell.isDisposed())
			return false;
		else
			return true;
	}

	/**
	 * Returns that instance.. warning, this can be null, be sure to check if it is 
	 * open before using
	 * @return instance
	 */
	public static StatusShell getInstance(){
		return instance;
	}
}
