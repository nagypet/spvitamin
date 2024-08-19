package hu.perit.spvitamin.spring.rest.model;

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ServerSettingsResponse
{
    private Map<String, Set<ServerParameter>> serverParameters;
}
