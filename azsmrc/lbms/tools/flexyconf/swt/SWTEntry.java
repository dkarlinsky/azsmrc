package lbms.tools.flexyconf.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

import lbms.tools.flexyconf.*;

public class SWTEntry implements DisplayAdapterEntry {

	private static Cursor handCursor;
	private static Color blueColor;
	private static final int NUMERIC_FIELD_WIDTH = 30;

	private Composite comp;
	private Entry entry;
	private Control control;
	private Label label;


	public SWTEntry (Entry e, Composite c) {
		try {
			entry = e;
			comp = c;
			entry.setDisplayAdapter(this);
			init ();
			if (entry.getType() == Entry.TYPE_BOOLEAN) { //Boolean

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

			} else if(entry.getType() == Entry.TYPE_LABEL) { //Label

				label = new Label(comp,SWT.NULL);
				GridData gd = new GridData(GridData.FILL_HORIZONTAL);
				gd.horizontalSpan = 2;
				label.setLayoutData(gd);

			} else if(entry.getType() == Entry.TYPE_URL) { //URL

					GridData gd = new GridData(GridData.FILL_HORIZONTAL);
					gd.horizontalSpan = 2;
					label = new Label(comp, SWT.NULL);
					label.setCursor(handCursor);
					label.setForeground(blueColor);
					label.setLayoutData( gd );
					label.addMouseListener(new MouseAdapter() {
					  public void mouseDoubleClick(MouseEvent arg0) {
						Program.launch(entry.getKey());
					  }
					  public void mouseUp(MouseEvent arg0) {
						Program.launch(entry.getKey());
					  }
					});

			} else { //anything else

				label = new Label(comp,SWT.NULL);
				if (entry.isOption()) {
					control = new Combo(comp,SWT.DROP_DOWN | SWT.READ_ONLY);

					Option[] opts = entry.getOptions();
					for (Option opt:opts) {
						((Combo)control).add(opt.getLabel());
					}
					((Combo)control).select(0);
					((Combo)control).addSelectionListener(new SelectionListener() {
						public void widgetDefaultSelected(SelectionEvent e) {}
						public void widgetSelected(SelectionEvent e) {
							Combo comb = (Combo)control;
							int selected = comb.getSelectionIndex();
							entry.setValue(entry.getOptions()[selected].getValue());
						}
					});
				} else {
					control = new Text(comp, SWT.SINGLE | SWT.BORDER);
					if(entry.getType() == Entry.TYPE_INT ||
							entry.getType() == Entry.TYPE_LONG) {

						GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
						gd.widthHint = NUMERIC_FIELD_WIDTH;
						control.setLayoutData(gd);
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
							entry.getType() == Entry.TYPE_FLOAT) {

						GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
						gd.widthHint = NUMERIC_FIELD_WIDTH;
						control.setLayoutData(gd);
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
					} else {
						GridData gd = new GridData(GridData.FILL_HORIZONTAL);
						control.setLayoutData(gd);
					}

					control.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent e) {}

						public void focusLost(FocusEvent e) {
							entry.setValue(((Text)control).getText());
						}
					});
				}
			}
			if (label != null)
				label.setText(entry.getLabel());
		} catch (SWTException e1) {
			e1.printStackTrace();
		}
	}

	private void init () {
		if (handCursor != null) return;
		Display display = comp.getDisplay();
		handCursor = new Cursor(display, SWT.CURSOR_HAND);
		blueColor = new Color(display, new RGB(0, 0, 170));
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
			if (label != null) label.setEnabled(e);
			if (control != null) control.setEnabled(e);
		} catch (SWTException e1) {
			e1.printStackTrace();
		}
	}

	public void updateValue() {
		if (entry.getType() == Entry.TYPE_LABEL || entry.getType() == Entry.TYPE_URL) {

		} else if (entry.getType() == Entry.TYPE_BOOLEAN) {
			((Button)control).setSelection(Boolean.parseBoolean(entry.getValue()));
		} else if (entry.isOption()) {
			Combo comb = (Combo)control;
			String v = entry.getValue();
			Option[] opts = entry.getOptions();
			for (int i=0;i<opts.length;i++) {
				if (opts[i].getValue().equals(v)) {
					comb.select(i);
					return;
				}
			}
		} else {
			((Text)control).setText(entry.getValue());
		}
	}
}
