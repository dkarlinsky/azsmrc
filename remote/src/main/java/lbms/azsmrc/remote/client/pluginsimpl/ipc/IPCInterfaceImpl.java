package lbms.azsmrc.remote.client.pluginsimpl.ipc;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import lbms.azsmrc.remote.client.plugins.ipc.IPCException;
import lbms.azsmrc.remote.client.plugins.ipc.IPCInterface;

/**
 * @author Damokles
 * 
 */

public class IPCInterfaceImpl implements IPCInterface {

	WeakReference<Object>	reference;

	public IPCInterfaceImpl(Object _target) {
		reference = new WeakReference<Object>(_target);
	}

	public Object invoke (String methodName, Object[] params)
			throws IPCException {

		Object target = reference.get();
		if (target == null) {
			throw new IPCException(
					"Invalid Operation. Target was already recycled.");
		}
		try {

			if (params == null) {
				params = new Object[0];
			}

			Class<?>[] paramTypes = new Class[params.length];
			for (int i = 0; i < params.length; i++) {
				if (params[i] instanceof Boolean) {
					paramTypes[i] = boolean.class;
				} else if (params[i] instanceof Integer) {
					paramTypes[i] = int.class;
				} else if (params[i] instanceof Long) {
					paramTypes[i] = long.class;
				} else if (params[i] instanceof Float) {
					paramTypes[i] = float.class;
				} else if (params[i] instanceof Double) {
					paramTypes[i] = double.class;
				} else if (params[i] instanceof Byte) {
					paramTypes[i] = byte.class;
				} else if (params[i] instanceof Character) {
					paramTypes[i] = char.class;
				} else if (params[i] instanceof Short) {
					paramTypes[i] = short.class;
				} else {
					paramTypes[i] = params[i].getClass();
				}
			}

			Method mtd = null;

			try {
				mtd = target.getClass().getMethod(methodName, paramTypes);

			} catch (NoSuchMethodException e) {

				Method[] methods = target.getClass().getMethods();

				for (int i = 0; i < methods.length; i++) {

					Method method = methods[i];

					Class<?>[] method_params = method.getParameterTypes();

					if (method.getName().equals(methodName)
							&& method_params.length == paramTypes.length) {

						boolean ok = true;

						for (int j = 0; j < method_params.length; j++) {

							Class<?> declared = method_params[j];
							Class<?> supplied = paramTypes[j];

							if (!declared.isAssignableFrom(supplied)) {

								ok = false;

								break;
							}
						}

						if (ok) {

							mtd = method;

							break;
						}
					}
				}

				if (mtd == null) {

					throw (e);
				}
			}

			return mtd.invoke(target, params);
		} catch (Exception e) {
			throw new IPCException(e);
		}
	}
}
