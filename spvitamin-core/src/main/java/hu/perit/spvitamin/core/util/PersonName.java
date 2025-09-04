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

package hu.perit.spvitamin.core.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.StringJoiner;

/**
 * Represents a person's name with support for different name orders (Western and Eastern).
 * The class handles parsing full names into their components: family name (surname/last name),
 * given name (first name), and additional given names (middle names).
 */

@Data
public class PersonName
{
    public enum NameOrder
    {
        WESTERN,
        EASTERN;
    }


    // Tracks the original name order used when parsing
    private final NameOrder nameOrder;
    // Family name (surname/last name)
    private String familyName;
    // Given name (first name)
    private String givenName;
    // Additional given names (middle names)
    private String additionalGivenNames;


    public static PersonName from(String fullName)
    {
        return from(fullName, NameOrder.WESTERN);
    }


    public static PersonName from(String fullName, NameOrder nameOrder)
    {
        PersonName personName = new PersonName(nameOrder);

        if (StringUtils.isBlank(fullName))
        {
            return personName;
        }

        String[] nameParts = fullName.trim().split("\\s+");

        if (nameParts.length == 1)
        {
            // Only one name part, assume it's the given name
            personName.setGivenName(nameParts[0]);
        }
        else if (nameParts.length == 2)
        {
            if (nameOrder == NameOrder.WESTERN)
            {
                // Two name parts in Western order: given name, family name
                personName.setGivenName(nameParts[0]);
                personName.setFamilyName(nameParts[1]);
            }
            else // EASTERN
            {
                // Two name parts in Eastern order: family name, given name
                personName.setFamilyName(nameParts[0]);
                personName.setGivenName(nameParts[1]);
            }
        }
        else
        {
            if (nameOrder == NameOrder.WESTERN)
            {
                // More than two name parts in Western order: given name, additional given name(s), family name
                personName.setGivenName(nameParts[0]);
                personName.setFamilyName(nameParts[nameParts.length - 1]);

                // Combine all middle parts into additional given names
                StringBuilder additionalNames = new StringBuilder();
                for (int i = 1; i < nameParts.length - 1; i++)
                {
                    if (i > 1)
                    {
                        additionalNames.append(" ");
                    }
                    additionalNames.append(nameParts[i]);
                }
                personName.setAdditionalGivenNames(additionalNames.toString());
            }
            else // EASTERN
            {
                // More than two name parts in Eastern order: family name, given name, additional given names
                personName.setFamilyName(nameParts[0]);
                personName.setGivenName(nameParts[1]);

                // Combine all remaining parts into additional given names
                StringBuilder additionalNames = new StringBuilder();
                for (int i = 2; i < nameParts.length; i++)
                {
                    if (i > 2)
                    {
                        additionalNames.append(" ");
                    }
                    additionalNames.append(nameParts[i]);
                }
                personName.setAdditionalGivenNames(additionalNames.toString());
            }
        }

        return personName;
    }


    public String getName()
    {
        return getName(this.nameOrder);
    }


    public String getName(NameOrder expectedNameOrder)
    {
        if (expectedNameOrder == NameOrder.WESTERN)
        {
            // Western order: given name, additional given names, family name
            StringJoiner nameJoiner = new StringJoiner(" ");

            addIfNotBlank(nameJoiner, givenName);
            addIfNotBlank(nameJoiner, additionalGivenNames);
            addIfNotBlank(nameJoiner, familyName);

            return nameJoiner.toString();
        }
        else // EASTERN
        {
            // Eastern order: family name, given name, additional given names
            if (StringUtils.isNotBlank(familyName) && (StringUtils.isNotBlank(givenName) || StringUtils.isNotBlank(additionalGivenNames)))
            {
                // Add comma only if the original name order was Western and we're displaying it in Eastern order
                boolean addComma = this.nameOrder == NameOrder.WESTERN;

                StringBuilder nameBuilder = new StringBuilder();
                nameBuilder.append(familyName);

                if (addComma)
                {
                    nameBuilder.append(", ");
                }
                else
                {
                    nameBuilder.append(" ");
                }

                if (StringUtils.isNotBlank(givenName))
                {
                    nameBuilder.append(givenName);
                }

                if (StringUtils.isNotBlank(additionalGivenNames))
                {
                    if (StringUtils.isNotBlank(givenName))
                    {
                        nameBuilder.append(" ");
                    }
                    nameBuilder.append(additionalGivenNames);
                }

                return nameBuilder.toString();
            }
            else
            {
                // If only one part is present, no comma is needed
                StringJoiner nameJoiner = new StringJoiner(" ");

                addIfNotBlank(nameJoiner, familyName);
                addIfNotBlank(nameJoiner, givenName);
                addIfNotBlank(nameJoiner, additionalGivenNames);

                return nameJoiner.toString();
            }
        }
    }


    /**
     * Adds a string to the StringJoiner if it's not blank.
     *
     * @param joiner the StringJoiner to add to
     * @param value  the string value to add if not blank
     */
    private void addIfNotBlank(StringJoiner joiner, String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            joiner.add(value);
        }
    }
}
