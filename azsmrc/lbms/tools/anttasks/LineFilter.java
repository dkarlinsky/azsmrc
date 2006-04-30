package lbms.tools.anttasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class LineFilter extends Task {

	private String source, target, pattern;
	private Pattern filter;

	public void setSource (String src) {
		this.source = src;
	}

	public void setTarget (String target) {
		this.target = target;
	}

	public void setFilter (String filter) {
		this.pattern = filter;
	}

	@Override
	public void execute() throws BuildException {
		if (source==null)
			throw new BuildException("source may not be null");

		if (target==null)
			throw new BuildException("target may not be null");

		if (pattern==null)
			throw new BuildException("filter may not be null");

		try {
			filter = Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			throw new BuildException(e);
		}
		File srcFile = new File (source);
		if (!srcFile.exists() || !srcFile.canRead())
			throw new BuildException("File/Dir does not exist "+source);
		File targetDir  = new File(target);
		if (!targetDir.exists())
			targetDir.mkdirs();
		try {
			if (srcFile.isDirectory()) {
				convertDiretory(srcFile, targetDir);
			} else {

				convertFile(srcFile, targetDir);

			}
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

	public void convertDiretory (File srcDir, File tgtDir) throws IOException {
		File[] files = srcDir.listFiles();
		for (File f:files) {
			if (f.isFile()) convertFile(f, tgtDir);
		}
	}

	public void convertFile(File inf, File oDir) throws IOException {
		BufferedReader br = null;
		BufferedWriter bw = null;
		System.out.println("Converting: "+inf.getAbsolutePath());
		try {
			br = new BufferedReader(new FileReader(inf));
			File out = new File (oDir,inf.getName());
			bw = new BufferedWriter(new FileWriter(out));
			do {
				String line = br.readLine();
				if (line == null) break;
				if (filter.matcher(line).find()) {
					bw.write(line+"\n");
				}
			} while (true);
		} finally {
			if (br!=null)br.close();
			if (bw!=null)bw.close();
		}
	}
}
