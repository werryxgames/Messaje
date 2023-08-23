package com.werryxgames.messaje;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

/**
 * Class for formatting messages to labels.
 *
 * @since 1.0
 */
public class FormattedText {
  /**
   * Formats parent with text.
   *
   * @param parent Table, that contains labels.
   * @param text Markup language.
   * @param color Color of text.
   * @param fontManager Instance of {@link FontManager}.
   * @param fontId Identifier of font.
   * @param fontSize Size of font.
   * @param maxWidthValue Maximum width, after which text will be wrapped.
   */
  public static void formatLabels(Table parent, String text, Color color,
      FontManager fontManager, int fontId, int fontSize, Value maxWidthValue) {
    float maxWidth = maxWidthValue.get();
    Table table = new Table();
    table.left();
    ArrayList<TextPart> textParts = FormattedText.formatText(text, color, fontManager, 0, 1, 2, 3,
        4, 5, fontSize);

    for (int i = 0; i < textParts.size(); i++) {
      TextPart textPart = textParts.get(i);
      String str1 = textPart.label.getText().toString();
      StringBuilder str2 = new StringBuilder(32);
      int constantWidth = 0;

      for (Actor actor : table.getChildren()) {
        if (!(actor instanceof Label label)) {
          continue;
        }

        label.getGlyphLayout().setText(label.getStyle().font, label.getText());
        constantWidth += label.getGlyphLayout().width;
      }

      textPart.label.getGlyphLayout().setText(textPart.label.getStyle().font, str1);

      while (constantWidth + textPart.label.getGlyphLayout().width > maxWidth) {
        str2.insert(0, str1.charAt(str1.length() - 1));
        str1 = str1.substring(0, str1.length() - 1);

        if (str1.length() == 0) {
          throw new RuntimeException("Screen width is too low to render 1 formatted character");
        }

        textPart.label.getGlyphLayout().setText(textPart.label.getStyle().font, str1);
      }

      if (str2.length() == 0) {
        table.add(textPart.label);
      } else {
        Label label1 = textPart.label;
        label1.setText(str1);
        table.add(label1);
        table.pack();
        parent.add(table).left();
        parent.row();
        table = new Table();
        table.left();
        TextPart textPart2 = new TextPart(textPart, str2.toString(), textPart.fontScale,
            textPart.label.getColor(), textPart.fontManager, textPart.fontId, textPart.fontSize);
        textParts.add(i + 1, textPart2);
      }
    }

    table.pack();
    parent.add(table).left();
  }

  /**
   * Parses string to markup language tokens.
   *
   * @param string String to parse.
   * @return Array of separated tokens.
   */
  public static String[] getTokens(String string) {
    ArrayList<String> tokens = new ArrayList<>(16);

    StringBuilder buffer = new StringBuilder(16);
    int sameOperator = 0;
    char[] charArray = string.toCharArray();

    for (char c : charArray) {
      if (c == '*' || c == '_' || c == '^' || c == '`') {
        if (sameOperator == 0) {
          tokens.add(buffer.toString());
          buffer = new StringBuilder(String.valueOf(c));
          sameOperator = 1;
        } else {
          if (buffer.charAt(buffer.length() - 1) == c) {
            sameOperator++;
            buffer.append(c);
          } else {
            if (sameOperator > 2 || c != '*') {
              if (tokens.size() == 0) {
                tokens.add(buffer.toString());
              } else {
                tokens.set(tokens.size() - 1, tokens.get(tokens.size() - 1) + buffer);
              }
            } else {
              tokens.add(buffer.toString());
            }

            buffer = new StringBuilder(String.valueOf(c));
            sameOperator = 1;
          }
        }
      } else {
        if (sameOperator > 0) {
          tokens.add(buffer.toString());
          buffer = new StringBuilder(String.valueOf(c));
          sameOperator = 0;
        } else {
          buffer.append(c);
        }
      }
    }

    if (buffer.length() > 0) {
      tokens.add(buffer.toString());
    }

    String[] array = new String[tokens.size()];
    return tokens.toArray(array);
  }

