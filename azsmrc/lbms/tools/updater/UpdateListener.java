package lbms.tools.updater;

public interface UpdateListener {
	public void exception(Exception e);
	public void updateAvailable(Update update);
	public void noUpdate();
	public void updateFinished();
	public void updateError (String error);
	public void updateFailed(String reason);
}
