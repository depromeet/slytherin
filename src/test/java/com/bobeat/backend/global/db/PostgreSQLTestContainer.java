package com.bobeat.backend.global.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ExtendWith(PostgreSQLContainerExtension.class)
@ContextConfiguration(initializers = PostgreSQLContainerExtension.Initializer.class)
public @interface PostgreSQLTestContainer {
}