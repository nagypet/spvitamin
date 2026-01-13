package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
class ParameterV2 extends ResilientJobParameter
{
    private Long docId;
    private Long fileSize;
    private UUID originalFileName;


    public static ParameterV2 of(Long docId, Long fileSize, UUID originalFileName)
    {
        ParameterV2 result = new ParameterV2();
        result.docId = docId;
        result.fileSize = fileSize;
        result.originalFileName = originalFileName;
        return result;
    }


    @Override
    public int getVersion()
    {
        return 2;
    }


    @Override
    protected Class<? extends ResilientJobParameter> getPreviousVersionClass()
    {
        return ParameterV1.class;
    }


    @Override
    protected ResilientJobParameter migrateFrom(ResilientJobParameter previous)
    {
        ParameterV1 v1 = (ParameterV1) previous;
        // Itt dől el az üzleti logika: pl. v1.fileName-ből nem tudunk UUID-t csinálni, marad null
        return ParameterV2.of(v1.getDocId(), v1.getFileSize(), null);
    }
}
