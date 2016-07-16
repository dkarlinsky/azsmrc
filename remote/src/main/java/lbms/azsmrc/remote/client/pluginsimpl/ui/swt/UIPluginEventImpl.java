package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent;

/**
 * @author Damokles
 *
 */
public class UIPluginEventImpl implements UIPluginEvent {

	private Object data, dataSource;
	private int type;



	public UIPluginEventImpl(Object data, int type, Object dataSource) {
		super();
		this.data = data;
		this.type = type;
		this.dataSource = dataSource;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent#getData()
	 */
	public Object getData() {
		// TODO Auto-generated method stub
		return data;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent#getDataSource()
	 */
	public Object getDataSource() {
		// TODO Auto-generated method stub
		return dataSource;
	}

	/* (non-Javadoc)
	 * @see lbms.azsmrc.remote.client.plugins.ui.swt.UIPluginEvent#getType()
	 */
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}

}
