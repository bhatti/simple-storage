package com.plexobject.storage.security


import com.coxautodev.graphql.tools.SchemaParser
import com.google.common.base.Preconditions
import com.plexobject.storage.graphql.Query
import com.plexobject.storage.graphql.Scalars
import com.plexobject.storage.api.*
import com.plexobject.storage.graphql.*
import graphql.schema.GraphQLSchema
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
open class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Value("\${security.jwtSecret:00000000-0000-0000-0000-000000000000}")
    private val jwtSecret: String = "00000000-0000-0000-0000-000000000000"
    @Value("\${security.jwtAlgorithm:HS512}")
    private val jwtAlgorithm: String = "HS512"
    @Value("\${security.expiresInMinutes:6000000}")
    private val expiresInMinutes: Long = 6000000

    @Bean
    open fun getTokenAuthenticationService(): TokenAuthenticationService {
        return TokenAuthenticationService(jwtSecret, jwtAlgorithm, expiresInMinutes)
    }

    @Throws(Exception::class)
    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity.authorizeRequests().antMatchers("/").permitAll().antMatchers("/metrics/**").permitAll()
                .antMatchers("/api/**").authenticated()
        httpSecurity.antMatcher("/api/**").addFilterBefore(JWTAuthenticationFilter(getTokenAuthenticationService()),
                UsernamePasswordAuthenticationFilter::class.java)
        httpSecurity.csrf().disable()
        httpSecurity.headers().frameOptions().sameOrigin()
    }
}