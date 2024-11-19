package com.github.sereden.swagger.engine

class TestFileAppendable : Appendable {
    private val stringBuilder = StringBuilder()
    val text: String
        get() = stringBuilder.toString()

    override fun append(csq: CharSequence?): java.lang.Appendable {
        if (csq == null) {
            stringBuilder.append("null")
        } else {
            stringBuilder.append(csq)
        }
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
        if (csq == null) {
            stringBuilder.append("null", start, end)
        } else {
            stringBuilder.append(csq, start, end)
        }
        return this
    }

    override fun append(csq: Char): java.lang.Appendable {
        stringBuilder.append(csq)
        return this
    }
}