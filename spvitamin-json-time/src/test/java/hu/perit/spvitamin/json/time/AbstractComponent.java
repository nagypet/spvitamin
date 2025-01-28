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

package hu.perit.spvitamin.json.time;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Peter Nagy
 */

// Version 1: using the class name encoded as a property of the class
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
//Output:
//	{
//	   "list":[
//	      {
//	         "class":"hu.perit.spvitamin.spring.json.TextComponent",
//	         "type":"Text",
//	         "text":"my text"
//	      },
//	      {
//	         "class":"hu.perit.spvitamin.spring.json.ButtonComponent",
//	         "type":"Button",
//	         "label":"my label"
//	      }
//	   ]
//	} 

//Version 2: using the class name encoded as a property of the class
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
//Output:
//	{
//	   "list":[
//	      {
//	         "class":".TextComponent",
//	         "type":"Text",
//	         "text":"my text"
//	      },
//	      {
//	         "class":".ButtonComponent",
//	         "type":"Button",
//	         "label":"my label"
//	      }
//	   ]
//	}

// Version 3 :using an existing property as the type info
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
//@JsonSubTypes({ //
//		@Type(value = TextComponent.class, name = "Text"), //
//		@Type(value = ButtonComponent.class, name = "Button") //
//})
//Output:
//	{
//		 "list":[
//	      {
//	         "type":"Text",
//	         "text":"my text"
//	      },
//	      {
//	         "type":"Button",
//	         "label":"my label"
//	      }
//	   ]
//	}

@Getter
@EqualsAndHashCode
public abstract class AbstractComponent
{
	private final String type;

	public AbstractComponent(String componentType)
	{
		super();
		this.type = componentType;
	}
}
