package hu.perit.spvitamin.spring.json;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class TextComponent extends AbstractComponent
{
	private final String text;

	public TextComponent()
	{
		super("Text");
		this.text = null;
	}

	public TextComponent(String text)
	{
		super("Text");
		this.text = text;
	}
}
