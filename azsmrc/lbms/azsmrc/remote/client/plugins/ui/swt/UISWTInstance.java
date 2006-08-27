package lbms.azsmrc.remote.client.plugins.ui.swt;

public interface UISWTInstance {

	public UISWTView[] getOpenViews(String sParentID);

	public void removeViews(String sParentID, String sViewID);

	public void openMainView(String sViewID, UISWTViewEventListener l, Object dataSource);

	public void addView(String sParentID, String sViewID, UISWTViewEventListener l);



}