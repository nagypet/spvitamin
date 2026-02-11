package hu.perit.spvitamin.spring.resilientjobrunner.service.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public final class SimpleResilientJobParameter<T> extends ResilientJobParameter
{
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    private final T data;


    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> SimpleResilientJobParameter<T> fromJson(String json, Class<T> dataClass)
    {
        SimpleResilientJobParameter<T> parameter = ResilientJobParameter.fromJson(json, 0, SimpleResilientJobParameter.class);
        if (!dataClass.isAssignableFrom(parameter.getData().getClass()))
        {
            throw new ClassCastException("Cannot cast " + parameter.getData().getClass().getName() + " to " + dataClass.getName());
        }
        return parameter;
    }


    public static <T> SimpleResilientJobParameter<T> of(T input)
    {
        return new SimpleResilientJobParameter<>(input);
    }


    @Override
    public int getVersion()
    {
        return 0;
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
