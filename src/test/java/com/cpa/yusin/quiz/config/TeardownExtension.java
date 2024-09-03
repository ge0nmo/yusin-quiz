package com.cpa.yusin.quiz.config;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;


public class TeardownExtension implements BeforeEachCallback
{
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception
    {
        ApplicationContext context = SpringExtension.getApplicationContext(extensionContext);
        CleanDatabase.teardown(context);
    }

    public static void cleanDatabase(ApplicationContext context)
    {
        try{
            CleanDatabase.teardown(context);
        } catch (Exception e){

        }
    }
}
