package com.ideacolorschemes.ideacolor.util

import org.jdom.{Element => JElement, Text => JText, Comment => JComment, EntityRef => JEntityRef, ProcessingInstruction => JProcessingInstruction}
import scala.xml._

/**
 * @author il
 */
object JDomHelper {
  def build(elem: Elem, jElem: JElement): JElement = {
    elem.attributes.foreach {
      attr => jElem.setAttribute(attr.key, attr.value.mkString)
    }

    elem.child.foreach {
      x =>
        jElem.addContent(x match {
          case child: Elem =>
            build(child, new JElement(child.label))
          case child: Atom[_] =>
            new JText(child.text)
          case child: Comment =>
            new JComment(child.commentText)
          case child: EntityRef =>
            new JEntityRef(child.entityName)
          case child: ProcInstr =>
            new JProcessingInstruction(child.target, child.proctext)
        })
    }

    jElem
  }

  def toNode(jElem: JElement) = {
    val source = new org.jdom.transform.JDOMSource(jElem)
    XML.load(source.getInputSource)
  }
}
