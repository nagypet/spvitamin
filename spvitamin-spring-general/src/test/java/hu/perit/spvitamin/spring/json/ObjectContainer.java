package hu.perit.spvitamin.spring.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class ObjectContainer implements JsonSerializable
{
	private final List<AbstractComponent> list = new ArrayList<>();

	public static ObjectContainer fromJson(String json) throws IOException
	{
		return JsonSerializable.fromJson(json, ObjectContainer.class);
	}
}
