package hu.perit.spvitamin.spring.feignclients.cookie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CookieJar implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * cookie name -> cookie value
     */
    private Map<String, String> cookies = new LinkedHashMap<>();
}
