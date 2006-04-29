package lbms.tools.flexyconf.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
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
		entry.setDisplayAdapter(this);
		if (entry.getType() == Entry.TYPE_BOOLEAN) {
			control = new Button(comp, SWT.CHECK);
			((Button)control).addSelectionListener(new SelectionAdapter () {
				@Override
				public void widgetSelected(SelectionEvent e) {
					entry.setValue(Boolean.toString(((Button)control).getSelection()));
				}
			});
			((Button)control).setText(entry.getLabel());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			control.setLayoutData(gd);
		} else if(entry.getType() == Entry.TYPE_LABEL) {
			label = new Label(comp,SWT.NULL);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			label.setLayoutData(gd);
		} else {
			label = new Label(comp,SWT.NULL);
			control = new Text(comp, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			control.setLayoutData(gd);
			control.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {}

				public void focusLost(FocusEvent e) {
					entry.setValue(((Text)control).getText());
				}
			});
		}
		if (label != null)
			label.setText(entry.getLabel());
	}

	public void dispose() {
		if (label != null && !label.isDisposed()) label.dispose();
		if (control!=null && !control.isDisposed()) control.dispose();
	}

	public boolean isEnabled() {
		if (entry.getType() == Entry.TYPE_LABEL) return false;
		return control.isEnabled();
	}

	public void setEnabled(boolean e) {
		if (entry.getType() == Entry.TYPE_LABEL) return;
		control.setEnabled(e);
	}

	public void updateValue() {
		if (entry.getType() == Entry.TYPE_LABEL) {

		} else if (entry.getType() == Entry.TYPE_BOOLEAN) {
			((Button)control).setSelection(Boolean.parseBoolean(entry.getValue()));
		} else {
			((Text)control).setText(entry.getValue());
		}
	}
}
