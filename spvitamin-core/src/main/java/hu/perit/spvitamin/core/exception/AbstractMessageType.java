package hu.perit.spvitamin.core.exception;

/**
 * @author nagy_peter
 */
public interface AbstractMessageType {

	AbstractMessageType params(Object... params);
	
	int getHttpStatusCode();
	
	LogLevel getLevel();
	
	String getMessage();
	
	String name();
}
