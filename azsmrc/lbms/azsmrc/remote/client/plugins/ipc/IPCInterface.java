package lbms.azsmrc.remote.client.plugins.ipc;

/**
 * @author Damokles
 *
 */
public interface IPCInterface {

	/**
	 * This function will call the given method on the plugin.
	 * 
	 * This function allows direct method calls to the plugin
	 * using Java Reflection API.
	 * 
	 * Primitives like int, boolean need to be wrappen in their
	 * Objects (int -> Integer).
	 * 
	 * Results will be returned as Object and can be classcasted.
	 * 
	 * WARNING: only call Methods that use Java or Azureus Classes
	 * 			the use of custom classes may cause problems.
	 * 
	 * Example:
	 * 
	 * 1.
	 * Plugin has method
	 * int add (int x, int y);
	 * 
	 * int result = ((Integer)invoke ("add", new Object[] {Integer.valueOf(10),Integer.valueOf(5)}).intValue();
	 * //result (15)
	 * 
	 * 2.
	 * String randomize (String x);
	 * 
	 * String result = (String)invoke("randomize", new Object[]{"foobar"});
	 * //result ("bfaoro")
	 * 
	 * 
	 * @param methodName the name of the Methods to be called
	 * @param params Parameters of the Method
	 * @return returns the result of the method
	 */
	public Object invoke (String methodName, Object[] params) throws IPCException;

}
