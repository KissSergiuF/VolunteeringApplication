package com.licenta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurație pentru servirea fișierelor statice (ex: poze de profil) din directorul local `uploads/`.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Adaugă un handler pentru resursele statice, mapând ruta /uploads/** la folderul local uploads/.
     *
     * @param registry registrul handlerelor de resurse
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
