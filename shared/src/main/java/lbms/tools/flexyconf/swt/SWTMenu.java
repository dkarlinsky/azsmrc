package lbms.tools.flexyconf.swt;

import lbms.tools.flexyconf.DisplayAdapter;
import lbms.tools.flexyconf.FlexyConfiguration;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SWTMenu implements DisplayAdapter {

	private Tree tree;
	private Composite target;
	private FlexyConfiguration fc;

	public SWTMenu (FlexyConfiguration c,Tree t, Composite target) {
		tree = t;
		this.target = target;
		fc = c;
		tree.addSelectionListener(new SelectionListener() {
			private String domain = fc.getFCInterface().getDomain();
			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				TreeItem t =(TreeItem)e.item;
				SWTSection s = (SWTSection)t.getData(domain+"_SWTSection");
				if (s!=null)
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
	
	public void addAsRoot (TreeItem item) {
		SWTSection.addAsRoot(fc.getRootSection(), item, target);
	}

	public void addAsSubItem(TreeItem item) {
		SWTSection.addAsChild(fc.getRootSection(), item, target);
	}
}
