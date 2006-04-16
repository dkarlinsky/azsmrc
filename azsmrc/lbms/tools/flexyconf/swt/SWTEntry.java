package lbms.tools.flexyconf.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import lbms.tools.flexyconf.*;

public class SWTEntry implements DisplayAdapterEntry {
	private Composite comp;
	private Entry entry;
	private Control control;
	private Label label;

	public SWTEntry (Entry e, Composite c) {
		entry = e;
		comp = c;
		e.setDisplayAdapter(this);
		label = new Label(comp,SWT.NULL);
		label.setText(entry.getLabel());
		if (e.getType() == Entry.TYPE_BOOLEAN) {
			control = new Button(comp, SWT.CHECK);
		} else {
			control = new Text(comp, SWT.SINGLE | SWT.BORDER);
		}
	}

	public void dispose() {
		if (!label.isDisposed()) label.dispose();
		if (!control.isDisposed()) control.dispose();
	}

	public boolean isEnabled() {
		return control.isEnabled();
	}

	public void setEnabled(boolean e) {
		control.setEnabled(e);
	}
}
