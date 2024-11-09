package hu.perit.spvitamin.core.thing;

public interface ThingVisitor
{
    void visit(String name, Value value);

    void visit(String name, ValueMap valueMap);

    void visit(String name, ValueList valueList);
}
