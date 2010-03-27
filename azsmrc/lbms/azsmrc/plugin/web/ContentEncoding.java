/**
 *
 */
package lbms.azsmrc.plugin.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

/**
 * @author Leonard
 *
 */
public enum ContentEncoding {
	Normal, Gzip, Deflate;

	public static ContentEncoding parseFromHeaders (Map<String, String> headers) {
		if (headers.containsKey("accept-encoding")) {
			String encoding = headers.get("accept-encoding").toLowerCase();
			if (encoding.contains("gzip")) {
				return Gzip;
			} else if (encoding.contains("gzip")) {
				return Deflate;
			}
		}

		return Normal;
	}

	public OutputStream wrapStream (TrackerWebPageResponse response)
			throws IOException {
		OutputStream os = response.getOutputStream();
		switch (this) {
		case Gzip:
			response.setHeader("Content-Encoding", this.name().toLowerCase());
			return new GZIPOutputStream(os);

		case Deflate:
			response.setHeader("Content-Encoding", this.name().toLowerCase());
			return new DeflaterOutputStream(os);

		default:
			return os;
		}
	}
}
