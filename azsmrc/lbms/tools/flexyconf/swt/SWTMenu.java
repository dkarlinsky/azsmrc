package lbms.tools.flexyconf.swt;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import lbms.tools.flexyconf.*;

public class SWTMenu implements DisplayAdapter {

	private Composite comp;

	public SWTMenu (Composite c) {
		comp = c;
		comp.setLayout(new GridLayout(2,false));
	}

	public void addEntry (Entry e) {
		new SWTEntry (e,comp);
	}
}
