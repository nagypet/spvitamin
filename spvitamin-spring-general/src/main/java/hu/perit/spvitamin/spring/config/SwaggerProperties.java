package hu.perit.spvitamin.spring.config;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "springfox.documentation")
@Slf4j
public class SwaggerProperties
{
    @NestedConfigurationProperty
    private SwaggerUiConfigurationProperties swaggerUi = new SwaggerUiConfigurationProperties();

    @NestedConfigurationProperty
    private SwaggerConfigurationProperties swagger = new SwaggerConfigurationProperties();

    @Setter
    public static class SwaggerUiConfigurationProperties
    {
        private String baseUrl;

        public String getBaseUrl()
        {
            String baseUrl = this.baseUrl;
            if (baseUrl == null)
            {
                baseUrl = "";
            }
            if (!baseUrl.isEmpty() && !baseUrl.startsWith("/"))
            {
                baseUrl = "/" + baseUrl;
            }
            return baseUrl;
        }
    }

    @Setter
    public static class SwaggerConfigurationProperties
    {
        @NestedConfigurationProperty
        private Swagger2Configuration v2 = new Swagger2Configuration();

        public Swagger2Configuration getV2()
        {
            return v2;
        }
    }

    @Setter
    public static class Swagger2Configuration
    {
        private String path;

        public String getPath()
        {
            String path = this.path;
            if (StringUtils.isBlank(path))
            {
                path = "/v2/api-docs";
            }
            if (!path.isEmpty() && !path.startsWith("/"))
            {
                path = "/" + path;
            }
            return path;
        }
    }
}
