package hu.perit.spvitamin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Case
{
    public static String toLower(String input)
    {
        return input == null ? null : input.toLowerCase().strip();
    }
}
