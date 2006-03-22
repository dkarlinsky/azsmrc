/*
 * Created on Jan 28, 2006
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.util.TimerEvent;
import lbms.azsmrc.remote.client.util.TimerEventPerformer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class MessageDialog {

	private int thread_Sleep_Steps = 35;
	private Shell splash;
	private Display display;
	private int steps;
	private Image popupImage;
	private static List<MessageDialog> messageDiags = Collections.synchronizedList(new ArrayList<MessageDialog>());
	TimerEvent timerEvent;

	/**
	 *
	 * @param display
	 * @param bautoclose -- whether it autocloses after timeToClose or is permenant
	 * @param timeToClose -- time in milliseconds until autoclose (if true)
	 * @param steps -- int of the amount of steps to open with in the animation
	 * @param title -- title for error box
	 * @param message -- details of the message
	 */

	public MessageDialog(final Display display, final boolean bautoclose, final int timeToClose, final int steps, final String title, final String message){

		if(!Boolean.parseBoolean(RCMain.getRCMain().getProperties().getProperty("popups_enabled", "true")))
			return;

		this.display = display;
		this.steps = steps;
		messageDiags.add(this);
		display.asyncExec(new Runnable (){

			public void run() {

				popupImage = new Image(display, ImageRepository.getImage("popup"), SWT.IMAGE_COPY);
				splash = new Shell(SWT.ON_TOP);

				FormLayout formLayout = new FormLayout();
				formLayout.marginHeight = 0;
				formLayout.marginWidth = 0;
				formLayout.spacing = 0;

				splash.setLayout(formLayout);

				GC gc = new GC(popupImage);

				Font initialFont = gc.getFont();
				FontData[] fontData = initialFont.getFontData();
				for (int i = 0; i < fontData.length; i++) {
					fontData[i].setStyle(SWT.NORMAL);
					fontData[i].setHeight(fontData[i].getHeight() + 2);
				}
				Font messageFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
				for (int i = 0; i < fontData.length; i++) {
					fontData[i].setStyle(SWT.BOLD);
					fontData[i].setHeight(fontData[i].getHeight() + 6);
				}
				Font titleFont = new Font(RCMain.getRCMain().getDisplay(), fontData);
				gc.setFont(titleFont);
				gc.drawText(title, 10, 8, true);
				titleFont.dispose();
				gc.setFont(messageFont);
				Rectangle rect = new Rectangle(60,45,150,150);
				printString(gc,message,rect);

				messageFont.dispose();


				gc.dispose();

				Button btnOK = new Button(splash, SWT.PUSH);
				btnOK.setText("OK");

				Button btnHideAll = new Button(splash, SWT.PUSH);
				btnHideAll.setText("Hide All");

				Label lblImage = new Label(splash,SWT.NULL);
				lblImage.setImage(popupImage);

				FormData formData;
				formData = new FormData();
				formData.right = new FormAttachment(100,-60);
				formData.bottom = new FormAttachment(100,-5);
				btnOK.setLayoutData(formData);

				formData = new FormData();
				formData.right = new FormAttachment(100,-5);
				formData.bottom = new FormAttachment(100,-5);
				btnHideAll.setLayoutData(formData);

				formData = new FormData();
				formData.left = new FormAttachment(0,0);
				formData.top = new FormAttachment(0,0);
				lblImage.setLayoutData(formData);

				splash.pack();
				splash.layout();


				timerEvent = RCMain.getRCMain().getMainTimer().addEvent(System.currentTimeMillis()+timeToClose, new TimerEventPerformer() {
					public void perform(TimerEvent event) {
						hideShell();
					}
				});

				btnOK.addListener(SWT.Selection, new Listener(){
					public void handleEvent(Event arg0) {
						hideShell();
					}
				});
				btnHideAll.addListener(SWT.Selection, new Listener(){
					public void handleEvent(Event arg0) {
						MessageDialog[] list = messageDiags.toArray(new MessageDialog[] {});
						for (MessageDialog md:list) {
							md.killShell();
						}
					}
				});
				showShell();
			}
		});
	}


	public void showShell() {
		int step = 0;
		Rectangle splashRect = splash.getBounds();
		Rectangle displayRect = display.getClientArea();
		final int start_x = displayRect.width - splashRect.width - 5;
		final int start_y = displayRect.height;
		final int end_x = (displayRect.width - splashRect.width - 5);
		final int end_y = (displayRect.height - splashRect.height);
		while(step <= steps) {
			try {
				final int x = start_x + ((end_x - start_x) * step ) / steps;
				final int y = start_y + ((end_y - start_y) * step ) / steps;
				display.asyncExec(new Runnable() {
				  public void run() {
					if(splash == null || splash.getShell().isDisposed())
					  return;

					splash.setLocation(x,y);
					if(!splash.isVisible()) splash.open();
					try {
						Thread.sleep(thread_Sleep_Steps);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				  }
				});
			  step++;
			} catch(Exception e) {
			  //Stop animating
			  step = steps;
			}
		}

	}

	public void hideShell() {
		if (timerEvent != null) timerEvent.cancel();
		messageDiags.remove(this);
		display.asyncExec(new Runnable() {
			public void run() {
				Rectangle splashRect = splash.getBounds();
				Rectangle displayRect = display.getClientArea();
				final int start_x = displayRect.width - splashRect.width - 5;
				final int start_y = displayRect.height;
				final int end_x = (displayRect.width - splashRect.width - 5);
				final int end_y = (displayRect.height - splashRect.height);
				int step = 0;
				while(step <= steps) {
					try {
						final int x = end_x + ((start_x - end_x) * step ) / steps;
						final int y = end_y + ((start_y - end_y) * step ) / steps;
						splash.setLocation(x,y);
						if(!splash.isVisible()) splash.open();
						try {
							Thread.sleep(thread_Sleep_Steps);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						step++;
					} catch(Exception e) {
						//Stop animating
						step = steps;
					}
				}
				splash.close();
				popupImage.dispose();
			}
		});
	}

	public void killShell() {
		if (timerEvent != null) timerEvent.cancel();
		messageDiags.remove(this);
		display.asyncExec(new Runnable() {
			public void run() {
				splash.close();
				popupImage.dispose();
			}
		});
	}

	  public static boolean printString(GC gc,String string,Rectangle printArea) {
		  int x0 = printArea.x;
		  int y0 = printArea.y;
		  int height = 0;
		  Rectangle oldClipping = gc.getClipping();

		  //Protect the GC from drawing outside the drawing area
		  gc.setClipping(printArea);

		  //We need to add some cariage return ...
		  String sTabsReplaced = string.replaceAll("\t", "  ");

		  StringBuffer outputLine = new StringBuffer();

		  // Process string line by line
		  StringTokenizer stLine = new StringTokenizer(sTabsReplaced,"\n");
		  while(stLine.hasMoreElements()) {
			int iLineHeight = 0;
			String sLine = stLine.nextToken();
			if (gc.stringExtent(sLine).x > printArea.width) {
			  //System.out.println("Line: "+ sLine);
			  StringTokenizer stWord = new StringTokenizer(sLine, " ");
			  String space = "";
			  int iLineLength = 0;
			  iLineHeight = gc.stringExtent(" ").y;

			  // Process line word by word
			  while(stWord.hasMoreElements()) {
				String word = stWord.nextToken();

				// check if word is longer than our print area, and split it
				Point ptWordSize = gc.stringExtent(word + " ");
				while (ptWordSize.x > printArea.width) {
					int endIndex = word.length() - 1;
					do {
						endIndex--;
						ptWordSize = gc.stringExtent(word.substring(0, endIndex) + " ");
					} while (endIndex > 3 && ptWordSize.x + iLineLength > printArea.width);
					// append part that will fit
					outputLine.append(space)
							  .append(word.substring(0, endIndex))
							  .append("\n");
				  height += ptWordSize.y;

				  // setup word as the remaining part that didn't fit
					word = word.substring(endIndex);
					ptWordSize = gc.stringExtent(word + " ");
					iLineLength = 0;
				}
				iLineLength += ptWordSize.x;
				//System.out.println(outputLine + " : " + word + " : " + iLineLength);
				if(iLineLength > printArea.width) {
				  iLineLength = ptWordSize.x;
				  height += iLineHeight;
				  iLineHeight = ptWordSize.y;
				  space = "\n";
				}
				if (iLineHeight < ptWordSize.y)
				  iLineHeight = ptWordSize.y;

				outputLine.append(space).append(word);
				space = " ";
			  }
			} else {
			  outputLine.append(sLine);
			  iLineHeight = gc.stringExtent(sLine).y;
			}
			outputLine.append("\n");
			height += iLineHeight;
		  }

		  String sOutputLine = outputLine.toString();
		  gc.drawText(sOutputLine,x0,y0,true);
		  gc.setClipping(oldClipping);
		  return height <= printArea.height;
		}

}//EOF
