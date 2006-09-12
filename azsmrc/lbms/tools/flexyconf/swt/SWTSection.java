package lbms.tools.flexyconf.swt;

import java.util.Set;

import lbms.tools.flexyconf.ConfigEntity;
import lbms.tools.flexyconf.DisplayAdapterSection;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.Group;
import lbms.tools.flexyconf.Section;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SWTSection implements DisplayAdapterSection {
	private Composite comp;
	private Section section;
	private TreeItem treeItem;


	private SWTSection (Section s, Tree t, Composite c, boolean root) {
		try {
			section = s;
			comp = c;
			comp.setLayout(new GridLayout(2,false));
			if (!root) {
				treeItem = new TreeItem(t,SWT.None);
				treeItem.setText(s.getLabel());
				treeItem.setData(section.getFCInterface().getDomain()+"_SWTSection", this);
				addSubSections(treeItem, c);
			} else {
				addSubSections(t, c);
			}
		} catch (SWTException e) {
			e.printStackTrace();
		}
	}

	private SWTSection (Section s, TreeItem t, Composite c, boolean root) {
		try {
			section = s;
			comp = c;
			if (!root) {
				treeItem = new TreeItem(t,SWT.None);
				treeItem.setText(s.getLabel());
				treeItem.setData(section.getFCInterface().getDomain()+"_SWTSection", this);
				addSubSections(treeItem, c);
			} else {
				addSubSections(t, c);
			}
		} catch (SWTException e) {
			e.printStackTrace();
		}
	}

	private void addSubSections(Tree t, Composite c) {
		Set<Section> sects = section.getChildren();
		for (Section s:sects) {
			SWTSection.addAsChild(s,t,c);
		}
	}

	private void addSubSections(TreeItem t, Composite c) {
		Set<Section> sects = section.getChildren();
		for (Section s:sects) {
			SWTSection.addAsChild(s,t,c);
		}
	}

	protected void fillMenu () {
		try {
			clearMenu ();
			comp.setLayout(new GridLayout(2,false));
			ConfigEntity[] entities = section.getSortedConfigEntities();
			for (ConfigEntity e:entities) {
				if (e instanceof Entry) {
					System.out.println("Creating SWTEntry");
					new SWTEntry((Entry)e,comp);
				} else if (e instanceof Group) {
					System.out.println("Creating SWTGroup");
					new SWTGroup((Group)e,comp);
				} else {
					System.out.println("Arghh!!");
				}
			}
			section.init();
			comp.layout();
			if (comp.getParent() instanceof ScrolledComposite) {
				((ScrolledComposite) comp.getParent()).setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		} catch (SWTException e) {
			e.printStackTrace();
		}
	}

	private void clearMenu () {
		try {
			Control[] controls = comp.getChildren();
			for (Control c:controls) {
				c.dispose();
			}
		} catch (SWTException e) {
			e.printStackTrace();
		}
	}

	public static SWTSection addAsRoot (Section s, Tree t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,true);
		return sect;
	}

	public static SWTSection addAsRoot (Section s, TreeItem t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,true);
		return sect;
	}

	public static SWTSection addAsChild (Section s, Tree t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,false);
		return sect;
	}

	public static SWTSection addAsChild (Section s, TreeItem t, Composite c) {
		SWTSection sect = new SWTSection(s,t,c,false);
		return sect;
	}
}