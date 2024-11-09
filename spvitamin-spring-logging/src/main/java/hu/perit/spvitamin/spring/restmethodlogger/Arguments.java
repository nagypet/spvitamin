package hu.perit.spvitamin.spring.restmethodlogger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class Arguments
{
    @Setter(AccessLevel.NONE)
    private final Map<String, Object> argumentMap = new LinkedHashMap<>();

    public static Arguments create(List<String> argNames, Object... args)
    {
        Arguments retval = new Arguments();
        if (args == null || args.length == 0)
        {
            return retval;
        }

        for (int i = 0; i < argNames.size(); i++)
        {
            if (i < args.length)
            {
                retval.argumentMap.put(argNames.get(i), args[i]);
            }
            else
            {
                retval.argumentMap.put(argNames.get(i), null);
            }
        }
        return retval;
    }

    public Object get(String argName)
    {
        return argumentMap.get(argName);
    }

    public String getString(String argName)
    {
        Object object = argumentMap.get(argName);
        if (object != null)
        {
            return object.toString();
        }
        return null;
    }

    public boolean isEmpty()
    {
        return this.argumentMap.isEmpty();
    }
}