  /**
   * Formats text.
   *
   * @param text Text to format.
   * @param initialColor Default color, that will be used, until directly specified in text.
   * @param fontManager Font manager to use.
   * @param normalFontId Identifier of normal font in specified earlier fontManager.
   * @param lightFontId Identifier of light font in specified earlier fontManager.
   * @param italicFontId Identifier of italic font in specified earlier fontManager.
   * @param boldFontId Identifier of bold font in specified earlier fontManager.
   * @param boldItalicFontId Identifier of bold italic font in specified earlier fontManager.
   * @param monospaceFontId Identifier of monospace font in specified earlier fontManager.
   * @param initialFontSize Default font size, that will be used, until directly specified in text.
   * @return {@link ArrayList} of {@link TextPart}s.
   */
  public static ArrayList<TextPart> formatText(String text,
      Color initialColor, FontManager fontManager, int normalFontId, int lightFontId,
      int italicFontId, int boldFontId, int boldItalicFontId, int monospaceFontId, int initialFontSize) {
    ArrayList<TextPart> textParts = new ArrayList<>(8);
    Stack<String> textFormatters = new Stack<>();
    String[] tokens = FormattedText.getTokens(text);
    String prevToken = null;
    TextPart lastPart = null;
    boolean bold = false;
    boolean light = false;
    boolean italic = false;
    boolean sup = false;
    boolean sub = false;
    boolean mono = false;

    for (String token : tokens) {
      if (!textFormatters.empty()) {
        String topTextFormatter = textFormatters.peek();

        if (Objects.equals(token, topTextFormatter)) {
          if (Objects.equals(token, "**")) {
            bold = false;
            textFormatters.pop();
            continue;
          }
          if (Objects.equals(token, "*")) {
            italic = false;
            textFormatters.pop();
            continue;
          }
          if (Objects.equals(token, "^")) {
            sup = false;
            textFormatters.pop();
            continue;
          }
          if (Objects.equals(token, "_")) {
            sub = false;
            textFormatters.pop();
            continue;
          }
          if (Objects.equals(token, "`")) {
            mono = false;
            textFormatters.pop();
            continue;
          }
          if (Objects.equals(token, "|")) {
            light = false;
            textFormatters.pop();
            continue;
          }

          throw new IllegalStateException("Unknown formatter: '" + token + "'");
        }
      }

      if (Objects.equals(token, "**")) {
        bold = true;
        textFormatters.add(token);
      } else if (Objects.equals(token, "*")) {
        italic = true;
        textFormatters.add(token);
      } else if (Objects.equals(token, "^")) {
        sup = true;
        textFormatters.add(token);
      } else if (Objects.equals(token, "_")) {
        sub = true;
        textFormatters.add(token);
      } else if (Objects.equals(token, "`")) {
        mono = true;
        textFormatters.add(token);
      } else if (Objects.equals(token, "|")) {
        light = true;
        textFormatters.add(token);
      } else {
        prevToken = token;
      }

      if (prevToken != null) {
        TextPart currentPart;

        float fontScale = !(sup || sub) ? 1f : 0.65f;
        int fontId;

        if (mono) {
          fontId = monospaceFontId;
        } else if (light) {
          fontId = lightFontId;
        } else if (bold) {
          if (italic) {
            fontId = boldItalicFontId;
          } else {
            fontId = boldFontId;
          }
        } else if (italic) {
          fontId = italicFontId;
        } else {
          fontId = normalFontId;
        }

        if (lastPart == null) {
          currentPart = new TextPart(null, prevToken, fontScale,
              initialColor, fontManager, fontId, initialFontSize);
        } else {
          currentPart = new TextPart(lastPart, prevToken, fontScale, initialColor, fontManager,
              fontId, initialFontSize);
        }

        textParts.add(currentPart);
        lastPart = currentPart;
        prevToken = null;
      }
    }

    return textParts;
  }
}
