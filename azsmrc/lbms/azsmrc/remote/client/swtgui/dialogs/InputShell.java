package lbms.azsmrc.remote.client.swtgui.dialogs;

import lbms.azsmrc.remote.client.internat.I18N;
import lbms.azsmrc.remote.client.swtgui.GUI_Utilities;
import lbms.azsmrc.remote.client.swtgui.RCMain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class InputShell {
	private String sTitleKey;

	private String Labeltext;

	private String textValue;

	private Text text;

	private boolean bIsPassword = false;


	public InputShell(String Title, String Labeltext) {
		this.sTitleKey = Title;

		this.Labeltext = Labeltext;


		this.setTextValue("");
	}

	public String open() {
		final Display display = RCMain.getRCMain().getDisplay();
		if (display == null)
			return null;

		final Shell shell = new Shell(display.getActiveShell());
		shell.setText(sTitleKey);


		GridLayout layout = new GridLayout();
		shell.setLayout(layout);

		Label label = new Label(shell, SWT.WRAP);
		label.setText(Labeltext);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 200;
		label.setLayoutData(gridData);

		if(this.bIsPassword){
			text = new Text(shell, SWT.BORDER);
		}else{
			text = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		}
		gridData = new GridData();
		gridData.widthHint = 300;
		text.setLayoutData(gridData);
		text.setText(textValue);
		text.selectAll();

		Composite panel = new Composite(shell, SWT.NULL);
		layout = new GridLayout();
		layout.numColumns = 3;
		panel.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		panel.setLayoutData(gridData);
		Button ok = new Button(panel, SWT.PUSH);
		ok.setText(I18N.translate("global.ok"));
		gridData = new GridData();
		gridData.widthHint = 70;
		ok.setLayoutData(gridData);
		shell.setDefaultButton(ok);
		ok.addListener(SWT.Selection, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				try {
					setTextValue(text.getText());
					shell.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Button cancel = new Button(panel, SWT.PUSH);
		cancel.setText(I18N.translate("global.cancel"));
		gridData = new GridData();
		gridData.widthHint = 70;
		cancel.setLayoutData(gridData);
		cancel.addListener(SWT.Selection, new Listener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		shell.pack();
		GUI_Utilities.centerShellandOpen(shell);
		setTextValue(null);
		shell.open();

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		return getTextValue();
	}

	/**
	 * @param textValue The textValue to set.
	 */
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}

	/**
	 * @return Returns the textValue.
	 */
	public String getTextValue() {
		return textValue;
	}

	/**
	 * Ability to set the input as a password "*" input.. must be set before open()
	 * @param _bIsPassword
	 */
	public void setIsPassword(boolean _bIsPassword){
		this.bIsPassword = _bIsPassword;
	}

}


