package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class BJobHelper
{
    public static String getBatchId(ProcessorType processorType, Long id)
    {
        String idText = id == null ? "" : " (" + id.toString() + ")";
        return MessageFormat.format("{0}{1}", processorType.getName(), idText);
    }
}
