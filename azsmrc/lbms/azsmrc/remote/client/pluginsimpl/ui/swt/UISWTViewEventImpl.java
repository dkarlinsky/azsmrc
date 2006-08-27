package lbms.azsmrc.remote.client.pluginsimpl.ui.swt;

import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTView;
import lbms.azsmrc.remote.client.plugins.ui.swt.UISWTViewEvent;

public class UISWTViewEventImpl implements UISWTViewEvent {
	int eventType;
	Object data;
	UISWTView view;


	public UISWTViewEventImpl(UISWTView view, int eventType, Object data) {
		this.view = view;
		this.eventType = eventType;
		this.data = data;
	}

	public int getType() {
		return eventType;
	}

	public Object getData() {
		return data;
	}

	public UISWTView getView() {
		return view;
	}
}
