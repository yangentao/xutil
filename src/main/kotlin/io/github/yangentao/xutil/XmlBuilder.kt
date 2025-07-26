@file:Suppress("MemberVisibilityCanBePrivate", "FunctionName", "unused")

package io.github.yangentao.xutil

import io.github.yangentao.types.replaceChars
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

val String.xmlEscaped: String
    get() {
        return this.replaceChars('<' to "&lt;", '>' to "&gt;", '&' to "&amp;", '"' to "&quot;", '\'' to "&apos;")
    }

class NodeBuild(val element: Element) {

    fun element(name: String, vararg attrPairs: Pair<String, Any>, block: NodeBuild.() -> Unit): NodeBuild {
        val e = element.ownerDocument.createElement(name)
        this.element.appendChild(e)
        val n = NodeBuild(e)
        for (p in attrPairs) {
            n.attr(p.first, p.second.toString())
        }
        n.block()
        return n
    }

    fun cdata(data: String) {
        val e = element.ownerDocument.createCDATASection(data)
        this.element.appendChild(e)
    }

    fun attr(key: String, value: String) {
        element.setAttribute(key, value)
    }

    infix fun String.TO(value: Any) {
        attr(this, value.toString())
    }

    operator fun String?.unaryPlus() {
        text(this)
    }

    fun text(text: String?) {
        val s = text ?: return
        val node = element.ownerDocument.createTextNode(s)
        element.appendChild(node)
    }

    fun text(block: () -> String?) {
        text(block())
    }

    override fun toString(): String {
        return this.toXml(false, false)
    }

    fun toXml(xmlDeclare: Boolean, indent: Boolean): String {
        val t = TransformerFactory.newInstance().newTransformer()
        if (!xmlDeclare) {
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
        if (indent) {
            t.setOutputProperty(OutputKeys.INDENT, "yes")
        }
        val w = StringWriter(2048)
        t.transform(DOMSource(this.element), StreamResult(w))
        return w.toString()
    }

}

fun Node.toXml(xmlDeclare: Boolean, indent: Boolean): String {
    val t = TransformerFactory.newInstance().newTransformer()
    if (!xmlDeclare) {
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    }
    if (indent) {
        t.setOutputProperty(OutputKeys.INDENT, "yes")
    }
    val w = StringWriter(2048)
    t.transform(DOMSource(this), StreamResult(w))
    return w.toString()
}

fun createRootElementXML(name: String): Element {
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    val e = doc.createElement(name)
    doc.appendChild(e)
    return e
}

fun buildXML(rootName: String, vararg attrPairs: Pair<String, Any>, block: NodeBuild.() -> Unit): NodeBuild {
    val n = NodeBuild(createRootElementXML(rootName))
    for (p in attrPairs) {
        n.attr(p.first, p.second.toString())
    }
    n.block()
    return n
}

fun testXml() {
    val a = buildXML("Person", "age" to 38) {
        element("child", "name" to "suo", "age" to 9) {
            element("school") {
                cdata("WenYuan")
            }
        }
    }
    print(a.toXml(true, true))
}

fun main() {
    testXml()
}

