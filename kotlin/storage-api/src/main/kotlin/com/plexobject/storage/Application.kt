package com.plexobject.storage

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@ComponentScan("com.plexobject.storage")
@ServletComponentScan
@EnableJpaRepositories("com.plexobject.storage.repository")
@EntityScan("com.plexobject.storage.domain")
@SpringBootApplication
@EnableWebMvc
open class Application : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(Application::class.java)
            // app.setBannerMode(Banner.Mode.OFF);
            //SpringApplication.run(Application::class.java, *args)
            app.run(*args)
        }
    }
}