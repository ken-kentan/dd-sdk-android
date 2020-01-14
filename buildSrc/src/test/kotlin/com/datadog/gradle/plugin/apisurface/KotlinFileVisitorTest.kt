/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-2020 Datadog, Inc.
 */

package com.datadog.gradle.plugin.apisurface

import java.io.File
import java.lang.IllegalStateException
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class KotlinFileVisitorTest {

    @get:Rule
    val tempDir = TemporaryFolder()
    lateinit var tempFile: File

    lateinit var testedVisitor: KotlinFileVisitor

    @Before
    fun `set up`() {
        tempFile = tempDir.newFile(FILE_NAME)
        testedVisitor = KotlinFileVisitor()
    }

    @Test
    fun `describes public class`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes public class in root package`() {
        tempFile.writeText(
            """
            class Spam {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes public object`() {
        tempFile.writeText(
            """
            package foo.bar
            object Spam {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "object foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes public interface`() {
        tempFile.writeText(
            """
            package foo.bar
            interface Spam {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "interface foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes public enums`() {
        tempFile.writeText(
            """
            package foo.bar
            enum class Spam {
                A, B, C;
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "enum foo.bar.Spam\n" +
                "  - A\n" +
                "  - B\n" +
                "  - C\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes public sealed class`() {
        tempFile.writeText(
            """
            package foo.bar
            sealed class Spam {
                class A : Spam()
                class B : Spam()
                class C : Spam()
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "sealed class foo.bar.Spam\n" +
                "  class A : Spam\n" +
                "  class B : Spam\n" +
                "  class C : Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public classes`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                internal class C {}
                private class D {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores init block`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                init {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public objects`() {
        tempFile.writeText(
            """
            package foo.bar
            object Spam {
                internal object C {}
                private object D {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "object foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public interfaces`() {
        tempFile.writeText(
            """
            package foo.bar
            interface Spam {
                internal interface C {}
                private interface D {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "interface foo.bar.Spam\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public methods`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                fun doSomething() {}
                internal fun youCantSeeThis() {}
                private fun youCantSeeThisEither() {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  fun doSomething()\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public methods in interfaces`() {
        tempFile.writeText(
            """
            package foo.bar
            interface Spam {
                fun doSomething()
                internal fun youCantSeeThis()
                private fun youCantSeeThisEither()
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "interface foo.bar.Spam\n" +
                "  fun doSomething()\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public properties`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                val s : String = "FOO"
                internal val youCantSeeThis = 0
                private val youCantSeeThisEither = false
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  val s: String\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public constructors`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam(i : Int) {
                private constructor(s : String) : this(s.length)
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  constructor(Int)\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes abstract and open classes`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                open class C {}
                abstract class D {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  open class C\n" +
                "  abstract class D\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes abstract and open methods`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                open fun doSomething {}
                abstract fun doSomethingElse {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  open fun doSomething()\n" +
                "  abstract fun doSomethingElse()\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes protected abstract and open methods`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                protected open fun doSomething {}
                protected abstract fun doSomethingElse {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  protected open fun doSomething()\n" +
                "  protected abstract fun doSomethingElse()\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes abstract and open properties`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                open val s:String = ""
                abstract var i : Int = 0
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  open val s: String\n" +
                "  abstract var i: Int\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes generics in class types`() {
        tempFile.writeText(
            """
            package foo.bar
            import java.io.IOException
            class Spam<T:IOException, R> {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam<T: java.io.IOException, R>\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes generics in functions`() {
        tempFile.writeText(
            """
            package foo.bar
            import java.io.IOException
            class Spam {
                fun <T: IOException> doSomething(input: T) : IOException {
                    return IOException(input)
                }
                
                fun <K, V> doSomethingElse(map : Map<K, V>) : Pair<List<K>, List<V>> {
                    TODO()
                }
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  fun <T: java.io.IOException> doSomething(T): java.io.IOException\n" +
                "  fun <K, V> doSomethingElse(Map<K, V>): Pair<List<K>, List<V>>\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes parent class and interfaces in class types`() {
        tempFile.writeText(
            """
            package foo.bar
            import java.io.IOException
            import java.lang.Runnable
            import java.util.Comparator
            class Spam:IOException, Runnable, Comparator<String> {
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam : java.io.IOException, java.lang.Runnable, java.util.Comparator<String>\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes function with default parameters`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                fun doSomething(i: Int, s: String? = null, l : List<String> = emptyList()) {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  fun doSomething(Int, String? = null, List<String> = emptyList())\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes overriden functions`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam : Runnable{
                override fun run ( ) { }
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam : Runnable\n" +
                "  override fun run()\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes function with lambda parameters`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                fun withInputs(block : (String, Char) -> Int) {}
                fun unit(block : (String) -> Unit) {}
                fun nullable(block : (String) -> Any?) {}
                fun withReceiver(block : String.() -> Int) {}
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  fun withInputs((String, Char) -> Int)\n" +
                "  fun unit((String) -> Unit)\n" +
                "  fun nullable((String) -> Any?)\n" +
                "  fun withReceiver(String.() -> Int)\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes companion object`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                companion object {
                    const val DATA : String= "Something"
                }
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  companion object\n" +
                "    const val DATA: String\n",
            testedVisitor.description.toString()
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `throw error on implicit type`() {
        tempFile.writeText(
            """
            package foo.bar
            class Spam {
                companion object {
                    const val DATA = "Something"
                }
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)
    }

    @Test
    fun `replace alias type with full name`() {
        tempFile.writeText(
            """
            package foo.bar
            import com.example.Data as FooData
            class Spam {
                fun doSomething() : FooData
            }
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "class foo.bar.Spam\n" +
                "  fun doSomething(): com.example.Data\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `describes type alias`() {
        tempFile.writeText(
            """
            typealias StringTransform = (String) -> String?
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "typealias StringTransform = (String) -> String?\n",
            testedVisitor.description.toString()
        )
    }

    @Test
    fun `ignores non public type alias`() {
        tempFile.writeText(
            """
            internal typealias StringTransform = (String) -> String?
            private typealias IntTransform = (Int) -> Int
        """.trimIndent()
        )

        testedVisitor.visitFile(tempFile)

        assertEquals(
            "",
            testedVisitor.description.toString()
        )
    }

    companion object {
        const val FILE_NAME = "file.kt"
    }
}
