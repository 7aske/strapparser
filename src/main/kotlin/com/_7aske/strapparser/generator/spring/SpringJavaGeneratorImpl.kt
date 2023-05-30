package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.cli.Args
import com._7aske.strapparser.generator.AvailableDatabases
import com._7aske.strapparser.generator.Generator
import com._7aske.strapparser.generator.GeneratorContext
import com._7aske.strapparser.generator.translation.TranslationStrategyFactory
import com._7aske.strapparser.parser.StrapFileResolver
import com._7aske.strapparser.util.writeString
import java.nio.file.Paths

class SpringJavaGeneratorImpl(private val args: Args) : Generator {
    private val dataTypeResolver = SpringJavaDataTypeResolverImpl()

    override fun generate() {
        val translationStrategy = TranslationStrategyFactory.getStrategy(args.namingStrategy)

        val entities = StrapFileResolver(translationStrategy).resolve(args.inputFile)
            .associateBy { it.name }

        val ctx = GeneratorContext(entities, translationStrategy, args)

        if (ctx.args.lombok) {
            ctx.dependencies.add("org.projectlombok.lombok")
        }

        when (ctx.args.database) {
            AvailableDatabases.MARIADB -> {
                ctx.dependencies.add("org.springframework.boot.spring-boot-starter-data-jpa")
                ctx.dependencies.add("org.mariadb.jdbc.mariadb-java-client")
            }
            AvailableDatabases.MYSQL -> {
                ctx.dependencies.add("org.springframework.boot.spring-boot-starter-data-jpa")
                ctx.dependencies.add("mysql.mysql-connector-java")
            }
            AvailableDatabases.POSTGRES -> {
                ctx.dependencies.add("org.springframework.boot.spring-boot-starter-data-jpa")
                ctx.dependencies.add("org.postgresql.postgresql")
            }
            AvailableDatabases.MONGODB -> {
                ctx.dependencies.add("org.springframework.boot.spring-boot-starter-data-mongodb")
                ctx.dependencies.add("org.mongodb.mongodb-driver")
            }
        }

        val rootGenerator = SpringJavaRootGeneratorImpl(ctx, dataTypeResolver)

        writeString(rootGenerator.getOutputFilePath(), rootGenerator.generate())

        if (ctx.args.auditable) {
            val auditableGenerator =
                SpringJavaAuditableGeneratorImpl(ctx, dataTypeResolver)

            writeString(
                auditableGenerator.getOutputFilePath(),
                auditableGenerator.generate()
            )
        }

        val configGenerator = SpringJavaConfigGeneratorImpl(ctx, dataTypeResolver)

        writeString(configGenerator.getOutputFilePath(), configGenerator.generate())

        if (ctx.args.security) {
            ctx.dependencies.add("org.springframework.boot.spring-boot-starter-security")
            ctx.dependencies.add("com.auth0.java-jwt:4.2.1")

            SpringJavaSecurityGeneratorImpl(ctx, dataTypeResolver).generate()
        }

        if (ctx.args.doc) {
            ctx.dependencies.add("org.springframework.boot.spring-boot-starter-validation")
            ctx.dependencies.add("org.springdoc.springdoc-openapi-starter-webmvc-ui:2.0.4")
            val openapiGenerator = SpringJavaApiDocConfigGeneratorImpl(ctx, dataTypeResolver)
            val openapiOutPath = openapiGenerator.getOutputFilePath()

            writeString(openapiOutPath, openapiGenerator.generate())
        }

        entities.values.forEach {

            // Entity
            val entityGenerator =
                SpringJavaEntityGeneratorImpl(it, ctx, dataTypeResolver)
            val entityOutPath = entityGenerator.getOutputFilePath()

            if (ctx.args.entity || ctx.args.all) {
                writeString(entityOutPath, entityGenerator.generate())
            }

            // Repository
            val repositoryGenerator = SpringJavaRepositoryGeneratorImpl(
                entityGenerator,
                ctx,
                dataTypeResolver
            )
            val repositoryOutPath = repositoryGenerator.getOutputFilePath()

            if (ctx.args.repository || ctx.args.all) {
                writeString(repositoryOutPath, repositoryGenerator.generate())
            }

            // Service
            val serviceGenerator = SpringJavaServiceGeneratorImpl(
                entityGenerator,
                ctx,
                dataTypeResolver
            )
            val serviceOutPath = serviceGenerator.getOutputFilePath()

            if (ctx.args.service || ctx.args.all) {
                writeString(serviceOutPath, serviceGenerator.generate())
            }

            // ServiceImpl
            val serviceImplGenerator = SpringJavaServiceImplGeneratorImpl(
                repositoryGenerator,
                serviceGenerator,
                entityGenerator,
                ctx,
                dataTypeResolver
            )
            val serviceImplOutPath = serviceImplGenerator.getOutputFilePath()

            if (ctx.args.service || ctx.args.all) {
                writeString(serviceImplOutPath, serviceImplGenerator.generate())
            }

            // Controller
            val controllerGenerator = SpringJavaControllerGeneratorImpl(
                serviceGenerator,
                ctx,
                dataTypeResolver
            )
            val controllerOutPath = controllerGenerator.getOutputFilePath()

            if (ctx.args.controller || ctx.args.all) {
                writeString(controllerOutPath, controllerGenerator.generate())
            }

            val configurationGenerator = SpringConfigurationGeneratorImpl(
                ctx,
                dataTypeResolver
            )

            val configurationOutPath = configurationGenerator.getOutputFilePath()
            writeString(configurationOutPath, configurationGenerator.generate())

            if (ctx.args.doc) {

                val docGenerator = SpringJavaApiDocGeneratorImpl(
                    controllerGenerator,
                    ctx,
                    dataTypeResolver
                )
                val docOutPath = docGenerator.getOutputFilePath()

                writeString(docOutPath, docGenerator.generate())
            }
        }

        // maven or gradle
        val buildGenerator = SpringJavaMavenGenerator(ctx, dataTypeResolver)
        val buildOutPath = buildGenerator.getOutputFilePath()

        writeString(buildOutPath, buildGenerator.generate())

        try {
            ProcessBuilder("mvn", "wrapper:wrapper")
                .inheritIO()
                .directory(Paths.get(ctx.getOutputLocation()).toFile())
                .start()
                .waitFor()
        } catch (ignored: Exception) {
            println("Failed to generate maven wrapper. Please run 'mvn wrapper:wrapper' manually.")
        }
    }
}
