package lbms.azsmrc.remote.client.swtgui.container;

import java.util.ArrayList;
import java.util.List;

import lbms.azsmrc.remote.client.Download;
import lbms.azsmrc.remote.client.swtgui.RCMain;
import lbms.azsmrc.shared.EncodingUtil;
import lbms.azsmrc.shared.RemoteConstants;

import org.eclipse.swt.widgets.Table;

public class SeedContainer extends Container{

	private static List<Integer> tableColumns = new ArrayList<Integer>();


	static {
		tableColumns.add(RemoteConstants.ST_HEALTH);
		tableColumns.add(RemoteConstants.ST_POSITION);
		tableColumns.add(RemoteConstants.ST_NAME);
		tableColumns.add(RemoteConstants.ST_UPLOADED);
		tableColumns.add(RemoteConstants.ST_UPLOAD_AVG);
		tableColumns.add(RemoteConstants.ST_ALL_LEECHER);
		tableColumns.add(RemoteConstants.ST_AVAILABILITY);

	}

	public SeedContainer(Download dl, Table parent, int style) {
		super(dl, parent, style);
		update(true);
	}



	public SeedContainer(Download dl) {
		super(dl);
		// TODO Auto-generated constructor stub
	}



	public void update(boolean bForce) {
		//super.update(bForce);
		updateData(tableColumns, bForce);
	}

	//@Override
	public static List<Integer> getColumns() {
		return tableColumns;
	}


	public static void loadColumns() {
		String colls = RCMain.getRCMain().getProperties().getProperty("seedTable.columns", null);
		if (colls == null) return;
		tableColumns.clear();
		tableColumns = EncodingUtil.StringToIntegerList(colls);
	}

	public static void saveColumns() {
		RCMain.getRCMain().getProperties().setProperty("seedTable.columns", EncodingUtil.IntListToString(tableColumns));
	}
}
