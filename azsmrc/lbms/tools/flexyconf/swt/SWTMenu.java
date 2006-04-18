package lbms.tools.flexyconf.swt;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import lbms.tools.flexyconf.*;

public class SWTMenu implements DisplayAdapter {

	private Tree tree;
	private Composite target;
	private FlexyConfiguration fc;

	public SWTMenu (FlexyConfiguration c,Tree t, Composite target) {
		tree = t;
		this.target = target;
		fc = c;
	}

	public void addAsRoot () {
		SWTSection.addAsRoot(fc.getRootSection(), tree, target);
	}

	public void addAsSubItem() {
		SWTSection.addAsChild(fc.getRootSection(), tree, target);
	}
}
