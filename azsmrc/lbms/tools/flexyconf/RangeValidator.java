package lbms.tools.flexyconf;

class RangeValidator extends Validator {
	private double dmin;
	private double dmax;
	private long  lmin;
	private long  lmax;

	private RangeValidator() {}

	public static RangeValidator getValidator (String rule, int type) throws InvalidRuleException, InvalidTypeException {
		if (rule.indexOf("..") == -1) throw new InvalidRuleException("Invalid Rule: "+rule);
		String[] parts = rule.split("\\.\\.");
		RangeValidator r = new RangeValidator();
		r.type = type;
		try {
			switch (type) {
			case Entry.TYPE_STRING:
				throw new InvalidTypeException ("Range Validator is not applicable for the Type String.");
			case Entry.TYPE_FLOAT:
			case Entry.TYPE_DOUBLE:
				if (parts.length==1) {
					r.dmin = Double.parseDouble(parts[0]);
					r.dmax = Double.MAX_VALUE;
				} else {
					if (parts[0].length()==0) {
						r.dmin = Double.MIN_VALUE;
					} else {
						r.dmin = Double.parseDouble(parts[0]);
					}
					r.dmax = Double.parseDouble(parts[1]);
				}
				if (r.lmin>=r.lmax) throw new InvalidRuleException("Invalid Rule: min ("+r.dmin+") is >= max ("+r.dmax+") "+rule);
				break;
			case Entry.TYPE_INT:
			case Entry.TYPE_LONG:
				if (parts.length==1) {
					r.lmin = Long.parseLong(parts[0]);
					r.lmax = Long.MAX_VALUE;
				} else {
					if (parts[0].length()==0) {
						r.lmin = Long.MIN_VALUE;
					} else {
						r.lmin = Long.parseLong(parts[0]);
					}
					r.lmax = Long.parseLong(parts[1]);
				}
				if (r.lmin>=r.lmax) throw new InvalidRuleException("Invalid Rule: min ("+r.lmin+") is >= max ("+r.lmax+") "+rule);
				break;
			default:
				throw new InvalidTypeException ("Range Validator is not applicable for the selected Type.");
			}
		} catch (NumberFormatException e) {
			throw new InvalidRuleException(e);
		}
		return r;
	}

	@Override
	public boolean validate(String value) {
		switch (type) {
		case Entry.TYPE_FLOAT:
			{
				double x = Double.parseDouble(value);
				if (x > Float.MAX_VALUE || x < Float.MIN_VALUE) return false;
			}
		case Entry.TYPE_DOUBLE:
			return validate(Double.parseDouble(value));
		case Entry.TYPE_INT:
			{
				long x = Long.parseLong(value);
				if (x > Integer.MAX_VALUE || x < Integer.MIN_VALUE) return false;
			}
		case Entry.TYPE_LONG:
			return validate(Long.parseLong(value));
		default:
			return false;
		}
	}

	public boolean validate (float value) {
		return validate ((double) value);
	}

	public boolean validate (double value) {
		if (dmin<=value && value<=dmax)
			return true;
		else
			return false;
	}

	public boolean validate (int value) {
		return validate ((long)value);
	}

	public boolean validate (long value) {
		if (lmin<=value && value<=lmax)
			return true;
		else
			return false;
	}
}
