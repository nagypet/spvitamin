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

package hu.perit.spvitamin.spring.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.perit.spvitamin.spring.json.JSonSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ObjectLogger
{
    public static String toString(Object object)
    {
        try
        {
            return JSonSerializer.toJson(object);
        }
        catch (JsonProcessingException e)
        {
            // Causes illegal reflective access, but good enough as fallback
            log.warn(e.toString());
            return ReflectionToStringBuilder.toString(object, new RecursiveJSonToStringStyle());
        }
        catch (RuntimeException e)
        {
            // last chance
            return (object != null) ? object.toString() : "null";
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // RecursiveJSonToStringStyle
    //------------------------------------------------------------------------------------------------------------------
    public static class RecursiveJSonToStringStyle extends RecursiveToStringStyle
    {
        private static final String FIELD_NAME_QUOTE = "\"";


        RecursiveJSonToStringStyle()
        {
            super();

            this.setUseClassName(false);
            this.setUseIdentityHashCode(false);

            this.setContentStart("{");
            this.setContentEnd("}");

            this.setArrayStart("[");
            this.setArrayEnd("]");

            this.setFieldSeparator(",");
            this.setFieldNameValueSeparator(":");

            this.setNullText("null");

            this.setSummaryObjectStartText("\"<");
            this.setSummaryObjectEndText(">\"");

            this.setSizeStartText("\"<size=");
            this.setSizeEndText(">\"");
        }


        @Override
        protected void appendDetail(final StringBuffer buffer, final String fieldName, final char value)
        {
            appendValueAsString(buffer, String.valueOf(value));
        }


        @Override
        public void appendDetail(final StringBuffer buffer, final String fieldName, final Object value)
        {

            if (value == null)
            {
                appendNullText(buffer, fieldName);
                return;
            }

            if (value instanceof String || value instanceof Character)
            {
                appendValueAsString(buffer, value.toString());
                return;
            }

            if (value instanceof Number || value instanceof Boolean)
            {
                buffer.append(value);
                return;
            }

            super.appendDetail(buffer, fieldName, value);
        }


        /**
         * Appends the given String enclosed in double-quotes to the given StringBuffer.
         *
         * @param buffer the StringBuffer to append the value to.
         * @param value  the value to append.
         */
        private void appendValueAsString(final StringBuffer buffer, final String value)
        {
            buffer.append('"').append(StringEscapeUtils.escapeJson(value)).append('"');
        }


        @Override
        protected void appendFieldStart(final StringBuffer buffer, final String fieldName)
        {

            if (fieldName == null)
            {
                throw new UnsupportedOperationException("Field names are mandatory when using JsonToStringStyle");
            }

            super.appendFieldStart(buffer, FIELD_NAME_QUOTE + StringEscapeUtils.escapeJson(fieldName) + FIELD_NAME_QUOTE);
        }


        /**
         * <p>
         * Ensure {@code Singleton} after serialization.
         * </p>
         *
         * @return the singleton
         */
        private Object readResolve()
        {
            return JSON_STYLE;
        }
    }
}
