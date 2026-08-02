package hu.perit.spvitamin.spring.security.xss;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
class XssRequestWrapper extends HttpServletRequestWrapper
{
    private static final Safelist SAFELIST = Safelist.relaxed().addAttributes(":all", "style");
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final byte[] sanitizedBody;
    private final boolean jsonRequest;


    XssRequestWrapper(HttpServletRequest request) throws IOException
    {
        super(request);
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("application/json"))
        {
            byte[] rawBody = request.getInputStream().readAllBytes();
            this.sanitizedBody = sanitizeJsonBody(rawBody);
            this.jsonRequest = true;
        }
        else
        {
            this.sanitizedBody = null;
            this.jsonRequest = false;
        }
    }


    private static byte[] sanitizeJsonBody(byte[] rawBody)
    {
        if (rawBody.length == 0)
        {
            return rawBody;
        }
        try
        {
            String json = new String(rawBody, StandardCharsets.UTF_8);
            JsonNode root = JSON_MAPPER.readTree(json);
            sanitizeNode(root);
            return JSON_MAPPER.writeValueAsBytes(root);
        }
        catch (JacksonException e)
        {
            log.warn("XSS sanitization: JSON parsing failed, passing original body: {}", e.getMessage());
            return rawBody;
        }
    }


    private static void sanitizeNode(JsonNode node)
    {
        if (node.isObject())
        {
            ObjectNode objectNode = (ObjectNode) node;
            for (String fieldName : new ArrayList<>(objectNode.propertyNames()))
            {
                JsonNode child = objectNode.get(fieldName);
                if (child.isString())
                {
                    objectNode.put(fieldName, Jsoup.clean(child.stringValue(), SAFELIST));
                }
                else
                {
                    sanitizeNode(child);
                }
            }
        }
        else if (node.isArray())
        {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++)
            {
                JsonNode child = arrayNode.get(i);
                if (child.isString())
                {
                    arrayNode.set(i, StringNode.valueOf(Jsoup.clean(child.stringValue(), SAFELIST)));
                }
                else
                {
                    sanitizeNode(child);
                }
            }
        }
    }


    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if (!jsonRequest)
        {
            return super.getInputStream();
        }
        return new CachedBodyServletInputStream(sanitizedBody);
    }


    @Override
    public BufferedReader getReader() throws IOException
    {
        if (!jsonRequest)
        {
            return super.getReader();
        }
        Charset charset = getCharacterEncoding() != null
                ? Charset.forName(getCharacterEncoding())
                : StandardCharsets.UTF_8;
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }


    @Override
    public String getParameter(String name)
    {
        String value = super.getParameter(name);
        return value != null ? Jsoup.clean(value, SAFELIST) : null;
    }


    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = super.getParameterValues(name);
        if (values == null)
        {
            return values;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++)
        {
            sanitized[i] = values[i] != null ? Jsoup.clean(values[i], SAFELIST) : null;
        }
        return sanitized;
    }
}
