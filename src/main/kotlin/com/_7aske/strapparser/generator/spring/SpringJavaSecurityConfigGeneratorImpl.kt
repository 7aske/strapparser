package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.java.JavaClassGenerator
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

const val CONTEXT_PACKAGE = "org.springframework.context.annotation"
const val SECURITY_PACKAGE = "org.springframework.security"

class SpringJavaSecurityConfigGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : JavaClassGenerator(ctx, dataTypeResolver) {
    private val formatter = Formatter()

    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "java",
        getPackage().replace(".", separator),
        this.getClassName() + ".java"
    )

    override fun generate(): String = formatter.formatSource(
        buildString {
            append("package ${getPackage()};")
            append("@org.springframework.context.annotation.Configuration\n")
            append("@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity\n")
            append("public class ${getClassName()} {")
            append("@$CONTEXT_PACKAGE.Bean\n")
            append("public $SECURITY_PACKAGE.web.SecurityFilterChain securityFilterChain(")
            append("$SECURITY_PACKAGE.config.annotation.web.builders.HttpSecurity http")
            append(", com.fasterxml.jackson.databind.ObjectMapper objectMapper")
            append(", ${getPackage()}.JwtProvider jwtProvider")
            append(") throws Exception {")
            append(
                "return http" +
                    ".csrf().disable().cors().and()" +
                    ".authorizeHttpRequests()" +
                    ".requestMatchers(\"/**\").permitAll()" +
                    ".requestMatchers(\"/login\").permitAll()" +
                    ".and()" +
                    ".addFilterAt(jwtAuthenticationFilter(objectMapper, jwtProvider), " +
                    "$SECURITY_PACKAGE.web.authentication.www.BasicAuthenticationFilter.class)" +
                    ".addFilterAt(jwtAuthorizationFilter(jwtProvider), " +
                    "$SECURITY_PACKAGE.web.authentication.www.BasicAuthenticationFilter.class)" +
                    ".sessionManagement().sessionCreationPolicy" +
                    "($SECURITY_PACKAGE.config.http.SessionCreationPolicy.STATELESS)" +
                    ".and()" +
                    ".build();"
            )
            append("}")

            append(
                """
                        @$CONTEXT_PACKAGE.Bean
                        @org.springframework.context.annotation.Primary
                        public org.springframework.security.authentication.AuthenticationManager authenticationManager(java.util.Optional<org.springframework.security.core.userdetails.UserDetailsService> userDetailsService) {
                            org.springframework.security.authentication.dao.DaoAuthenticationProvider authenticationProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
                            userDetailsService.ifPresent(authenticationProvider::setUserDetailsService);
                            authenticationProvider.setPasswordEncoder(passwordEncoder());
                            return new org.springframework.security.authentication.ProviderManager(authenticationProvider);
                        }

                """.trimIndent()
            )

            append(
                """
                        @$CONTEXT_PACKAGE.Bean
                        public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
                            return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                        }

                """.trimIndent()
            )
            append(generateAuthenticationFilterBean())
            append(generateAuthorizationFilterBean())
            append("}")
        }
    )

    private fun generateAuthorizationFilterBean(): String = buildString {
        append(
            """
                    public org.springframework.web.filter.OncePerRequestFilter jwtAuthorizationFilter(${getPackage()}.JwtProvider jwtProvider){
                    return new  org.springframework.web.filter.OncePerRequestFilter(){
                    @Override
                    protected void doFilterInternal(@org.springframework.lang.NonNull jakarta.servlet.http.HttpServletRequest request, @org.springframework.lang.NonNull jakarta.servlet.http.HttpServletResponse response, @org.springframework.lang.NonNull jakarta.servlet.FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
                        $SECURITY_PACKAGE.core.context.SecurityContext context = $SECURITY_PACKAGE.core.context.SecurityContextHolder.getContext();
                        jwtProvider.getAuthentication(request)
                                .ifPresent(context::setAuthentication);
                        filterChain.doFilter(request, response);
                    }
                    };
                    }
            """.trimIndent()
        )
    }

    private fun generateAuthenticationFilterBean(): String = buildString {
        append(
            """
                public $SECURITY_PACKAGE.web.authentication.UsernamePasswordAuthenticationFilter jwtAuthenticationFilter(com.fasterxml.jackson.databind.ObjectMapper objectMapper, JwtProvider jwtProvider) {
                	return new $SECURITY_PACKAGE.web.authentication.UsernamePasswordAuthenticationFilter() {

                		@Override
                		public $SECURITY_PACKAGE.core.Authentication attemptAuthentication(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws $SECURITY_PACKAGE.core.AuthenticationException {
                			// We can possibly handle form requests as well but for now we stick to JSON.
                			if (!request.getContentType().equals(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
                				throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json");
                			if (!request.getMethod().equals(org.springframework.http.HttpMethod.POST.name()))
                				throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, String.format("404 %s %s", request.getMethod(), request.getRequestURI()));

                			try {
                				${getPackage()}.JwtAuthenticationRequest authenticationRequest = objectMapper.readValue(request.getInputStream(), ${getPackage()}.JwtAuthenticationRequest.class);
                				$SECURITY_PACKAGE.authentication.UsernamePasswordAuthenticationToken token = $SECURITY_PACKAGE.authentication.UsernamePasswordAuthenticationToken.unauthenticated(
                						authenticationRequest.getUsername(),
                						authenticationRequest.getPassword());
                				return getAuthenticationManager().authenticate(token);
                			} catch (java.io.IOException e) {
                				throw new RuntimeException(e);
                			}
                		}

                		@Override
                		protected void successfulAuthentication(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain, $SECURITY_PACKAGE.core.Authentication authResult) throws java.io.IOException, jakarta.servlet.ServletException {
                			$SECURITY_PACKAGE.core.context.SecurityContext context = $SECURITY_PACKAGE.core.context.SecurityContextHolder.getContext();
                			context.setAuthentication(authResult);

                			${getPackage()}.JwtAuthenticationResponse authenticationResponse =
                					new ${getPackage()}.JwtAuthenticationResponse(jwtProvider.generateToken(authResult));
                			objectMapper.writeValue(response.getWriter(), authenticationResponse);

                			response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                			response.setStatus(org.springframework.http.HttpStatus.OK.value());

                			chain.doFilter(request, response);
                		}
                	};
                }
            """.trimIndent()
        )
    }

    override fun getClassName(): String = "SecurityConfig"

    override fun getPackage(): String = ctx.getPackageName("security")
}
