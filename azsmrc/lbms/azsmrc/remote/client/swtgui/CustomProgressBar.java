/*
 * Created on Aug 4, 2005
 * Created by omschaub
 *
 */
package lbms.azsmrc.remote.client.swtgui;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

public class CustomProgressBar {
	private NumberFormat longPercentFormat;
	private RGB backgroundRGB, backgroundShadowRGB, backgroundHighlightRGB, progressRGB, progressShadowRGB, progressHighlightRGB, textRGB, borderRGB;
	private Display display;
	private Color backgoundColor, backgroundShadowColor, backgroundHighlightColor, progressColor, progressShadowColor, progressHighlightColor, textColor, borderColor;

	public Image paintProgressBar(TableItem cell, int column_of_cell, int width, int height, Integer completed, Display sentDisplay, boolean isRelief) {
		display = sentDisplay;

		this.longPercentFormat = new DecimalFormat("##0.0");
		initDefaultRGBs();
		Image image = this.getImage(cell,column_of_cell, width, height);
		GC gc = new GC(image);

		this.paintBackground(width, height, gc, isRelief);
		this.paintProgress(width, height, completed, gc, isRelief);
		this.paintPercent(width, height, completed, gc);
		this.paintBorder(width, height, gc);

		gc.dispose();
		return image;

	}

	private void initDefaultRGBs() {
		//this.defaultBackgroundRGB = new RGB(214, 235, 255); //210, 16, 100 (hsv)
		//this.defaultProgressRGB = new RGB(128, 191, 255);//210, 50, 500 (hsv)
		//this.defaultTextRGB = new RGB(0, 64, 128); //210, 100, 50 (hsv)
		//this.defaultBorderRGB = this.defaultTextRGB;

		//new colors based on system colors to accomodate manifest
		this.backgroundRGB = new RGB(214, 235, 255);
		this.backgroundShadowRGB = new RGB(0, 255, 0);
		this.backgroundHighlightRGB = new RGB(255, 0, 0);
		this.progressRGB = new RGB(128, 191, 255);
		this.progressShadowRGB = new RGB(0, 0, 0);
		this.progressHighlightRGB = new RGB(0, 0, 255);

		this.textRGB = display.getSystemColor(SWT.COLOR_DARK_BLUE).getRGB();
		this.borderRGB = this.textRGB;
	}

	private RGB getBackgroundRGB() {

		return this.backgroundRGB;
	}

	private RGB getBackgroundShadowRGB() {

		return this.backgroundShadowRGB;
	}

	private RGB getBackgroundHighlightRGB() {

		return this.backgroundHighlightRGB;
	}


	private RGB getProgressRGB() {

		return this.progressRGB;
	}

	private RGB getProgressShadowRGB() {

		return this.progressShadowRGB;
	}

	private RGB getProgressHighlightRGB() {

		return this.progressHighlightRGB;
	}

	private RGB getTextRGB() {

		return this.textRGB;
	}

	private RGB getBorderRGB() {

		return this.borderRGB;
	}
	public Color getBackgroundColor() {
		if (this.backgoundColor == null) {
			this.backgoundColor = new Color(display, this.getBackgroundRGB());
		}
		return this.backgoundColor;
	}

	public Color getBackgroundShadowColor() {
		if (this.backgroundShadowColor == null) {
			this.backgroundShadowColor = new Color(display, this.getBackgroundShadowRGB());
		}
		return this.backgroundShadowColor;
	}

	public Color getBackgroundHighlightColor() {
		if (this.backgroundHighlightColor == null) {
			this.backgroundHighlightColor = new Color(display, this.getBackgroundHighlightRGB());
		}
		return this.backgroundHighlightColor;
	}

	public Color getProgressColor() {
		if (this.progressColor == null) {
			this.progressColor = new Color(display, this.getProgressRGB());
		}
		return this.progressColor;
	}

	public Color getProgressHighlightColor() {
		if (this.progressHighlightColor == null) {
			this.progressHighlightColor = new Color(display, this.getProgressHighlightRGB());
		}
		return this.progressHighlightColor;
	}

	public Color getProgressShadowColor() {
		if (this.progressShadowColor == null) {
			this.progressShadowColor = new Color(display, this.getProgressShadowRGB());
		}
		return this.progressShadowColor;
	}

	public Color getBorderColor() {
		if (this.borderColor == null) {
			this.borderColor = new Color(display, this.getBorderRGB());
		}
		return this.borderColor;
	}

