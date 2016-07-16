package lbms.tools.flexyconf;

public class InvalidRuleException extends Exception {

	public InvalidRuleException() {
		super();
	}

	public InvalidRuleException(String message) {
		super(message);
	}

	public InvalidRuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRuleException(Throwable cause) {
		super(cause);
	}
}
