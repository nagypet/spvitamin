package hu.perit.spvitamin.core.thing;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = ValueListSerializer.class)
public class ValueList extends Thing
{
    private final List<Thing> elements = new ArrayList<>();

    @Override
    public void accept(String name, ThingVisitor visitor)
    {
        visitor.visit(name, this);
    }

    @Override
    boolean isEmpty()
    {
        return elements.isEmpty();
    }
}
