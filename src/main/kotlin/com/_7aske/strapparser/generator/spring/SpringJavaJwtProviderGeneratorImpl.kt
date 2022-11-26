package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.extensions.uncapitalize
import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com.google.googlejavaformat.java.Formatter
import java.nio.file.Path
import java.nio.file.Paths

class SpringJavaJwtProviderGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(
    ctx, dataTypeResolver
) {
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
            append("@org.springframework.stereotype.Service\n")
            append("public class ${getClassName()} {")

            append(
                """
                private static final String SECRET = "secret";
                private static final String TOKEN_PREFIX = "Bearer ";
                private static final String TOKEN_HEADER = "Authorization";
    public String generateToken(org.springframework.security.core.Authentication authentication) {
    return com.auth0.jwt.JWT.create()
                .withSubject(authentication.getName())
                .withIssuedAt(new java.util.Date())
                .withExpiresAt(new java.util.Date(System.currentTimeMillis() + 7200L * 1000L))
                .withArrayClaim("roles", authentication.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC512(SECRET));
    }

    public java.util.Optional<org.springframework.security.core.Authentication> getAuthentication(String token) {
        try {
            com.auth0.jwt.interfaces.DecodedJWT decodedJWT = com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC512(SECRET))
                    .build()
                    .verify(token);

            org.springframework.security.core.Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    decodedJWT.getSubject(),
                    token,
                    decodedJWT.getClaim("roles").asList(org.springframework.security.core.GrantedAuthority.class));

            return java.util.Optional.of(authentication);

        } catch (com.auth0.jwt.exceptions.JWTVerificationException e) {
            return java.util.Optional.empty();
        }

    }

    public java.util.Optional<org.springframework.security.core.Authentication> getAuthentication(jakarta.servlet.http.HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null)
            return java.util.Optional.empty();

        if (!token.startsWith(TOKEN_PREFIX))
            return java.util.Optional.empty();

        return getAuthentication(token.substring(TOKEN_PREFIX.length()));
    }

                """.trimIndent()
            )
            append("}")
        }
    )

    override fun getVariableName(): String = getClassName().uncapitalize()

    override fun getClassName(): String = "JwtProvider"

    override fun getPackage(): String = ctx.getPackageName("security")

    override fun getFQCN(): String = getPackage() + "." + getClassName()
}
