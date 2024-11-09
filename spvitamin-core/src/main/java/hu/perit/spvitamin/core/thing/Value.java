package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ValueSerializer.class)
public class Value extends Thing
{
    private final Object value;

    public void accept(String name, ThingVisitor visitor)
    {
        visitor.visit(name, this);
    }

    @Override
    boolean isEmpty()
    {
        return value == null;
    }
}
