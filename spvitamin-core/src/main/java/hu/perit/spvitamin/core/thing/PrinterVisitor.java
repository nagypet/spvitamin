package hu.perit.spvitamin.core.thing;

import hu.perit.spvitamin.core.typehelpers.LocalDateTimeUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
public class PrinterVisitor implements ThingVisitor
{
    public static final String PASSWORD = "password";

    @Builder
    @Getter
    public static class Options
    {
        @Builder.Default
        private boolean prettyPrint = false;
        @Builder.Default
        private boolean hidePasswords = true;
        @Builder.Default
        private boolean ignoreNulls = true;
        @Builder.Default
        private int maxStringLength = 100;
    }

    private final StringBuilder jsonBuilder = new StringBuilder();
    private int indentLevel = 0;
    private final Options options;

    protected void indent()
    {
        if (options.prettyPrint)
        {
            jsonBuilder.append("  ".repeat(indentLevel));
        }
    }

    protected void newLine()
    {
        if (options.prettyPrint)
        {
            jsonBuilder.append("\n");
        }
    }

    public String getJson()
    {
        return jsonBuilder.toString();
    }

    @Override
    public void visit(String name, Value value)
    {
        if (value.isEmpty())
        {
            jsonBuilder.append("null");
            return;
        }

        String convertedValue = convertProperty(name, value.getValue());
        String escaped = convertedValue
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r\n", "\\r\\n")
                .replace("\t", "\\t");
        jsonBuilder.append("\"").append(escaped).append("\"");
    }


    // To override for other special conversions
    protected String convertProperty(String name, Object value)
    {
        if (value == null)
        {
            return "null";
        }

        if (value instanceof byte[] bytes)
        {
            return formatByteArray(bytes);
        }
        else if (value instanceof XMLGregorianCalendar xmlGregorianCalendar)
        {
            return formatXmlGregorianCalendar(xmlGregorianCalendar);
        }
        else if (value instanceof InputStream inputStream)
        {
            return formatInputStream(inputStream);
        }
        else if (value instanceof Date date)
        {
            return LocalDateTimeUtils.format(date);
        }
        else if (value instanceof OffsetDateTime offsetDateTime)
        {
            return LocalDateTimeUtils.format(offsetDateTime);
        }

        // if this is a password
        if (options.hidePasswords && StringUtils.contains(name, PASSWORD))
        {
            return "*** [hidden]";
        }

        // if string is too long
        if (value instanceof String stringValue && stringValue.length() > options.getMaxStringLength())
        {
            return formatLongString(stringValue, options.getMaxStringLength());
        }

        return value.toString();
    }


    protected String formatByteArray(byte[] bytes)
    {
        return MessageFormat.format("byte[] of length: {0}", FileUtils.byteCountToDisplaySize(bytes.length));
    }


    protected String formatXmlGregorianCalendar(XMLGregorianCalendar xmlGregorianCalendar)
    {
        OffsetDateTime offsetDateTime = xmlGregorianCalendar.toGregorianCalendar().getTime().toInstant().atOffset(OffsetDateTime.now().getOffset());
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    protected String formatLongString(String longString, int maxLength)
    {
        return MessageFormat.format("String of size {0} beginning with: {1}", longString.length(), StringUtils.abbreviate(longString, maxLength));
    }


    protected String formatInputStream(InputStream inputStream)
    {
        try
        {
            return MessageFormat.format("{0} of size {1}", inputStream.getClass().getName(), inputStream.available());
        }
        catch (IOException e)
        {
            return MessageFormat.format("{0} of unknown size", inputStream.getClass().getName());
        }
    }


    @Override
    public void visit(String name, ValueList valueList)
    {
        jsonBuilder.append("[");
        newLine();
        indentLevel++;

        int elementCount = 0;
        for (Thing element : valueList.getElements())
        {
            indent();
            element.accept(name, this);

            elementCount++;
            if (elementCount < valueList.getElements().size())
            {
                jsonBuilder.append(",");
            }
            newLine();
        }

        indentLevel--;
        indent();
        jsonBuilder.append("]");
    }

    @Override
    public void visit(String name, ValueMap valueMap)
    {
        jsonBuilder.append("{");
        newLine();
        indentLevel++;

        int entryCount = 0;
        for (Map.Entry<String, Thing> entry : valueMap.getProperties().entrySet())
        {
            String key = entry.getKey();
            Thing property = entry.getValue();
            if (!(options.ignoreNulls && property.isEmpty()))
            {
                indent();
                jsonBuilder.append("\"").append(key).append("\":");

                property.accept(key, this);

                entryCount++;
                if (entryCount < valueMap.getProperties().size())
                {
                    jsonBuilder.append(",");
                }
                newLine();
            }
        }

        indentLevel--;
        indent();
        jsonBuilder.append("}");
    }
}
