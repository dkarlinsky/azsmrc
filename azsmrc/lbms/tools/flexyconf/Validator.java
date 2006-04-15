package lbms.tools.flexyconf;

public class Validator {
	int type;

	public boolean validate (String value) {
		return false;
	}

	public static Validator getValidator (String rule, int type) throws InvalidRuleException, InvalidTypeException {
		switch(type) {
			case Entry.TYPE_STRING:
				return RegexValidator.getValidator(rule, type);
			case Entry.TYPE_DOUBLE:
			case Entry.TYPE_FLOAT:
			case Entry.TYPE_INT:
			case Entry.TYPE_LONG:
				return RangeValidator.getValidator(rule, type);
			default:
				throw new InvalidTypeException("There is no Validator for the selected Type: "+type);
		}
	}
}
