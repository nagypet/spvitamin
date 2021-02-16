package hu.perit.spvitamin.spring.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlTest
{
	@AllArgsConstructor
	@ToString
	@EqualsAndHashCode
	@Getter
	static class Example implements JsonSerializable
	{
		private final String name;
		private final int age;
		private final Date date;
		private final LocalDate localDate;
		private final LocalDateTime localDateTime;

		public Example()
		{
			this.name = null;
			age = 0;
			date = null;
			localDate = null;
			localDateTime = null;
		}


		@Override
		public void finalizeJsonDeserialization()
		{
			log.debug("finalizeJsonDeserialization() called!");
		}
	}

	@Test
	void toYamlRoundtrip() throws IOException
	{
		Calendar cal = Calendar.getInstance();
		cal.set(2020, 4, 1, 10, 0, 0);
		cal.set(Calendar.MILLISECOND, 100);
		Example originalObject = new Example("Nagy", 55, cal.getTime(), LocalDate.of(2020, 5, 11), LocalDateTime.of(2020, 5, 11, 10, 10, 10));

		String yamlString = originalObject.toYaml();
		log.debug(yamlString);
		Example decodedObject = JsonSerializable.fromYaml(yamlString, Example.class);
		log.debug("original: " + originalObject.toString());
		log.debug("decoded:  " + decodedObject.toString());
		Assertions.assertEquals(originalObject, decodedObject);
	}
}
