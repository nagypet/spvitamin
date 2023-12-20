package hu.perit.spvitamin.spring.data.mapper;

import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface ConnectionParamMapper
{
    void copy(DatasourceProperties source, @MappingTarget DatasourceProperties target);
}
