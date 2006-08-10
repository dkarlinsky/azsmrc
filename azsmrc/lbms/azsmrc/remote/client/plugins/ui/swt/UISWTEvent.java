/**
 * 
 */
package lbms.azsmrc.remote.client.plugins.ui.swt;

/**
 * @author Damokles
 *
 */
public interface UISWTEvent {

	public static final int TYPE_CREATE = 0;

	public static final int TYPE_INITIALIZE = 1;

	public static final int TYPE_REFRESH = 2;

	public static final int TYPE_DESTROY = 3;

	public int getType();

	public Object getData();

}
