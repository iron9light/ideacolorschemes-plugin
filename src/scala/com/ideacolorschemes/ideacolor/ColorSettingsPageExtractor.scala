package com.ideacolorschemes.ideacolor

import com.intellij.application.options.colors.highlighting.HighlightsExtractor
import com.ideacolorschemes.commons.Implicits._
import annotation.tailrec
import com.intellij.openapi.options.colors.{ColorDescriptor, ColorSettingsPage}
import com.ideacolorschemes.commons.entities._
import com.intellij.application.options.colors.ColorSettingsUtil
import collection.JavaConversions._

object ColorSettingsPageExtractor {
  def extract(page: ColorSettingsPage): ColorSettingsPageObject = {
    val (id, version) = IdAndVersionUtil.idAndVersion(page)
    val name = page.getDisplayName
    val icon = extractIcon(page)
    val colors = extractColors(page)
    val attributes = extractAttributes(page)
    val codeSnippet = extractCodeSnippet(page)
    ColorSettingsPageObject(id, version, name, icon, colors, attributes, codeSnippet)
  }

  def extractColors(page: ColorSettingsPage) = {
    def convert(kind: ColorDescriptor.Kind) = kind match {
      case ColorDescriptor.Kind.FOREGROUND => ColorKind.FOREGROUND
      case ColorDescriptor.Kind.BACKGROUND => ColorKind.BACKGROUND
    }

    page.getColorDescriptors.toList.map(x => ColorDescriptorObject(x.getKey.getExternalName, x.getDisplayName, convert(x.getKind)))
  }

  def extractAttributes(page: ColorSettingsPage) = {
    ColorSettingsUtil.getAllAttributeDescriptors(page).toList.map(x => AttributesDescriptorObject(x.getKey.getExternalName, x.getDisplayName))
  }

  def extractCodeSnippet(page: ColorSettingsPage) = {
    val demo = page.getDemoText
    val highlighter = page.getHighlighter
    val extractor = new HighlightsExtractor(page.getAdditionalHighlightingTagToDescriptorMap)
    val extraMarks = extractor.extractHighlights(demo).toList.map(x => HighlightMark(x.getStartOffset, x.getEndOffset, x.getHighlightType :: Nil))
    val text = extractor.cutDefinedTags(demo)

    val lexer = highlighter.getHighlightingLexer
    lexer.start(text)

    @tailrec
    def advance(marks: List[HighlightMark]): List[HighlightMark] = {
      Option(lexer.getTokenType) match {
        case None => marks
        case Some(tokenType) =>
          val mark = HighlightMark(lexer.getTokenStart, lexer.getTokenEnd, highlighter.getTokenHighlights(tokenType).toList.map(_.getExternalName))
          lexer.advance()
          advance(mark :: marks)
      }
    }

    val lexerMarks = advance(Nil).reverse

    val filteredLexerMarks = extraMarks.foldLeft(lexerMarks) {
      case (marks, extraMark) =>
        marks.filter(mark => mark.startOffset < extraMark.startOffset || mark.endOffset > extraMark.endOffset)
    }

    CodeSnippet(text, filteredLexerMarks ::: extraMarks)
  }

  def extractIcon(page: ColorSettingsPage) = {
    val icon = Option(page.getIcon)
    ImageUtil.icon2Binary(icon)
  }
}