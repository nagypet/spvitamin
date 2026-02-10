package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.perit.spvitamin.json.JSonSerializer;
import lombok.SneakyThrows;

public abstract class ResilientJobParameter
{
    @SneakyThrows
    public String toJson()
    {
        return JSonSerializer.toJson(this);
    }


    @JsonIgnore
    public abstract int getVersion();


    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T extends ResilientJobParameter> T fromJson(String json, int jsonVersion, Class<T> targetClass)
    {
        T instance = targetClass.getDeclaredConstructor().newInstance();

        if (jsonVersion == instance.getVersion())
        {
            return JSonSerializer.fromJson(json, targetClass);
        }

        if (jsonVersion > instance.getVersion())
        {
            throw new IllegalArgumentException(String.format("Cannot downgrade parameter from version %d to %d", jsonVersion, instance.getVersion()));
        }

        // Migráció: megkeressük az előző verziót
        Class<? extends ResilientJobParameter> previousClass = instance.getPreviousVersionClass();
        if (previousClass == null)
        {
            throw new IllegalStateException("Missing migration path for version " + instance.getVersion());
        }

        // Rekurzívan deszerializáljuk az előző verziót
        ResilientJobParameter previousVersionObject = fromJson(json, jsonVersion, (Class<ResilientJobParameter>) previousClass);

        // Majd migráljuk az aktuálisra
        return (T) instance.migrateFrom(previousVersionObject);
    }


    protected abstract Class<? extends ResilientJobParameter> getPreviousVersionClass();

    protected abstract ResilientJobParameter migrateFrom(ResilientJobParameter previous);
}
