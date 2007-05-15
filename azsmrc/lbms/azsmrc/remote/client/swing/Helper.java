/*
 * Helper.java
 *
 * Created on 12. Mai 2007, 18:04
 */

package lbms.azsmrc.remote.client.swing;

import lbms.azsmrc.remote.client.internat.I18N;

/**
 *
 * @author  Leonard
 */
public class Helper extends javax.swing.JFrame {

	private static final String PFX = "dialog.helperdialog.";

	/** Creates new form Helper */
	public Helper(String suggestion) {
		initComponents();
		helpPane.setText(suggestion);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		helpPane = new javax.swing.JEditorPane();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jLabel1.setText(I18N.translate(PFX+"title"));

		helpPane.setEditable(false);
		jScrollPane1.setViewportView(helpPane);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
					.addComponent(jLabel1))
				.addContainerGap())
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(jLabel1)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
				.addContainerGap())
		);

		pack();
	}// </editor-fold>//GEN-END:initComponents

		// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JEditorPane helpPane;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JScrollPane jScrollPane1;
	// End of variables declaration//GEN-END:variables

}
