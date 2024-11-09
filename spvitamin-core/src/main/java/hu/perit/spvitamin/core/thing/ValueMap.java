package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@JsonSerialize(using = ValueMapSerializer.class)
public class ValueMap extends Thing
{
    @Setter(AccessLevel.NONE)
    private final Map<String, Thing> properties = new LinkedHashMap<>();


    @Override
    public void accept(String name, ThingVisitor visitor)
    {
        visitor.visit(name, this);
    }


    @Override
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }
}
