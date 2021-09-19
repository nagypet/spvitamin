package hu.perit.spvitamin.core.exception;

/**
 * @author nagy_peter
 */
public class ApplicationRuntimeException extends RuntimeException implements ApplicationSpecificException {

	private static final long serialVersionUID = 5390293268764684183L;

	private final AbstractMessageType type;

	public ApplicationRuntimeException(AbstractMessageType type) {
		this(type, null);
	}

	public ApplicationRuntimeException(AbstractMessageType type, Throwable cause) {
		super(type.getMessage(), cause);
		this.type = type;
	}

	public ApplicationRuntimeException(ApplicationException ex) {
		super(ex.getMessage(), ex);
		this.type = ex.getType();
	}

	@Override
	public AbstractMessageType getType() {
		return type;
	}
}
