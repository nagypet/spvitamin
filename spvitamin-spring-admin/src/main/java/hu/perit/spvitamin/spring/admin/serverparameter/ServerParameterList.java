package hu.perit.spvitamin.spring.admin.serverparameter;

import java.util.Map;
import java.util.Set;

public interface ServerParameterList
{
    void add(String group, ServerParameter serverParameter);

    void add(ServerParameterList serverParameterList);

    void add(String group, ServerParameterList serverParameterList);

    Map<String, Set<ServerParameter>> getParameters();
}
