/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * @author Peter Nagy
 */

public interface JsonSerializable
{

	default String toJson() throws JsonProcessingException
	{
		return JSonSerializer.toJson(this);
	}


	default String toYaml() throws JsonProcessingException
	{
		return JSonSerializer.toYaml(this);
	}


	static <T> T fromJson(String jsonString, Class<T> target) throws IOException
	{
		T obj = JSonSerializer.fromJson(jsonString, target);

		if (obj instanceof JsonSerializable jsonSerializable)
		{
			jsonSerializable.finalizeJsonDeserialization();
		}
		return obj;
	}


	static <T> T fromYaml(String jsonString, Class<T> target) throws IOException
	{
		T obj = JSonSerializer.fromYaml(jsonString, target);

		if (obj instanceof JsonSerializable jsonSerializable)
		{
			jsonSerializable.finalizeJsonDeserialization();
		}
		return obj;
	}


	default void finalizeJsonDeserialization()
	{
		// Override if you want to some post processing. E.g. if you have a member
		// containing a data type information
		// you may want to case the data member to have the real data type.
	}
}

//	Requirements:
//	- a default constructor 
//	- Getter methods or @JsonProperty annotation 
//	
//	class Example implements JsonSerializable {
//		@JsonProperty private final String name;
//		@JsonProperty private final int age;
//		public Example() { 
//			this.name = null; 
//			age = 0; 
//		} 
//	}
