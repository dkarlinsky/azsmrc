package lbms.tools.flexyconf.swt;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import lbms.tools.flexyconf.DisplayAdapterSection;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.Section;

public class SWTSection implements DisplayAdapterSection {
	private Composite comp;
	private Tree tree;
	private TreeItem parent;
	private Section section;

	private TreeItem treeItem;
	private Listener selectionListener = new Listener() {
		public void handleEvent(Event event) {
			fillMenu();
		};
	};

	private SWTSection (Section s, Tree t, Composite c, boolean root) {
		section = s;
		tree = t;
		comp = c;
		comp.setLayout(new GridLayout(2,false));
		if (!root) {
			treeItem = new TreeItem(t,SWT.None);
			treeItem.setText(s.getLabel());
			treeItem.addListener(SWT.Selection, selectionListener);
			addSubSections(treeItem, c);
		} else {
			addSubSections(t, c);
		}
	}

	private SWTSection (Section s, TreeItem t, Composite c) {
		section = s;
		parent = t;
		comp = c;
		comp.setLayout(new GridLayout(2,false));
		treeItem = new TreeItem(t,SWT.None);
		treeItem.setText(s.getLabel());
		treeItem.addListener(SWT.Selection, selectionListener);
	}

	private void addSubSections(Tree t, Composite c) {
		List<Section> sects = section.getChildren();
		for (Section s:sects) {
			SWTSection.addAsChild(s,t,c);
		}
	}

	private void addSubSections(TreeItem t, Composite c) {
		List<Section> sects = section.getChildren();
		for (Section s:sects) {
			SWTSection.addAsChild(s,t,c);
		}
	}

	private void fillMenu () {
		Map<String, Entry> entries = section.getEntries();
		Set<String> keys = entries.keySet();
		for (String key:keys) {
			new SWTEntry(entries.get(key),comp);
		}
	}

	public static SWTSection addAsRoot (Section s, Tree t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,true);
		return sect;
	}

	public static SWTSection addAsChild (Section s, Tree t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,false);
		return sect;
	}

	public static SWTSection addAsChild (Section s, TreeItem t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c);
		return sect;
	}
}
