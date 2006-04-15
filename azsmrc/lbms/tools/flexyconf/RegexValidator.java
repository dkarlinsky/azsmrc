package lbms.tools.flexyconf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator extends Validator {
	Pattern p;

	private RegexValidator () {}

	public static RegexValidator getValidator (String rule, int type) throws InvalidRuleException, InvalidTypeException {
		if (type!= Entry.TYPE_STRING) throw new InvalidTypeException ("Regex Validator is not applicable for the selected Type.");
		RegexValidator r = new RegexValidator();
		r.type = type;
		try {
			r.p = Pattern.compile(rule);
		} catch (PatternSyntaxException e) {
			throw new InvalidRuleException(e.getMessage());
		}
		return r;
	}

	@Override
	public boolean validate(String value) {
		Matcher m = p.matcher(value);
		if (m.find()) return true;
		else return false;
	}
}
