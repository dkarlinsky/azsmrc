package lbms.tools.flexyconf.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
		try {
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
				if(entry.getType() == Entry.TYPE_INT ||
						entry.getType() == Entry.TYPE_LONG) {
					control.addListener (SWT.Verify, new Listener () {
						public void handleEvent (Event e) {
							String string = e.text;
							char [] chars = new char [string.length ()];
							string.getChars (0, chars.length, chars, 0);
							for (int i=0; i<chars.length; i++) {
								if (!('0' <= chars [i] && chars [i] <= '9')) {
									e.doit = false;
									return;
								}
							}
						}
					});
				} else if(entry.getType() == Entry.TYPE_DOUBLE ||
						entry.getType() == Entry.TYPE_FLOAT){
					control.addListener (SWT.Verify, new Listener () {
						public void handleEvent (Event e) {
							String string = e.text;
							char [] chars = new char [string.length ()];
							string.getChars (0, chars.length, chars, 0);
							for (int i=0; i<chars.length; i++) {
								if (!('0' <= chars [i] && chars [i] <= '9') && chars [i] != '.') {
									e.doit = false;
									return;
								}
							}
						}
					});
				}

				control.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {}

					public void focusLost(FocusEvent e) {
						entry.setValue(((Text)control).getText());
					}
				});
			}
			if (label != null)
				label.setText(entry.getLabel());
		} catch (SWTException e1) {
			e1.printStackTrace();
		}
	}

	public void dispose() {
		if (label != null && !label.isDisposed()) label.dispose();
		if (control!=null && !control.isDisposed()) control.dispose();
	}

	public boolean isEnabled() {
		try {
			if (entry.getType() == Entry.TYPE_LABEL) return false;
			return control.isEnabled();
		} catch (SWTException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void setEnabled(boolean e) {
		try {
			if (entry.getType() == Entry.TYPE_LABEL) return;
			control.setEnabled(e);
		} catch (SWTException e1) {
			e1.printStackTrace();
		}
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
