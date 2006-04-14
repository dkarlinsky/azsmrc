package lbms.azsmrc.remote.client.swtgui.container;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadStats;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.swtgui.CustomProgressBar;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public abstract class Container implements Comparable<Container> {
	protected TableItem item;
	protected Download dl;
	protected DownloadStats ds;
	protected int rowHeight = 16;
	protected int rowWidth = 120;
	protected int oldHealth;
	private int oldPercentage=-1;
	private float oldAvail = -1;
	private int oldPosition = -1;
	private String oldName = "";
	private long oldDownloadAverage = -1;
	private long oldUploadAverage = -1;
	private int oldSeeds = -1;
	private int oldLeechers = -1;
	private int oldConSeeds = -1;
	private int oldConLeechers = -1;
	private long oldUploaded = -1;
	private long oldDownloaded = -1;
	private String oldETA = "";
	private String oldStatus = "";
	private String oldTrackerStatus = "";
	private int oldDownloadRate = -5;
	private int oldUploadRate = -5;
	private long oldDiscarded = -1;
	private long oldSize = -1;
	private String oldElapsedTime = "";
	private long oldTotalAverage = -1;
	private float oldShareRatio = -1;
	protected static Image[] resizedHealthImages;
	protected CustomProgressBar progBar;
	protected Image progBarImage;

	private static List<Integer> totalTableColumns = new ArrayList<Integer>();

	static {
		totalTableColumns.add(RemoteConstants.ST_HEALTH);
		totalTableColumns.add(RemoteConstants.ST_POSITION);
		totalTableColumns.add(RemoteConstants.ST_NAME);
		totalTableColumns.add(RemoteConstants.ST_COMPLETITION);
		totalTableColumns.add(RemoteConstants.ST_AVAILABILITY);
		totalTableColumns.add(RemoteConstants.ST_DOWNLOAD_AVG);
		totalTableColumns.add(RemoteConstants.ST_UPLOAD_AVG);
		totalTableColumns.add(RemoteConstants.ST_ALL_SEEDS);
		totalTableColumns.add(RemoteConstants.ST_ALL_LEECHER);
		totalTableColumns.add(RemoteConstants.ST_UPLOADED);
		totalTableColumns.add(RemoteConstants.ST_DOWNLOADED);
		totalTableColumns.add(RemoteConstants.ST_ETA);
		totalTableColumns.add(RemoteConstants.ST_STATUS);
		totalTableColumns.add(RemoteConstants.ST_TRACKER);
		totalTableColumns.add(RemoteConstants.ST_DISCARDED);
		totalTableColumns.add(RemoteConstants.ST_SHARE);
		totalTableColumns.add(RemoteConstants.ST_SIZE);
		totalTableColumns.add(RemoteConstants.ST_ELAPSED_TIME);
		totalTableColumns.add(RemoteConstants.ST_TOTAL_AVG);
		totalTableColumns.add(RemoteConstants.ST_LIMIT_UP);
		totalTableColumns.add(RemoteConstants.ST_LIMIT_DOWN);
	}


	public Container (final Download dl, final Table parent, final int style) {
		this.dl = dl;
		this.ds = dl.getStats();
		final Display display = RCMain.getRCMain().getDisplay();

		if(Utilities.isLinux()){
			rowWidth = 120 - 4;
		}

		display.syncExec(new Runnable(){

			public void run() {
				try {
					item = new TableItem(parent,SWT.NULL);
					generateHealthImages(false);
					update(true);
				} catch (SWTException e) {
					e.printStackTrace();
				}
			}

		});
	}

	public Container (Download dl) {
		this.dl = dl;
		this.ds = dl.getStats();
		if(Utilities.isLinux()){
			rowWidth = 120 - 4;
		}
	}

	public void addToTable (final Table parent, final int style, int position) {
		if (item != null) return;
		 final Display display = RCMain.getRCMain().getDisplay();
		 display.syncExec(new Runnable(){
			 public void run() {
				 try {
					 item = new TableItem(parent,SWT.NULL);
					 generateHealthImages(false);
					 update(true);
				 } catch (SWTException e) {
					 e.printStackTrace();
				 }
			 }

		 });
	}

	public int compareTo(Container o) {
		if (dl.getPosition()==o.dl.getPosition()) {
			return dl.getName().compareToIgnoreCase(o.dl.getName());
		} else return dl.getPosition()-o.dl.getPosition();
	}

	public void update(final boolean bForce) {

		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.syncExec(new Runnable(){
			public void run() {
				try {
					item.setData(Container.this);
				} catch (SWTException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void updateData(final List<Integer> tableColumns, final boolean bForce) {
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) return;
		display.syncExec(new Runnable(){
			public void run() {
				try {
					DecimalFormat df = new DecimalFormat();
					df.applyPattern("##0.00");

					//ProgressBar
					if (tableColumns.contains(RemoteConstants.ST_COMPLETITION)) {
						int newPercentage = dl.getStats().getCompleted();
						if(progBar == null) progBar = new CustomProgressBar();
						if((progBarImage == null || (oldPercentage != -1 && oldPercentage != newPercentage)) || bForce){
							if (progBarImage != null) progBarImage.dispose();
							progBarImage = progBar.paintProgressBar(item,tableColumns.indexOf(RemoteConstants.ST_COMPLETITION),
									rowWidth,
									rowHeight,
									newPercentage,
									display,
									RCMain.getRCMain().isManifestInUse()?false:true);
							item.setImage(tableColumns.indexOf(RemoteConstants.ST_COMPLETITION),progBarImage);
						}

						oldPercentage = newPercentage;
					}

					//Availability
					if (tableColumns.contains(RemoteConstants.ST_AVAILABILITY)) {
						float avail = ds.getAvailability();
						if(oldAvail != avail || bForce){
							if(avail < 0)
								item.setText(tableColumns.indexOf(RemoteConstants.ST_AVAILABILITY), "N/A");
							else
								item.setText(tableColumns.indexOf(RemoteConstants.ST_AVAILABILITY), df.format(avail));
						}
						oldAvail = avail;
					}

					//Health
					if (tableColumns.contains(RemoteConstants.ST_HEALTH)) {
						int newHealth = ds.getHealth();
						if(oldHealth != newHealth || bForce){
							//Set the Health image
							if (newHealth==0){
								item.setImage(tableColumns.indexOf(RemoteConstants.ST_HEALTH),resizedHealthImages[5]);
							}

							else{
								item.setImage(tableColumns.indexOf(RemoteConstants.ST_HEALTH),resizedHealthImages[newHealth-1]);
							}

						}
					}



					//position
					if (tableColumns.contains(RemoteConstants.ST_POSITION)) {
						int position = dl.getPosition();
						if(oldPosition != position || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_POSITION), Integer.toString(position));
						oldPosition = position;
					}

					// name
					if (tableColumns.contains(RemoteConstants.ST_NAME)) {
						String name = dl.getName();
						if(!oldName.equalsIgnoreCase(name) || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_NAME), name);
						oldName = name;
					}

					//down speed
					if (tableColumns.contains(RemoteConstants.ST_DOWNLOAD_AVG)) {
						Long downloadAverage = ds.getDownloadAverage();
						if(oldDownloadAverage != downloadAverage || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOAD_AVG), DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downloadAverage));
						oldDownloadAverage = downloadAverage;
					}
					//up speed
					if (tableColumns.contains(RemoteConstants.ST_UPLOAD_AVG)) {
						Long uploadAverage = ds.getUploadAverage();
						if(oldUploadAverage != uploadAverage || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_UPLOAD_AVG), DisplayFormatters.formatByteCountToBase10KBEtcPerSec(uploadAverage));
						oldUploadAverage = uploadAverage;
					}
					//Seeds
					if (tableColumns.contains(RemoteConstants.ST_ALL_SEEDS)) {
						int seeds = dl.getTotalSeeds();
						int conSeeds = dl.getSeeds();
						if(oldSeeds != seeds || oldConSeeds != conSeeds || bForce){
							if(seeds<0)
								seeds = 0;
							item.setText(tableColumns.indexOf(RemoteConstants.ST_ALL_SEEDS), conSeeds + " (" + seeds+ ")");
						}
						oldSeeds = seeds;
						oldConSeeds = conSeeds;
					}
					//Leechers
					if (tableColumns.contains(RemoteConstants.ST_ALL_LEECHER)) {
						int leechers = dl.getTotalLeecher();
						int conLeechers = dl.getLeecher();
						if(oldLeechers != leechers || oldConLeechers != conLeechers || bForce){
							if(leechers < 0)
								leechers = 0;
							item.setText(tableColumns.indexOf(RemoteConstants.ST_ALL_LEECHER), conLeechers + " (" + leechers + ")");
						}
						oldLeechers = leechers;
						oldConLeechers = conLeechers;
					}
					//Amount Uploaded
					if (tableColumns.contains(RemoteConstants.ST_UPLOADED)) {
						Long uploaded = ds.getUploaded();
						if(oldUploaded != uploaded || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_UPLOADED),DisplayFormatters.formatByteCountToBase10KBEtc(uploaded));
						oldUploaded = uploaded;
					}

					//Amount Downloaded
					if (tableColumns.contains(RemoteConstants.ST_DOWNLOADED)) {
						Long downloaded = ds.getDownloaded();
						if(oldDownloaded != downloaded || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOADED),DisplayFormatters.formatByteCountToBase10KBEtc(downloaded));
						oldDownloaded = downloaded;
					}

					//ETA
					if (tableColumns.contains(RemoteConstants.ST_ETA)) {
						String eta = ds.getETA();
						if(!oldETA.equalsIgnoreCase(eta) || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_ETA),eta);
						oldETA = eta;
					}


					//Status
					if (tableColumns.contains(RemoteConstants.ST_STATUS)) {
						String status = ds.getStatus();
						if(!oldStatus.equalsIgnoreCase(status) || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_STATUS),status);
						oldStatus = status;
					}

					//Tracker Status
					if (tableColumns.contains(RemoteConstants.ST_TRACKER)) {
						String trackerStatus = ds.getTrackerStatus();
						if(!oldTrackerStatus.equalsIgnoreCase(trackerStatus) || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_TRACKER),trackerStatus);
						oldTrackerStatus = trackerStatus;
					}

					//Download Limit
					if (tableColumns.contains(RemoteConstants.ST_LIMIT_DOWN)) {
						int rate = dl.getMaximumDownloadKBPerSecond();
						if(oldDownloadRate != rate || bForce){
							if(rate <= 0)
								item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_DOWN),"Maximum");
							else
								item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_DOWN),DisplayFormatters.formatByteCountToBase10KBEtc(rate));
						}
						oldDownloadRate = rate;
					}


					//Upload Limit
					if (tableColumns.contains(RemoteConstants.ST_LIMIT_UP)) {
						int rate = dl.getUploadRateLimitBytesPerSecond();
						if(oldUploadRate != rate || bForce){
							if(rate <= 0)
								item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_UP),"Maximum");
							else
								item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_UP),DisplayFormatters.formatByteCountToBase10KBEtc(rate));
						}
						oldUploadRate = rate;
					}

					//Discarded
					if (tableColumns.contains(RemoteConstants.ST_DISCARDED)) {
						Long discarded = dl.getDiscarded();
						if(oldDiscarded != discarded || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_DISCARDED),DisplayFormatters.formatByteCountToBase10KBEtc(discarded));
						oldDiscarded = discarded;
					}

					//Size
					if (tableColumns.contains(RemoteConstants.ST_SIZE)) {
						Long size = dl.getSize();
						if(oldSize != size || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_SIZE),DisplayFormatters.formatByteCountToBase10KBEtc(size));
						oldSize = size;
					}

					//Elapsed Time
					if (tableColumns.contains(RemoteConstants.ST_ELAPSED_TIME)) {
						String elapsedTime = ds.getElapsedTime();
						if(!oldElapsedTime.equalsIgnoreCase(elapsedTime) || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_ELAPSED_TIME),elapsedTime);
						oldElapsedTime = elapsedTime;
					}

					//Swarm Speed
					if (tableColumns.contains(RemoteConstants.ST_TOTAL_AVG)) {
						Long totalAverage = ds.getTotalAverage();
						if(oldTotalAverage != totalAverage || bForce)
							item.setText(tableColumns.indexOf(RemoteConstants.ST_TOTAL_AVG),DisplayFormatters.formatByteCountToBase10KBEtc(totalAverage)+"/s");
						oldTotalAverage = totalAverage;
					}


					//Share Ratio
					if (tableColumns.contains(RemoteConstants.ST_SHARE)) {
						float ratio = (ds.getShareRatio()/1000f);
						if(oldShareRatio != ratio || bForce){
							int shareIndex = tableColumns.indexOf(RemoteConstants.ST_SHARE);
							item.setText(shareIndex,df.format(ratio));
							if(ratio < 0.5)
								item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
							else if(ratio > 0.05 && ratio < 0.9)
								item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
							else
								item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
						}
						oldShareRatio = ratio;
					}


					//item.getParent().redraw();
					//item.getParent().getParent().layout();
				} catch (SWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void changePosition (final int newPos, final Table parent) {
		Display display = RCMain.getRCMain().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				try {
					if(item != null && !item.isDisposed())
						item.getParent().remove(item.getParent().indexOf(item));
					item = new TableItem(parent,SWT.NULL,newPos);
					item.setData(Container.this);
					update(true);
				} catch (SWTException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void removeFromTable(){
		Display display = RCMain.getRCMain().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				try {
					if(item != null && !item.isDisposed())
						item.getParent().remove(item.getParent().indexOf(item));
					dispose();
				} catch (SWTException e) {
					e.printStackTrace();
				}
			}
		});
	}



	public static List<Integer> getTotalColumns() {
		return totalTableColumns;
	}

	/**
	 * @return Returns the dl.
	 */
	public Download getDownload() {
		return dl;
	}

	public void dispose() {
		if (progBarImage != null) progBarImage.dispose();
	}

	public static List<Integer> getColumns() {
		return null;
	}

	/* (non-Javadoc)
	 * This is just a Fallback don't count on it use dispose();
	 */
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}


	private Image resizeHealthIcon(Image oldHealthIcon,int iconColWidth, int newPBWidth){
		if (RCMain.getRCMain().isManifestInUse()) return oldHealthIcon;
		Image newImage = new Image(RCMain.getRCMain().getDisplay(),newPBWidth,rowHeight);
		GC gc = new GC(newImage);
		int x = (iconColWidth /2) - (oldHealthIcon.getImageData().width / 2);
		gc.drawImage(oldHealthIcon, x, 0);
		gc.dispose();

		ImageData newData = newImage.getImageData();
		int whitePixel = newData.palette.getPixel(new RGB(255,255,255));
		newData.transparentPixel = whitePixel;

		return new Image(RCMain.getRCMain().getDisplay(),newData);
	}


	public void generateHealthImages(boolean bforced) {
		if (resizedHealthImages == null || bforced) {
			if (resizedHealthImages == null) {
				resizedHealthImages = new Image[6];
			}
			for (int i = 0; i<resizedHealthImages.length; i++) {
				if (resizedHealthImages[i] != null) resizedHealthImages[i].dispose();
				switch(i+1) {
				case DownloadStats.HEALTH_OK:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_green")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);

					break;
				case DownloadStats.HEALTH_ERROR:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_red")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);

					break;
				case DownloadStats.HEALTH_NO_TRACKER:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_blue")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);

					break;
				case DownloadStats.HEALTH_NO_REMOTE:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_yellow")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);

					break;
				case DownloadStats.HEALTH_KO:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_red")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);

					break;
				default:
					resizedHealthImages[i] = resizeHealthIcon(ImageRepository.getImage("health_gray")
							,18 /* static health column widht */
							,120 /*static pb column width*/ -4);
				}
			}
		}
	}

}//EOF
