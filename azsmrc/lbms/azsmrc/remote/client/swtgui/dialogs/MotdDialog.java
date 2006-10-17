package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.util.Constants;
import org.gudy.azureus2.ui.swt.ImageRepository;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.components.shell.ShellFactory;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.tools.HTTPDownload;

/**
 * @author Damokles
 *
 */
public class MotdDialog {

	//GUI stuff
	private Display display;
	private Shell shell;
	private Color black,white,light,grey,green,blue,fg,bg;
	private static final String lineSeparator = System.getProperty ("line.separator");

	private String motd = "No Data";

	private MotdDialog (String msg) {
		motd = msg;
		openGUI();

	}

	public static void open() {
		Thread t = new Thread () {
			public void run() {
				try {
					HTTPDownload dl = new HTTPDownload (new URL(RemoteConstants.MOTD_URL));
					dl.run();
					if (!dl.hasFailed()) {
						new MotdDialog(dl.getBuffer().toString("UTF-8"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
	}


	private void openGUI(){
		shell = ShellFactory.createShell(SWT.BORDER | SWT.TITLE | SWT.CLOSE | SWT.RESIZE);
		if(! Constants.isOSX) {
			shell.setImage(ImageRepository.getImage("azureus"));
		}

		shell.setText(I18N.translate("dialog.motd.shell.text"));

		display = shell.getDisplay();

		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		GridData data;

		StyledText helpPanel = new StyledText(shell, SWT.VERTICAL | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		helpPanel.setLayoutData(data);

		helpPanel.setEditable(false);
		try {
			String[] lines = motd.split("/n");



			helpPanel.setRedraw(false);
			helpPanel.setWordWrap(true);

			black = new Color((Device)display, 0,0,0);
			white = new Color((Device)display, 255,255,255);
			light = new Color((Device)display, 200,200,200);
			grey = new Color((Device)display, 50,50,50);
			green = new Color((Device)display, 30,80,30);
			blue = new Color((Device)display, 20,20,80);
			int style;
			boolean setStyle;

			helpPanel.setForeground(grey);

			for(String line:lines){

				setStyle = false;
				fg = grey;
				bg = white;
				style = SWT.NORMAL;

				char styleChar;
				String text;

				if (line.length() < 2) {
					styleChar = ' ';
					text = " " + lineSeparator;
				}
				else {
					styleChar = line.charAt(0);
					text = line.substring(1) + lineSeparator;
				}

				switch (styleChar) {
				case '*':
					text = "  * " + text;
					fg = green;
					setStyle = true;
					break;
				case '+':
					text = "     " + text;
					fg = black;
					bg = light;
					style = SWT.BOLD;
					setStyle = true;
					break;
				case '!':
					style = SWT.BOLD;
					setStyle = true;
					break;
				case '@':
					fg = blue;
					setStyle = true;
					break;
				case '$':
					bg = blue;
					fg = white;
					style = SWT.BOLD;
					setStyle = true;
					break;
				case ' ':
					text = "  " + text;
					break;
				}

				helpPanel.append(text);

				if (setStyle) {
					int lineCount = helpPanel.getLineCount()-1;
					int charCount = helpPanel.getCharCount();
					//          System.out.println("Got Linecount " + lineCount + ", Charcount " + charCount);

					int lineOfs = helpPanel.getOffsetAtLine(lineCount - 1);
					int lineLen = charCount-lineOfs;
					//          System.out.println("Setting Style : " + lineOfs + ", " + lineLen);
					helpPanel.setStyleRange(new StyleRange(lineOfs, lineLen, fg, bg, style));
					helpPanel.setLineBackground(lineCount-1, 1, bg);
				}
			}

		helpPanel.setRedraw(true);
	}
	catch (Exception e) {
		System.out.println("Unable to load help contents because:" + e);
		//e.printStackTrace();
	}

	Button bClose = new Button(shell,SWT.PUSH);
	bClose.setText(MessageText.getString("Button.close"));
	data = new GridData();
	data.widthHint = 70;
	data.horizontalAlignment = SWT.RIGHT;
	bClose.setLayoutData(data);

	Listener closeListener = new Listener() {
		public void handleEvent(Event event) {
			close();
		}
	};

	bClose.addListener(SWT.Selection, closeListener);
	shell.addListener(SWT.Close,closeListener);

	shell.setDefaultButton( bClose );

	shell.addListener(SWT.Traverse, new Listener() {
		public void handleEvent(Event e) {
			if ( e.character == SWT.ESC){
				close();
			}
		}
	});

	shell.setSize(500,400);
	Utils.centreWindow(shell);
	shell.layout();
	shell.open();
}

private void close() {
	if(black != null && !black.isDisposed())  black.dispose();
	if(white != null && !white.isDisposed())  white.dispose();
	if(light != null && !light.isDisposed())  light.dispose();
	if(grey  != null && !grey.isDisposed() )  grey.dispose();
	if(green != null && !green.isDisposed())  green.dispose();
	if(blue  != null && !blue.isDisposed() )  blue.dispose();
	shell.dispose();

}

}
