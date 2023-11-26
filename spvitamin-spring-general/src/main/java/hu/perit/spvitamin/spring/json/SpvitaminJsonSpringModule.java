package hu.perit.spvitamin.spring.json;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class SpvitaminJsonSpringModule extends SimpleModule
{
    public SpvitaminJsonSpringModule()
    {
        super.addSerializer(new CustomMultipartFileSerializer());
    }
}
