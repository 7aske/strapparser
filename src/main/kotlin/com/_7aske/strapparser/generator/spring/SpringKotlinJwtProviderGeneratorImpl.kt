package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.kotlin.Formatter
import com._7aske.strapparser.generator.kotlin.KotlinClassGenerator
import java.nio.file.Path
import java.nio.file.Paths

class SpringKotlinJwtProviderGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : KotlinClassGenerator(
    ctx, dataTypeResolver
) {
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
                import com.auth0.jwt.JWT
                import com.auth0.jwt.algorithms.Algorithm
                import com.auth0.jwt.exceptions.JWTVerificationException
                import jakarta.servlet.http.HttpServletRequest
                import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                import org.springframework.security.core.Authentication
                import org.springframework.security.core.GrantedAuthority
                import org.springframework.stereotype.Service
                import java.util.*

                @Service
                class JwtProvider {
                companion object {
                    private const val SECRET = "secret"
                    private const val TOKEN_PREFIX = "Bearer "
                    private const val TOKEN_HEADER = "Authorization"
                }

                fun generateToken(authentication: Authentication): String {
                    return JWT
                        .create()
                        .withSubject(authentication.name)
                        .withIssuedAt(Date())
                        .withExpiresAt(Date(System.currentTimeMillis() + 7200L * 1000L))
                        .withArrayClaim(
                            "roles",
                            authentication.authorities.map {
                                it.authority
                            }.toTypedArray()
                        )
                        .sign(Algorithm.HMAC512(SECRET))
                }

                fun getAuthentication(
                    token: String?
                ): Optional<Authentication> {
                    return try {
                        val decodedJWT = JWT
                            .require(Algorithm.HMAC512(SECRET))
                            .build()
                            .verify(token)
                        val authentication: Authentication =
                            UsernamePasswordAuthenticationToken(
                                decodedJWT.subject,
                                token,
                                decodedJWT
                                    .getClaim("roles")
                                    .asList(GrantedAuthority::class.java)
                            )
                        Optional.of(authentication)
                    } catch (e: JWTVerificationException) {
                        Optional.empty()
                    }
                }

                fun getAuthentication(
                    request: HttpServletRequest
                ): Optional<Authentication> {
                    val token = request.getHeader(TOKEN_HEADER)
                        ?: return Optional.empty()
                    return if (!token.startsWith(TOKEN_PREFIX)) Optional.empty() else getAuthentication(
                        token.substring(TOKEN_PREFIX.length)
                    )
                }
                }
                """.trimIndent()
            )
        }
    )

    override fun getClassName(): String = "JwtProvider"

    override fun getPackage(): String = ctx.getPackageName("security")
}
