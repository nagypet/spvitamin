package hu.perit.spvitamin.spring.rolemapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties
public class Role2PermissionMappingProperties {

	private Map<String, List<String>> rolemap = new HashMap<>();

}