	public Color getTextColor() {
		if (this.textColor == null) {
			this.textColor = new Color(display, this.getTextRGB());
		}
		return this.textColor;
	}
	private void paintBackground(int imageWidth, int imageHeight, GC gc, boolean isRelief) {
		int heightToPaint = imageHeight - 2;
		int widthToPaint = imageWidth - 2;
		if (!isRelief) {
			gc.setBackground(getBackgroundColor());
			gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
		} else {
			gc.setBackground(getBackgroundHighlightColor());
			gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
			gc.setBackground(getBackgroundShadowColor());
			gc.fillRectangle(2, 2, widthToPaint-1, heightToPaint-1);
			gc.setBackground(getBackgroundColor());
			gc.fillRectangle(2, 2, widthToPaint-2, heightToPaint-2);
		}
	   /* else {
			Image background = this.getColoredBackground();
			int srcHeight = background.getImageData().height;
			gc.drawImage(background, 0, 0, 1, srcHeight, 1, 1, widthToPaint, heightToPaint);
		}*/
	}

	private void paintProgress(int imageWidth, int imageHeight, Integer completed, GC gc, boolean isRelief) {
		if (!isRelief) {
			this.paintSolidProgress(imageWidth, imageHeight, completed, gc);
		} else {
			this.paintReliefProgress(imageWidth, imageHeight, completed, gc);
		}
	}

	private void paintSolidProgress(int imageWidth, int imageHeight, Integer completed, GC gc) {
		int widthToPaint = getWidthToPaint(completed, imageWidth);
		gc.setBackground(new Color(display, progressRGB));
		gc.fillRectangle(1, 1, widthToPaint, imageHeight - 2);
	}

	private void paintReliefProgress(int imageWidth, int imageHeight, Integer completed, GC gc) {
		int widthToPaint = getWidthToPaint(completed, imageWidth);
		int heightToPaint = imageHeight - 2;
		gc.setBackground(getProgressHighlightColor());
		gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
		gc.setBackground(getProgressShadowColor());
		gc.fillRectangle(2, 2, widthToPaint-1, heightToPaint-1);
		gc.setBackground(getProgressColor());
		gc.fillRectangle(2, 2, widthToPaint-2, heightToPaint-2);
	}

	private void paintPercent(int imageWidth, int imageHeight, Integer completed, GC gc) {
		gc.setForeground(new Color(display, textRGB));
		String percent = this.longPercentFormat.format(completed.intValue() / 10.0f) + " %";
		Point extent = gc.stringExtent(percent);
		if (extent.x <= imageWidth) {
			this.paintString(imageWidth, imageHeight, percent, extent, gc, true);
		} else {
			percent = completed.toString() + " %";
			extent = gc.stringExtent(percent);
			if (extent.x <= imageWidth) {
				this.paintString(imageWidth, imageHeight, percent, extent, gc, true);
			} else {
				percent = completed.toString();
				extent = gc.stringExtent(percent);
				this.paintString(imageWidth, imageHeight, percent, extent, gc, false);
			}
		}
	}

	private void paintString(int imageWidth, int imageHeight, String percent, Point extent, GC gc, boolean isPaintPercent) {
		if (isPaintPercent) {
			int x = (imageWidth - extent.x + 1) / 2;
			int y = (imageHeight - extent.y + 1) / 2;
			gc.drawString(percent, x, y, true);
		}
	}

	private int getWidthToPaint(Integer completed, int imageWidth) {
		float precentComplete = completed.intValue() / 1000.0f;
		int widthToPaint = (int) ((imageWidth - 2) * precentComplete);
		return widthToPaint;
	}

	private void paintBorder(int imageWidth, int imageHeight, GC gc) {
		gc.setForeground(new Color(display, borderRGB));
		gc.drawRectangle(0, 0, imageWidth - 1, imageHeight - 1);
	}

	private Image getImage(TableItem cell, int column_of_cell, int width, int height) {
			Image oldImage = cell.getImage(column_of_cell);
			if (oldImage != null && !oldImage.isDisposed()) {
				Rectangle oldBounds =  oldImage.getBounds();
				if (oldBounds.width != width || oldBounds.height != height) {
					oldImage.dispose();
					return this.createImage(width, height);
				}
				return oldImage;
			}

		return this.createImage(width, height);
	}

	private Image createImage(int width, int height) {
		return new Image(display, width, height);
	}


}//EOF
