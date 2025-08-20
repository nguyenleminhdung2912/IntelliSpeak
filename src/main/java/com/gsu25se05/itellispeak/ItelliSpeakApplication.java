package com.gsu25se05.itellispeak;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.reflections.Reflections;
import org.springframework.stereotype.Service;
import java.lang.reflect.Method;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(info = @Info(title = "ItelliSpeak API", version = "1.0", description = "Information"))
@SecurityScheme(name = "api", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class ItelliSpeakApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItelliSpeakApplication.class, args);


        Reflections reflections = new Reflections("com.gsu25se05.itellispeak.service");
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);

        for (Class<?> service : services) {
            System.out.println(service.getSimpleName());

            // Lấy class gốc nếu bị Spring proxy (CGLIB)
            if (service.getName().contains("$$")) {
                service = service.getSuperclass();
            }

            for (Method method : service.getDeclaredMethods()) {
                // Lọc lambda & bridge method
                if (method.isSynthetic() || method.isBridge()) continue;

                System.out.print("  + " + method.getName() + "(");
                Class<?>[] params = method.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    System.out.print(params[i].getSimpleName());
                    if (i < params.length - 1) System.out.print(", ");
                }
                System.out.println(")");
            }
        }
    }

}
