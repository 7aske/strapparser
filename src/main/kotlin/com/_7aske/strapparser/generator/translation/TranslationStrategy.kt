package com._7aske.strapparser.generator.translation

import com._7aske.strapparser.extensions.toKebabCase
import com._7aske.strapparser.extensions.toSnakeCase

fun interface TranslationStrategy {
    fun translate(input: String): String
}

class CompositeTranslationStrategy(
    private val strategies: List<TranslationStrategy>
) : TranslationStrategy {
    override fun translate(input: String): String =
        strategies.fold(input) { acc, strategy -> strategy.translate(acc) }
}

class TranslationStrategyFactory private constructor() {
    companion object {
        fun getStrategy(strategies: List<TranslationStrategyType>) =
            CompositeTranslationStrategy(
                strategies.map { getStrategy(it) }
            )

        fun getStrategy(vararg strategies: TranslationStrategyType): TranslationStrategy =
            CompositeTranslationStrategy(
                strategies.map { getStrategy(it) }
            )

        private fun getStrategy(strategy: TranslationStrategyType): TranslationStrategy =
            when (strategy) {
                TranslationStrategyType.KEBAB -> KebabCaseTranslationStrategy()
                TranslationStrategyType.SNAKE -> SnakeCaseTranslationStrategy()
                TranslationStrategyType.UPPERCASE -> UpperCaseTranslationStrategy()
                TranslationStrategyType.LOWERCASE -> LowerCaseTranslationStrategy()
                TranslationStrategyType.NONE -> NoTranslationStrategy()
            }
    }
}

enum class TranslationStrategyType {
    KEBAB,
    SNAKE,
    UPPERCASE,
    LOWERCASE,
    NONE
}

class UpperCaseTranslationStrategy : TranslationStrategy {
    override fun translate(input: String): String = input.uppercase()
}

class LowerCaseTranslationStrategy : TranslationStrategy {
    override fun translate(input: String): String = input.lowercase()
}

class SnakeCaseTranslationStrategy : TranslationStrategy {
    override fun translate(input: String): String = input.toSnakeCase()
}

class KebabCaseTranslationStrategy : TranslationStrategy {
    override fun translate(input: String): String = input.toKebabCase()
}

class NoTranslationStrategy : TranslationStrategy {
    override fun translate(input: String): String = input
}
