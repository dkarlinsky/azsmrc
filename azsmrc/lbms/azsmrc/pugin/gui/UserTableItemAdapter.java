package lbms.azsmrc.pugin.gui;

import lbms.azsmrc.pugin.main.Plugin;
import lbms.azsmrc.pugin.main.User;
import lbms.azsmrc.shared.RemoteConstants;
import lbms.azsmrc.shared.UserNotFoundException;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class UserTableItemAdapter {
	
	User currentUser;
	User itemUser;
	
	/**
	 * @param item The User for the TableItem object.
	 * @param currentUser The current User, this is used for access checks.
	 */
	public UserTableItemAdapter (User itemUser, User currentUser) {
		this.itemUser = itemUser;
		this.currentUser = currentUser; 
	}
	
	/**
	 * @param itemUser
	 * @param currentUser
	 * @throws UserNotFoundException
	 */
	public UserTableItemAdapter (String itemUser, User currentUser) throws UserNotFoundException {
		this.itemUser = Plugin.getXMLConfig().getUser(itemUser);
		this.currentUser = currentUser; 
		if (this.itemUser == null) throw new UserNotFoundException ();
	}
	
	
	
	/**
	 * @param parent a composite control which will be the parent of the new instance
	 * @param style the style of control to construct 
	 * @return TableItem for use in the display
	 */
	public TableItem getTableItem (Table parent, int style) {
		TableItem item = new TableItem (parent,style);		
		return getTableItem(item);
	}
	
	/**
	 * @param parent a composite control which will be the parent of the new instance
	 * @param style the style of control to construct 
	 * @param index
	 * @return the index to store the receiver in its parent 
	 */
	public TableItem getTableItem (Table parent, int style, int index) {
		TableItem item = new TableItem (parent,style,index);		
		return getTableItem(item);
	}
	
	/**
	 * This function will fill the TableItem
	 * with the prior provided User info.
	 * 
	 * @param item TableItem that will be filled
	 * @return the filled TableItem
	 */
	public TableItem getTableItem (TableItem item) {
		item.setText(0,itemUser.getUsername());
        //TODO set user type here
        if (currentUser.checkAccess(RemoteConstants.RIGHTS_ADMIN) || currentUser == itemUser) { //checks if current User has the rights to see this.        	
        	item.setText(1,itemUser.getRole());        	
        	item.setText(2,String.valueOf(itemUser.getDownloadSlots()));
            item.setText(3,String.valueOf(itemUser.getDownloads().length));
            item.setText(4,itemUser.getOutputDir());
        	item.setText(5,itemUser.getAutoImportDir());
        } else {
        	item.setText(1,"*hidden*");
        	item.setText(2,"*hidden*");
        	item.setText(3,"*hidden*");
            item.setText(4,"*hidden*");
            item.setText(5,"*hidden*");
        }
		return item;
	}
}
