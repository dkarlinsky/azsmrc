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
		label = new Label(comp,SWT.NULL);
		label.setText(entry.getLabel());
		if (entry.getType() == Entry.TYPE_BOOLEAN) {
			control = new Button(comp, SWT.CHECK);
			((Button)control).addSelectionListener(new SelectionAdapter () {
				@Override
				public void widgetSelected(SelectionEvent e) {
					entry.setValue(Boolean.toString(((Button)control).getSelection()));
				}
			});
		} else {
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

	public void updateValue() {
		if (entry.getType() == Entry.TYPE_BOOLEAN) {
			((Button)control).setSelection(Boolean.parseBoolean(entry.getValue()));
		} else {
			((Text)control).setText(entry.getValue());
		}
	}
}
