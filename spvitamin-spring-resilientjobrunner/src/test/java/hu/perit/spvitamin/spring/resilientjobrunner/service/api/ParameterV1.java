package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
class ParameterV1 extends ResilientJobParameter
{
    private Long docId;
    private Long fileSize;
    private String originalFileName;


    public static ParameterV1 of(Long docId, Long fileSize, String originalFileName)
    {
        ParameterV1 result = new ParameterV1();
        result.docId = docId;
        result.fileSize = fileSize;
        result.originalFileName = originalFileName;
        return result;
    }


    @Override
    public int getVersion()
    {
        return 1;
    }


    @Override
    protected Class<? extends ResilientJobParameter> getPreviousVersionClass()
    {
        return null;
    }


    @Override
    protected ResilientJobParameter migrateFrom(ResilientJobParameter previous)
    {
        return null;
    }
}
