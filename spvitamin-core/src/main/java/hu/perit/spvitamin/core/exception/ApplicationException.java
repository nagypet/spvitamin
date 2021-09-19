package hu.perit.spvitamin.core.exception;

import lombok.Getter;


/**
 * @author nagy_peter
 */
@Getter
public class ApplicationException extends Exception implements ApplicationSpecificException {

	private static final long serialVersionUID = -4383847341048335399L;

	private final AbstractMessageType type;

	public ApplicationException(AbstractMessageType type) {
		this(type, null);
	}

	public ApplicationException(AbstractMessageType type, Throwable cause) {
		super(type.getMessage(), cause);
		this.type = type;
	}

	@Override
	public AbstractMessageType getType() {
		return type;
	}
}
