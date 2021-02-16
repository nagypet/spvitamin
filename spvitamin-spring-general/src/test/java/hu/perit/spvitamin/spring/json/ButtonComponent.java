package hu.perit.spvitamin.spring.json;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ButtonComponent extends AbstractComponent
{
	private final String label;

	public ButtonComponent()
	{
		super("Button");
		this.label = null;
	}

	public ButtonComponent(String label)
	{
		super("Button");
		this.label = label;
	}
}
