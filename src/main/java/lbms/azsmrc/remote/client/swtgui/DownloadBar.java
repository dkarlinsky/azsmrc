package lbms.azsmrc.remote.client.swtgui;

import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class DownloadBar {
	//Constants -- maybe allow the user to change these later on
	private static int fontSize = 8; //height in pixels of the font for the labels
	private static int nameWidth = 30; //maximum characters of the name
	private static int speedWidth = 19; //Allow for max of D:xxxkb/s U:xxxkb/s
	private static int seedsWidth = 24; //Allow for max of (xxx)xxxx S - xxx(xxxx)L

	//Shell
	private Shell shell;

	//Movement
	private boolean moving;
	private int xPressed, yPressed;

	//Labels for torrent
	private Label lName, lSpeed, lSeeds;
	private String sName = "12345678901234567890";
	private String sSpeed = "D:0kb/s U:0kb/s";
	private String sSeeds = "0(0)S - 0(0)L";
	private Display display;


	//Nice small font for use by text widgets
	//Be sure to dispose of me!
	private Font font;
	private Font boldFont;

	//Average size of the font
	private int averageFontWidth = 0;

	public DownloadBar(Display _display){
		display = _display;
		//Shell -- On Top
		shell = new Shell(display, SWT.ON_TOP);
		//gridlayout
		GridLayout gl = new GridLayout();
		gl.numColumns = 7;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		shell.setLayout(gl);


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


		GC gc = new GC (shell);
		averageFontWidth = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();

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

		//TODO - X label -- change to pic
		final Label xLabel = new Label(shell, SWT.NULL);
		xLabel.setText("X");
		xLabel.setFont(boldFont);
		xLabel.addListener(SWT.MouseUp, new Listener (){
			public void handleEvent(Event arg0) {
				if(shell != null || !shell.isDisposed())
					shell.close();
			}
		});

		//Separator
		Label sep = new Label(shell, SWT.SEPARATOR);
		GridData gd = new GridData();
		gd.heightHint = fontSize;
		sep.setLayoutData(gd);


		//Name Label
		lName = new Label(shell, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = (averageFontWidth * nameWidth);
		lName.setLayoutData(gd);
		lName.setText(trimName(sName, nameWidth));
		lName.setFont(boldFont);

		//Separator
		Label sep2 = new Label(shell, SWT.SEPARATOR);
		gd = new GridData();
		gd.heightHint = fontSize;
		sep2.setLayoutData(gd);

		//Speed
		lSpeed = new Label(shell, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = (averageFontWidth * speedWidth);
		lSpeed.setLayoutData(gd);
		lSpeed.setText(sSpeed);
		lSpeed.setFont(font);

		//Separator
		Label sep3 = new Label(shell, SWT.SEPARATOR);
		gd = new GridData();
		gd.heightHint = fontSize;
		sep3.setLayoutData(gd);

		lSeeds = new Label(shell, SWT.NULL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.widthHint = (averageFontWidth * seedsWidth);
		lSeeds.setLayoutData(gd);
		lSeeds.setText(sSeeds);
		lSeeds.setFont(font);


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
			Control[] controls = shell.getChildren();
			for(Control control:controls){
				if(control != xLabel){
					control.addMouseListener(mListener);
					control.addMouseMoveListener(mMoveListener);
				}
			}

	}

	public void open(){
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){

			@Override
			public void runSafe() {
				if(shell != null || !shell.isDisposed()){
					shell.layout();
					shell.pack();
					shell.open();
				}
			}
		});
	}


	/**
	 * Public method to set the name of the torrent
	 * @param name
	 */
	public void setName(String name){
		sName = name;
		redrawAll();
	}


	/**
	 * Public method to just set the speed of the torrent
	 * Be sure it is in the following format: D:0kb/s U:0kb/s
	 * @param speeds
	 */
	public void setSpeeds(String speeds){
		sSpeed = speeds;
		redrawAll();
	}

	/**
	 * Public method to set the number of seeds and leechers 
	 * Be sure it is in the following format: 0(0)S - 0(0)L
	 * @param seedsLeecher
	 */
	public void setSeedsLeechers(String seedsLeechers){
		sSeeds = seedsLeechers;
		redrawAll();
	}

	/**
	 * Public method to set all info and redraw the labels
	 * For Speed be sure it is in the following format:  D:0kb/s U:0kb/s
	 * For seedsLeecerhs:  0(0)S - 0(0)L
	 * @param name
	 * @param speeds
	 * @param seedsLeechers
	 */
	public void setAll(String name, String speeds, String seedsLeechers){
		sName = name;
		sSpeed = speeds;
		sSeeds = seedsLeechers;
		redrawAll();
	}
	/**
	 * Private method to trim the name to the given size and
	 * add '...' after it	
	 * @param name
	 * @param size
	 * @return
	 */
	private String trimName(String name, int size){
		if(name.length() > size){
			name = name.substring(0, size - 3) + "...";
		}
		return name;
	}


	private void redrawAll(){
		if(display == null || display.isDisposed()) return;
		display.asyncExec(new SWTSafeRunnable(){

			@Override
			public void runSafe() {
				//set all the texts
				if(lName != null || !lName.isDisposed()){
					lName.setText(trimName(sName, nameWidth));
					lName.setToolTipText(sName);
				}
				if(lSpeed != null || !lSpeed.isDisposed())
					lSpeed.setText(sSpeed);
				if(lSeeds != null || !lSeeds.isDisposed())
					lSeeds.setText(sSeeds);

				//Re-layout the shell
				if(shell != null || !shell.isDisposed())
					shell.layout();
			}

		});
	}
}
