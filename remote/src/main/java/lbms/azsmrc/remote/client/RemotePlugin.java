package lbms.azsmrc.remote.client;

/**
 * @author Damokles
 *
 */
public interface RemotePlugin {

	public String	getPluginDirectoryName();
	public String	getPluginName();
	public String	getPluginID();
	public String	getPluginVersion();
	public boolean 	isBuiltIn() ;
	public boolean 	isDisabled();
	public boolean 	isMandatory();
	public boolean 	isOperational();
	public boolean 	isUnloadable();
	public void 	setDisabled(boolean disabled);
	public void 	uninstall();
}
