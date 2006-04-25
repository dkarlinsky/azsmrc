package lbms.tools.flexyconf.swt;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import lbms.tools.flexyconf.*;

public class SWTMenu implements DisplayAdapter {

	private Tree tree;
	private Composite target;
	private FlexyConfiguration fc;

	public SWTMenu (FlexyConfiguration c,Tree t, Composite target) {
		tree = t;
		this.target = target;
		fc = c;
		tree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				System.out.println("Tree Selected");
				TreeItem t =(TreeItem)e.item;
				SWTSection s = (SWTSection)t.getData("SWTSection");
				s.fillMenu();
			}
		});
	}

	public void addAsRoot () {
		SWTSection.addAsRoot(fc.getRootSection(), tree, target);
	}

	public void addAsSubItem() {
		SWTSection.addAsChild(fc.getRootSection(), tree, target);
	}
}
