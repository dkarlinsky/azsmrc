package lbms.azsmrc.remote.client.plugins.ui.swt;

public interface UISWTInstance {

	public UISWTView[] getOpenViews(ViewID sParentID);

	public void removeViews(ViewID sParentID, String sViewID);

	public void openMainView(ViewID sViewID, UISWTViewEventListener l, Object dataSource);

	public void addView(ViewID sParentID, String sViewID, UISWTViewEventListener l);



}