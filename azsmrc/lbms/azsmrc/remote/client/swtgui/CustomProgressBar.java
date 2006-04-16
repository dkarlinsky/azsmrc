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
	private static RGB backgroundRGB, backgroundShadowRGB, backgroundHighlightRGB, progressRGB, progressShadowRGB, progressHighlightRGB, textRGB, borderRGB;
	private Display display;
	private static Color backgoundColor, backgroundShadowColor, backgroundHighlightColor, progressColor, progressShadowColor, progressHighlightColor, textColor, borderColor;

	public Image paintProgressBar(TableItem cell, int column_of_cell, int width, int height, Integer completed, Display sentDisplay, boolean isRelief) {
		display = sentDisplay;

		this.longPercentFormat = new DecimalFormat("##0.0");
		initDefaultRGBs();
		Image image = this.getImage(cell,column_of_cell, width, height);
		GC gc = new GC(image);

		this.paintBackground(width, height, gc, isRelief);
		this.paintProgress(width, height, completed, gc, isRelief);
		this.paintPercent(width, height, completed, gc);
		if (!isRelief)this.paintBorder(width, height, gc);

		gc.dispose();
		return image;

	}

	private void initDefaultRGBs() {
		//this.defaultBackgroundRGB = new RGB(214, 235, 255); //210, 16, 100 (hsv)
		//this.defaultProgressRGB = new RGB(128, 191, 255);//210, 50, 500 (hsv)
		//this.defaultTextRGB = new RGB(0, 64, 128); //210, 100, 50 (hsv)
		//this.defaultBorderRGB = this.defaultTextRGB;

		//new colors based on system colors to accomodate manifest
		backgroundRGB = new RGB(214, 235, 255);
		backgroundShadowRGB = new RGB(150, 178, 205);
		backgroundHighlightRGB = new RGB(241, 248, 255);
		progressRGB = new RGB(128, 191, 255);
		progressShadowRGB = new RGB(83, 134, 186);
		progressHighlightRGB = new RGB(185, 218, 253);

		textRGB = display.getSystemColor(SWT.COLOR_DARK_BLUE).getRGB();
		borderRGB = textRGB;
	}

	private RGB getBackgroundRGB() {

		return backgroundRGB;
	}

	private RGB getBackgroundShadowRGB() {

		return backgroundShadowRGB;
	}

	private RGB getBackgroundHighlightRGB() {

		return backgroundHighlightRGB;
	}


	private RGB getProgressRGB() {

		return progressRGB;
	}

	private RGB getProgressShadowRGB() {

		return progressShadowRGB;
	}

	private RGB getProgressHighlightRGB() {

		return progressHighlightRGB;
	}

	private RGB getTextRGB() {

		return textRGB;
	}

	private RGB getBorderRGB() {

		return borderRGB;
	}
	public Color getBackgroundColor() {
		if (backgoundColor == null) {
			backgoundColor = new Color(display, this.getBackgroundRGB());
		}
		return backgoundColor;
	}

	public Color getBackgroundShadowColor() {
		if (backgroundShadowColor == null) {
			backgroundShadowColor = new Color(display, this.getBackgroundShadowRGB());
		}
		return backgroundShadowColor;
	}

	public Color getBackgroundHighlightColor() {
		if (backgroundHighlightColor == null) {
			backgroundHighlightColor = new Color(display, this.getBackgroundHighlightRGB());
		}
		return backgroundHighlightColor;
	}

	public Color getProgressColor() {
		if (progressColor == null) {
			progressColor = new Color(display, this.getProgressRGB());
		}
		return progressColor;
	}

	public Color getProgressHighlightColor() {
		if (progressHighlightColor == null) {
			progressHighlightColor = new Color(display, this.getProgressHighlightRGB());
		}
		return progressHighlightColor;
	}

	public Color getProgressShadowColor() {
		if (progressShadowColor == null) {
			progressShadowColor = new Color(display, this.getProgressShadowRGB());
		}
		return progressShadowColor;
	}

	public Color getBorderColor() {
		if (borderColor == null) {
			borderColor = new Color(display, this.getBorderRGB());
		}
		return borderColor;
	}

	public Color getTextColor() {
		if (textColor == null) {
			textColor = new Color(display, this.getTextRGB());
		}
		return textColor;
	}
	private void paintBackground(int imageWidth, int imageHeight, GC gc, boolean isRelief) {
		int heightToPaint = imageHeight - ((isRelief)?1:2);
		int widthToPaint = imageWidth - ((isRelief)?1:2);
		if (!isRelief) {
			gc.setBackground(getBackgroundColor());
			gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
		} else {
			gc.setBackground(getBackgroundShadowColor());
			gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
			gc.setBackground(getBackgroundHighlightColor());
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
		int heightToPaint = imageHeight - 1;
		gc.setBackground(getProgressHighlightColor());
		gc.fillRectangle(1, 1, widthToPaint, heightToPaint);
		gc.setBackground(getProgressShadowColor());
		gc.fillRectangle(3, 3, widthToPaint-2, heightToPaint-2);
		gc.setForeground(getProgressShadowColor());
		gc.drawPoint(2, heightToPaint);
		gc.drawPoint(widthToPaint, 2);
		gc.setBackground(getProgressColor());
		gc.fillRectangle(3, 3, widthToPaint-4, heightToPaint-4);
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
