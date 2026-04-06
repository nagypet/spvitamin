package hu.perit.spvitamin.json;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * A custom implementation of Jackson's {@link JacksonAnnotationIntrospector}
 * tailored for handling property names with an underscore prefix.
 * This class modifies the implicit property naming strategy to recognize
 * and preserve underscore-prefixed property names or derive such names
 * from method names following specific naming conventions.
 * <p>
 * The introspector processes field names, getter names, and setter names:
 * - Field names that start with an underscore (`_`) remain unchanged.
 * - Getter methods with names in the form `get_<name>` are converted to `_name`.
 * - Setter methods with names in the form `set_<name>` are converted to `_name`.
 * <p>
 * If no underscore-based naming convention matches, the implementation falls
 * back to the default behavior of the superclass.
 */
public class UnderscorePrefixIntrospector extends JacksonAnnotationIntrospector
{

    @Override
    public String findImplicitPropertyName(MapperConfig<?> config, AnnotatedMember member)
    {
        String name = member.getName();
        // field name: _links -> _links (keep it)
        if (name.startsWith("_"))
        {
            return name;
        }
        // getter name: get_links -> _links
        if (name.startsWith("get_") && name.length() > 4)
        {
            return "_" + name.substring(4);
        }
        // setter name: set_links -> _links
        if (name.startsWith("set_") && name.length() > 4)
        {
            return "_" + name.substring(4);
        }
        return super.findImplicitPropertyName(config, member);
    }
}
