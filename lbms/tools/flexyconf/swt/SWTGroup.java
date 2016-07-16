package lbms.tools.flexyconf.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import lbms.tools.flexyconf.DisplayAdapterGroup;
import lbms.tools.flexyconf.Entry;
import lbms.tools.flexyconf.Group;

public class SWTGroup implements DisplayAdapterGroup {
	private Composite comp;
	private Group group;
	private org.eclipse.swt.widgets.Group swtGroup;

	public SWTGroup (Group g, Composite parent) {
		try {
			this.group = g;
			this.comp = parent;
			//new Label(parent,SWT.NULL);
			swtGroup = new org.eclipse.swt.widgets.Group(parent, SWT.NULL);
			swtGroup.setLayout(new GridLayout(2,false));
			swtGroup.setText(group.getLabel());
			GridData gd = new GridData(GridData.VERTICAL_ALIGN_END);
			gd.verticalIndent = 10;
			gd.horizontalSpan = 2;
			swtGroup.setLayoutData(gd);

			Entry[] entries = group.getSortedEntries();
			for (Entry e:entries) {
				new SWTEntry(e,swtGroup);
			}
			
			group.init();
			swtGroup.layout();
		} catch (SWTException e) {
			e.printStackTrace();
		}
	}
}
