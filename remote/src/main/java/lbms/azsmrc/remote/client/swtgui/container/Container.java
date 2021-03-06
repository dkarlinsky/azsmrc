package lbms.azsmrc.remote.client.swtgui.container;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.DownloadStats;
import lbms.azsmrc.remote.client.Utilities;
import lbms.azsmrc.remote.client.swtgui.CustomProgressBar;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.remote.client.swtgui.ImageRepository;
import lbms.azsmrc.remote.client.util.DisplayFormatters;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.SWTSafeRunnable;

import org.eclipse.swt.SWT;

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
	private String oldCategory = "";
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
	private static Map<Integer,Comparator<Container>> comparatorCollection = new HashMap<Integer, Comparator<Container>>();

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
		totalTableColumns.add(RemoteConstants.ST_CATEGORY);

		comparatorCollection.put(RemoteConstants.ST_POSITION, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return o1.compareTo(o2);
			}
		});
		comparatorCollection.put(RemoteConstants.ST_NAME, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return o1.dl.getName().compareToIgnoreCase(o2.dl.getName());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_COMPLETITION, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return o1.dl.getStats().getCompleted()-o2.dl.getStats().getCompleted();
			}
		});
		comparatorCollection.put(RemoteConstants.ST_AVAILABILITY, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return (int)(o1.dl.getStats().getAvailability()-o2.dl.getStats().getAvailability());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_DOWNLOAD_AVG, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getStats().getDownloadAverage(),o2.dl.getStats().getDownloadAverage());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_UPLOAD_AVG, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getStats().getUploadAverage(),o2.dl.getStats().getUploadAverage());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_UPLOADED, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getStats().getUploaded(),o2.dl.getStats().getUploaded());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_DOWNLOADED, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getStats().getDownloaded(),o2.dl.getStats().getDownloaded());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_ALL_LEECHER, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				int value = o1.dl.getLeecher()-o2.dl.getLeecher();
				if(value == 0) {
					return (o1.dl.getTotalLeecher() - o2.dl.getTotalLeecher());
				} else {
					return (value);
				}
			}
		});
		comparatorCollection.put(RemoteConstants.ST_ALL_SEEDS, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				int value = o1.dl.getSeeds() - o2.dl.getSeeds();
				if( value == 0) {
					return (o1.dl.getTotalSeeds() - o2.dl.getTotalSeeds());
				} else {
					return (value);
				}
			}
		});
		comparatorCollection.put(RemoteConstants.ST_DISCARDED, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getDiscarded(),o2.dl.getDiscarded());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_SHARE, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return (o1.dl.getStats().getShareRatio()-o2.dl.getStats().getShareRatio());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_SIZE, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				return compareLong(o1.dl.getSize(),o2.dl.getSize());
			}
		});
		comparatorCollection.put(RemoteConstants.ST_CATEGORY, new Comparator<Container>() {
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Container o1, Container o2) {
				String cat1 = o1.getDownload().getTorrentAttribute(RemoteConstants.TA_CATEGORY);
				String cat2 = o2.getDownload().getTorrentAttribute(RemoteConstants.TA_CATEGORY);
				if (cat1 == null) {
					cat1 = "";
				}
				if (cat2 == null) {
					cat2 = "";
				}
				return cat1.compareToIgnoreCase(cat2);
			}
		});
	}


	public Container (final Download dl, final Table parent, final int style) {
		this.dl = dl;
		this.ds = dl.getStats();
		final Display display = RCMain.getRCMain().getDisplay();

		if(Utilities.isLinux()){
			rowWidth = 120 - 4;
		}

		display.syncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				generateHealthImages(false);
			}
		});
	}

	private static int compareLong (long v1, long v2) {
		long t = v1-v2;
		if (t>0) {
			return 1;
		}
		if (t==0) {
			return 0;
		}
		return -1;
	}

	public Container (Download dl) {
		this.dl = dl;
		this.ds = dl.getStats();
		if(Utilities.isLinux()){
			rowWidth = 120 - 4;
		}
	}
	public void setTableItem (TableItem item){
		this.item = item;
		this.item.setData(Container.this);
		generateHealthImages(false);
		//update(true);
	}

	public TableItem getTableItem(){
		return item;
	}

	public void deleteTableItem(){
		if(item != null){
			item.dispose();
			item = null;
		}
	}

	public int compareTo(Container o) {
		if (dl.getPosition()==o.dl.getPosition()) {
			return dl.getName().compareToIgnoreCase(o.dl.getName());
		} else {
			return dl.getPosition()-o.dl.getPosition();
		}
	}

	public void updateData(final List<Integer> tableColumns, final boolean bForce) {
		if(item == null) {
			return;
		}
		final Display display = RCMain.getRCMain().getDisplay();
		if(display == null || display.isDisposed()) {
			return;
		}
		display.syncExec(new SWTSafeRunnable(){
			@Override
			public void runSafe() {
				DecimalFormat df = new DecimalFormat();
				df.applyPattern("##0.000");

				//ProgressBar
				if (tableColumns.contains(RemoteConstants.ST_COMPLETITION)) {
					int newPercentage = dl.getStats().getCompleted();
					if(progBar == null) {
						progBar = new CustomProgressBar();
					}
					if((progBarImage == null
							|| (oldPercentage != -1 && oldPercentage != newPercentage))
							|| item.getImage(tableColumns.indexOf(RemoteConstants.ST_COMPLETITION)) == null
							|| bForce){
						if (progBarImage != null) {
							progBarImage.dispose();
						}
						progBarImage = progBar.paintProgressBar(item,tableColumns.indexOf(RemoteConstants.ST_COMPLETITION),
								rowWidth,
								rowHeight,
								newPercentage,
								display,
								//RCMain.getRCMain().isManifestInUse()?false:
								true);
						item.setImage(tableColumns.indexOf(RemoteConstants.ST_COMPLETITION),progBarImage);
					}
					oldPercentage = newPercentage;
				}

				//Availability
				if (tableColumns.contains(RemoteConstants.ST_AVAILABILITY)) {
					float avail = ds.getAvailability();
					if(oldAvail != avail
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_AVAILABILITY)).equals(df.format(avail))
							|| bForce){
						if(avail < 0) {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_AVAILABILITY), "N/A");
						} else {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_AVAILABILITY), df.format(avail));
						}
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
					if(oldPosition != position
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_POSITION)).equals(position)
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_POSITION), Integer.toString(position));
					}
					oldPosition = position;
				}

				// name
				if (tableColumns.contains(RemoteConstants.ST_NAME)) {
					String name = dl.getName();
					if(!oldName.equalsIgnoreCase(name)
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_NAME)).equals(name)
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_NAME), name);
					}
					oldName = name;
				}

				//down speed
				if (tableColumns.contains(RemoteConstants.ST_DOWNLOAD_AVG)) {
					Long downloadAverage = ds.getDownloadAverage();
					if(oldDownloadAverage != downloadAverage
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOAD_AVG)).equals(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downloadAverage))
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOAD_AVG), DisplayFormatters.formatByteCountToBase10KBEtcPerSec(downloadAverage));
					}
					oldDownloadAverage = downloadAverage;
				}
				//up speed
				if (tableColumns.contains(RemoteConstants.ST_UPLOAD_AVG)) {
					Long uploadAverage = ds.getUploadAverage();
					if(oldUploadAverage != uploadAverage
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_UPLOAD_AVG)).equals(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(uploadAverage))
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_UPLOAD_AVG), DisplayFormatters.formatByteCountToBase10KBEtcPerSec(uploadAverage));
					}
					oldUploadAverage = uploadAverage;
				}
				//Seeds
				if (tableColumns.contains(RemoteConstants.ST_ALL_SEEDS)) {
					int seeds = dl.getTotalSeeds();
					int conSeeds = dl.getSeeds();
					if(oldSeeds != seeds
							|| oldConSeeds != conSeeds
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_ALL_SEEDS)).equalsIgnoreCase("")
							|| bForce){
						if(seeds<0) {
							seeds = 0;
						}
						item.setText(tableColumns.indexOf(RemoteConstants.ST_ALL_SEEDS), conSeeds + " (" + seeds+ ")");
					}
					oldSeeds = seeds;
					oldConSeeds = conSeeds;
				}
				//Leechers
				if (tableColumns.contains(RemoteConstants.ST_ALL_LEECHER)) {
					int leechers = dl.getTotalLeecher();
					int conLeechers = dl.getLeecher();
					if(oldLeechers != leechers
							|| oldConLeechers != conLeechers
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_ALL_LEECHER)).equalsIgnoreCase("")
							|| bForce){
						if(leechers < 0) {
							leechers = 0;
						}
						item.setText(tableColumns.indexOf(RemoteConstants.ST_ALL_LEECHER), conLeechers + " (" + leechers + ")");
					}
					oldLeechers = leechers;
					oldConLeechers = conLeechers;
				}
				//Amount Uploaded
				if (tableColumns.contains(RemoteConstants.ST_UPLOADED)) {
					Long uploaded = ds.getUploaded();
					if(oldUploaded != uploaded
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_UPLOADED)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_UPLOADED),DisplayFormatters.formatByteCountToBase10KBEtc(uploaded));
					}
					oldUploaded = uploaded;
				}

				//Amount Downloaded
				if (tableColumns.contains(RemoteConstants.ST_DOWNLOADED)) {
					Long downloaded = ds.getDownloaded();
					if(oldDownloaded != downloaded
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOADED)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_DOWNLOADED),DisplayFormatters.formatByteCountToBase10KBEtc(downloaded));
					}
					oldDownloaded = downloaded;
				}

				//ETA
				if (tableColumns.contains(RemoteConstants.ST_ETA)) {
					String eta = ds.getETA();
					if(!oldETA.equalsIgnoreCase(eta)
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_ETA)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_ETA),eta);
					}
					oldETA = eta;
				}


				//Status
				if (tableColumns.contains(RemoteConstants.ST_STATUS)) {
					String status = ds.getStatus();
					if(!oldStatus.equalsIgnoreCase(status)
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_STATUS)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_STATUS),status);
					}
					oldStatus = status;
				}

				//Tracker Status
				if (tableColumns.contains(RemoteConstants.ST_TRACKER)) {
					String trackerStatus = ds.getTrackerStatus();
					if(!oldTrackerStatus.equalsIgnoreCase(trackerStatus)
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_TRACKER)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_TRACKER),trackerStatus);
					}
					oldTrackerStatus = trackerStatus;
				}

				//Download Limit
				if (tableColumns.contains(RemoteConstants.ST_LIMIT_DOWN)) {
					int rate = dl.getMaximumDownloadKBPerSecond();
					if(oldDownloadRate != rate
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_DOWN)).equalsIgnoreCase("")
							|| bForce){
						if(rate <= 0) {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_DOWN),"Maximum");
						} else {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_DOWN),DisplayFormatters.formatByteCountToBase10KBEtcPerSec(rate));
						}
					}
					oldDownloadRate = rate;
				}


				//Upload Limit
				if (tableColumns.contains(RemoteConstants.ST_LIMIT_UP)) {
					int rate = dl.getUploadRateLimitBytesPerSecond();
					if(oldUploadRate != rate
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_UP)).equalsIgnoreCase("")
							|| bForce){
						if(rate <= 0) {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_UP),"Maximum");
						} else {
							item.setText(tableColumns.indexOf(RemoteConstants.ST_LIMIT_UP),DisplayFormatters.formatByteCountToBase10KBEtcPerSec(rate));
						}
					}
					oldUploadRate = rate;
				}

				//Discarded
				if (tableColumns.contains(RemoteConstants.ST_DISCARDED)) {
					Long discarded = dl.getDiscarded();
					if(oldDiscarded != discarded
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_DISCARDED)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_DISCARDED),DisplayFormatters.formatByteCountToBase10KBEtc(discarded));
					}
					oldDiscarded = discarded;
				}

				//Size
				if (tableColumns.contains(RemoteConstants.ST_SIZE)) {
					Long size = dl.getSize();
					if(oldSize != size
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_SIZE)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_SIZE),DisplayFormatters.formatByteCountToBase10KBEtc(size));
					}
					oldSize = size;
				}

				//Elapsed Time
				if (tableColumns.contains(RemoteConstants.ST_ELAPSED_TIME)) {
					String elapsedTime = ds.getElapsedTime();
					if(!oldElapsedTime.equalsIgnoreCase(elapsedTime)
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_ELAPSED_TIME)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_ELAPSED_TIME),elapsedTime);
					}
					oldElapsedTime = elapsedTime;
				}

				//Swarm Speed
				if (tableColumns.contains(RemoteConstants.ST_TOTAL_AVG)) {
					Long totalAverage = ds.getTotalAverage();
					if(oldTotalAverage != totalAverage
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_TOTAL_AVG)).equalsIgnoreCase("")
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_TOTAL_AVG),DisplayFormatters.formatByteCountToBase10KBEtc(totalAverage)+"/s");
					}
					oldTotalAverage = totalAverage;
				}


				//Share Ratio
				if (tableColumns.contains(RemoteConstants.ST_SHARE)) {
					float ratio = (ds.getShareRatio()/1000f);
					if(ratio < 0f) {
						ratio = 0f;
					}
					if(oldShareRatio != ratio
							|| item.getText(tableColumns.indexOf(RemoteConstants.ST_SHARE)).equalsIgnoreCase("")
							|| bForce){
						int shareIndex = tableColumns.indexOf(RemoteConstants.ST_SHARE);
						item.setText(shareIndex,df.format(ratio));
						if(ratio < 0.5) {
							item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
						} else if(ratio > 0.05 && ratio < 0.9) {
							item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
						} else {
							item.setForeground(shareIndex, RCMain.getRCMain().getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
						}
					}
					oldShareRatio = ratio;
				}

				//Swarm Speed
				if (tableColumns.contains(RemoteConstants.ST_CATEGORY)) {
					String cat = dl.getTorrentAttribute(RemoteConstants.TA_CATEGORY);
					if (cat == null) {
						cat = "";
					}
					if(!oldCategory.equals(cat)
							|| !item.getText(tableColumns.indexOf(RemoteConstants.ST_CATEGORY)).equalsIgnoreCase(cat)
							|| bForce) {
						item.setText(tableColumns.indexOf(RemoteConstants.ST_CATEGORY),cat);
					}
					oldCategory = cat;
				}
			}
		});
	}

	public static List<Integer> getTotalColumns() {
		return totalTableColumns;
	}

	public static Map<Integer,Comparator<Container>> getComparators() {
		return comparatorCollection;
	}

	/**
	 * @return Returns the dl.
	 */
	public Download getDownload() {
		return dl;
	}

	public void dispose() {
		if (progBarImage != null) {
			progBarImage.dispose();
		}
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
		if (RCMain.getRCMain().isManifestInUse()) {
			return oldHealthIcon;
		}
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
				if (resizedHealthImages[i] != null) {
					resizedHealthImages[i].dispose();
				}
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
