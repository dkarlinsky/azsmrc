package lbms.tools.updater;

import java.util.List;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;



public class UpdateList {
	private SortedSet<Update> uSet = new TreeSet<Update>(new Comparator<Update>() {
		public int compare(Update o1, Update o2) {
			return o1.compareTo(o2)*-1; //We need Descending order to speed things up
		}
	});

	public UpdateList() {

	}

	public UpdateList(Document xml) {
		Element root = xml.getRootElement();
		List<Element> updates = root.getChildren("Update");
		for (Element u:updates) {
			uSet.add(new Update(u));
		}
	}

	public Document toDocument() {
		Document doc = new Document();
		Element root = new Element ("Updates");
		Iterator<Update> it = uSet.iterator();
		while (it.hasNext()) {
			root.addContent(it.next().toElement());
		}
		doc.addContent(root);
		return doc;
	}

	public Update getLatest () {
		return uSet.first();
	}

	public Update getLatestStable() {
		Iterator<Update> it = uSet.iterator();
		Update result = null;
		while (it.hasNext()) {
			result = it.next();
			if (result.isStable()) break;
			result = null;
		}
		return result;
	}

	public SortedSet<Update> getUpdateSet () {
		return uSet;
	}

	protected void addUpdate (Update u) {
		uSet.add(u);
	}

	protected void removeUpdate (Update u) {
		uSet.remove(u);
	}
}
