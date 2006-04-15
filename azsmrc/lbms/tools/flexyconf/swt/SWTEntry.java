package lbms.tools.flexyconf.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import lbms.tools.flexyconf.*;

public class SWTEntry implements DisplayAdapterEntry {
	private Composite comp;
	private Entry entry;
	private Control control;

	public SWTEntry (Entry e, Composite c) {
		entry = e;
		comp = c;
		e.setDisplayAdapter(this);
		Label label = new Label(comp,SWT.NULL);
		label.setText(entry.getLable());
		if (e.getType() == Entry.TYPE_BOOLEAN) {
			control = new Button(comp, SWT.CHECK);
		} else {
			control = new Text(comp, SWT.SINGLE | SWT.BORDER);
		}
	}

	public boolean isEnabled() {
		return control.isEnabled();
	}

	public void setEnabled(boolean e) {
		control.setEnabled(e);
	}
}
