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

package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonSerialize(using = ValueListSerializer.class)
@ToString
@EqualsAndHashCode(callSuper = true)
public class ValueList extends Thing
{
    private final List<Thing> elements = new ArrayList<>();

    protected ValueList(String name)
    {
        super(name);
    }

    @Override
    public void accept(ThingVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    boolean isEmpty()
    {
        return elements.isEmpty();
    }
}
