package com._7aske.strapparser.generator.spring

import com._7aske.strapparser.generator.AvailableDatabases
import com._7aske.strapparser.generator.BaseGenerator
import com._7aske.strapparser.generator.DataTypeResolver
import com._7aske.strapparser.generator.GeneratorContext
import java.nio.file.Path
import java.nio.file.Paths

class SpringConfigurationGeneratorImpl(
    ctx: GeneratorContext,
    dataTypeResolver: DataTypeResolver
) : BaseGenerator(
    ctx, dataTypeResolver
) {
    override fun getOutputFilePath(): Path = Paths.get(
        ctx.getOutputLocation(),
        "src",
        "main",
        "resources",
        "application.properties"
    )

    override fun generate(): String = buildString {
        append(getDatabaseConnection())
        appendLine("spring.jpa.hibernate.ddl-auto=update")
    }

    private fun getDatabaseConnection(): String =
        when (ctx.args.database) {
            AvailableDatabases.POSTGRES -> buildString {
                appendLine(
                    "spring.datasource.url=jdbc:postgresql://${
                    ctx.args.databaseHost ?: "localhost"
                    }:${
                    ctx.args.databasePort ?: "5432"
                    }/${
                    ctx.args.databaseName ?: "postgres"
                    }"
                )
                appendLine("spring.datasource.username=${ctx.args.databaseUser ?: "postgres"}")
                appendLine("spring.datasource.password=${ctx.args.databasePass ?: "postgres"}")
            }

            AvailableDatabases.MARIADB -> buildString {
                appendLine(
                    "spring.datasource.url=jdbc:mariadb://${
                    ctx.args.databaseHost ?: "localhost"
                    }:${
                    ctx.args.databasePort ?: "3306"
                    }/${
                    ctx.args.databaseName ?: "test"
                    }"
                )
                appendLine("spring.datasource.username=${ctx.args.databaseUser ?: "root"}")
                appendLine("spring.datasource.password=${ctx.args.databasePass ?: ""}")
            }

            AvailableDatabases.MYSQL -> buildString {
                appendLine(
                    "spring.datasource.url=jdbc:mysql://${
                    ctx.args.databaseHost ?: "localhost"
                    }:${
                    ctx.args.databasePort ?: "3306"
                    }/${
                    ctx.args.databaseName ?: "test"
                    }"
                )
                appendLine("spring.datasource.username=${ctx.args.databaseUser ?: "root"}")
                appendLine("spring.datasource.password=${ctx.args.databasePass ?: ""}")
            }

            AvailableDatabases.MONGODB -> buildString {
                appendLine("spring.data.mongodb.host=${ctx.args.databaseHost ?: "localhost"}")
                appendLine("spring.data.mongodb.port=${ctx.args.databasePort ?: "27017"}")
                appendLine("spring.data.mongodb.database=${ctx.args.databaseName ?: "admin"}")
                appendLine("spring.data.mongodb.username=${ctx.args.databaseUser ?: "admin"}")
                appendLine("spring.data.mongodb.password=${ctx.args.databasePass ?: ""}")
            }
        }
}
