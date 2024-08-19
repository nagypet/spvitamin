package hu.perit.spvitamin.spring.admin.serverparameter;

public class ServerParameterListBuilder
{
    public static ServerParameterList of(Object o)
    {
        return of(o, null);
    }

    public static ServerParameterList of(Object o, String namePrefix)
    {
        return new ServerParameterListImpl(o, namePrefix);
    }
}
