package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinSecurityConfigGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(ctx, dataTypeResolver) {
    private val formatter = Formatter()

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "kotlin",
        getPackage().replace(".", separator),
        this.getClassName() + ".kt"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            appendLine("package ${getPackage()}")
            append(
                """
            import com.fasterxml.jackson.databind.ObjectMapper
            import jakarta.servlet.FilterChain
            import jakarta.servlet.ServletException
            import jakarta.servlet.http.HttpServletRequest
            import jakarta.servlet.http.HttpServletResponse
            import org.springframework.context.annotation.Bean
            import org.springframework.context.annotation.Configuration
            import org.springframework.context.annotation.Primary
            import org.springframework.http.HttpMethod
            import org.springframework.http.HttpStatus
            import org.springframework.http.MediaType
            import org.springframework.lang.NonNull
            import org.springframework.security.authentication.AuthenticationManager
            import org.springframework.security.authentication.ProviderManager
            import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            import org.springframework.security.authentication.dao.DaoAuthenticationProvider
            import org.springframework.security.config.annotation.web.builders.HttpSecurity
            import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
            import org.springframework.security.config.http.SessionCreationPolicy
            import org.springframework.security.core.Authentication
            import org.springframework.security.core.AuthenticationException
            import org.springframework.security.core.context.SecurityContextHolder
            import org.springframework.security.core.userdetails.UserDetailsService
            import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
            import org.springframework.security.crypto.password.PasswordEncoder
            import org.springframework.security.web.SecurityFilterChain
            import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
            import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
            import org.springframework.web.filter.OncePerRequestFilter
            import org.springframework.web.server.ResponseStatusException
            import java.io.IOException
            import java.util.*

            @Configuration
            @EnableWebSecurity
            open class SecurityConfig {
                @Bean
                @Throws(Exception::class)
                open fun securityFilterChain(
                    http: HttpSecurity,
                    objectMapper: ObjectMapper,
                    jwtProvider: JwtProvider
                ): SecurityFilterChain {
                    return http.csrf()
                        .disable()
                        .cors()
                        .and()
                        .authorizeHttpRequests()
                        .requestMatchers("/**")
                        .permitAll()
                        .requestMatchers("/login")
                        .permitAll()
                        .and()
                        .addFilterAt(
                            jwtAuthenticationFilter(objectMapper, jwtProvider),
                            BasicAuthenticationFilter::class.java
                        )
                        .addFilterAt(
                            jwtAuthorizationFilter(jwtProvider),
                            BasicAuthenticationFilter::class.java
                        )
                        .sessionManagement()
                        .sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                        )
                        .and()
                        .build()
                }

                @Bean
                @Primary
                open fun authenticationManager(
                    userDetailsService: Optional<UserDetailsService?>
                ): AuthenticationManager {
                    val authenticationProvider = DaoAuthenticationProvider()
                    userDetailsService.ifPresent { userDetailsService: UserDetailsService? ->
                        authenticationProvider.setUserDetailsService(
                            userDetailsService
                        )
                    }
                    authenticationProvider.setPasswordEncoder(passwordEncoder())
                    return ProviderManager(authenticationProvider)
                }

                @Bean
                open fun passwordEncoder(): PasswordEncoder {
                    return BCryptPasswordEncoder()
                }

                fun jwtAuthenticationFilter(
                    objectMapper: ObjectMapper, jwtProvider: JwtProvider
                ): UsernamePasswordAuthenticationFilter {
                    return object : UsernamePasswordAuthenticationFilter() {
                        @Throws(AuthenticationException::class)
                        override fun attemptAuthentication(
                            request: HttpServletRequest,
                            response: HttpServletResponse
                        ): Authentication {
                            // We can possibly handle form requests as well but for now we stick to JSON.
                            if (request
                                    .contentType != MediaType.APPLICATION_JSON_VALUE
                            ) throw ResponseStatusException(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "Content-Type must be application/json"
                            )
                            if (request.method != HttpMethod.POST.name()) throw ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                String.format(
                                    "404 %s %s",
                                    request.method,
                                    request.requestURI
                                )
                            )
                            return try {
                                val authenticationRequest = objectMapper.readValue(
                                    request.inputStream,
                                    JwtAuthenticationRequest::class.java
                                )
                                val token = UsernamePasswordAuthenticationToken
                                    .unauthenticated(
                                        authenticationRequest.username,
                                        authenticationRequest.password
                                    )
                                authenticationManager.authenticate(token)
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            }
                        }

                        @Throws(IOException::class, ServletException::class)
                        override fun successfulAuthentication(
                            request: HttpServletRequest,
                            response: HttpServletResponse,
                            chain: FilterChain,
                            authResult: Authentication
                        ) {
                            val context = SecurityContextHolder.getContext()
                            context.authentication = authResult
                            val authenticationResponse = JwtAuthenticationResponse(
                                jwtProvider.generateToken(authResult)
                            )
                            objectMapper.writeValue(response.writer, authenticationResponse)
                            response.contentType = MediaType.APPLICATION_JSON_VALUE
                            response.status = HttpStatus.OK.value()
                            chain.doFilter(request, response)
                        }
                    }
                }

                fun jwtAuthorizationFilter(
                    jwtProvider: JwtProvider
                ): OncePerRequestFilter {
                    return object : OncePerRequestFilter() {
                        @Throws(IOException::class, ServletException::class)
                        override fun doFilterInternal(
                            @NonNull request: HttpServletRequest,
                            @NonNull response: HttpServletResponse,
                            @NonNull filterChain: FilterChain
                        ) {
                            val context = SecurityContextHolder.getContext()
                            jwtProvider.getAuthentication(request)
                                .ifPresent { authentication: Authentication? ->
                                    context.authentication = authentication
                                }
                            filterChain.doFilter(request, response)
                        }
                    }
                }
            }
                """.trimIndent()
            )
        }
    )

    override fun getClassName(): String = "SecurityConfig"

    override fun getPackage(): String = ctx.getPackageName("security")
}
