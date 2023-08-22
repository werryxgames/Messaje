package com.werryxgames.messaje;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;

class TextPart {
  final public TextPart PARENT;
  final public float FONT_SCALE;
  final public FontManager FONT_MANAGER;
  final public int FONT_ID;
  final public int FONT_SIZE;
  final public String TEXT;
  final public Label LABEL;

  TextPart(TextPart parent, String text, float fontScale, Color color, FontManager fontManager,
      int fontId, int fontSize) {
    this.PARENT = parent;
    this.FONT_SCALE = fontScale;
    this.FONT_MANAGER = fontManager;
    this.FONT_ID = fontId;
    this.FONT_SIZE = fontSize;
    this.TEXT = text;

    LabelStyle labelStyle = new LabelStyle(fontManager.getFont(fontId,
        (int) (fontSize * fontScale)), color);
    this.LABEL = new Label(text, labelStyle);
  }
}

public class FormattedText {

  protected static final String[] TEXT_FORMATTERS = new String[]{
      "**", "*", "^", "_", "`"
  };
  protected static final String[] FORMAT_ENDERS = new String[]{
      "**", "*", "^", "_", "`"
  };

  public static void formatLabels(Table parent, String text, Color color,
      FontManager fontManager, int fontId, int fontSize, Value maxWidthValue) {
    float maxWidth = maxWidthValue.get();
    Table table = new Table();
    table.left();
    ArrayList<TextPart> textParts = FormattedText.formatText(text, color, fontManager, 1, 0, 2, 3, 4,
        fontSize);

    for (int i = 0; i < textParts.size(); i++) {
      TextPart textPart = textParts.get(i);
      String str1 = textPart.LABEL.getText().toString();
      StringBuilder str2 = new StringBuilder(32);
      int constantWidth = 0;

      for (Actor actor : table.getChildren()) {
        if (!(actor instanceof Label label)) continue;

        label.getGlyphLayout().setText(label.getStyle().font, label.getText());
        constantWidth += label.getGlyphLayout().width;
      }

      textPart.LABEL.getGlyphLayout().setText(textPart.LABEL.getStyle().font, str1);

      while (constantWidth + textPart.LABEL.getGlyphLayout().width > maxWidth) {
        str2.insert(0, str1.charAt(str1.length() - 1));
        str1 = str1.substring(0, str1.length() - 1);

        if (str1.length() == 0) {
          throw new RuntimeException("Screen width is too low to render 1 formatted character");
        }

        textPart.LABEL.getGlyphLayout().setText(textPart.LABEL.getStyle().font, str1);
      }

      if (str2.length() == 0) {
        table.add(textPart.LABEL);
      } else {
        Label label1 = textPart.LABEL;
        label1.setText(str1);
        TextPart textPart2 = new TextPart(textPart, str2.toString(), textPart.FONT_SCALE, textPart.LABEL.getColor(), textPart.FONT_MANAGER, textPart.FONT_ID, textPart.FONT_SIZE);
        table.add(label1).expandX();
        table.pack();
        parent.add(table);
        parent.row();
        table = new Table();
        table.left();
        textParts.add(i + 1, textPart2);
      }
    }

    table.pack();
    parent.add(table);
  }

  public static int getFormatterIndex(String str) {
    int i = 0;

    for (String s : TEXT_FORMATTERS) {
      if (str.endsWith(s)) {
        return i;
      }

      i++;
    }

    return -1;
  }

  public static int getFormatEnder(String str, String ender) {
    int i = 0;

    for (String s : FORMAT_ENDERS) {
      if (!Objects.equals(s, ender)) {
        i++;
        continue;
      }

      if (str.endsWith(s)) {
        return i;
      }

      break;
    }

    return -1;
  }

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

  public static ArrayList<TextPart> formatText(String text,
      Color initialColor, FontManager fontManager, int normalFontId, int lightFontId,
      int italicFontId, int boldFontId, int boldItalicFontId, int initialFontSize) {
    ArrayList<TextPart> textParts = new ArrayList<>(8);
    Stack<String> textFormatters = new Stack<>();
    String[] tokens = FormattedText.getTokens(text);
    String prevToken = null;
    TextPart lastPart = null;
    boolean bold = false;
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
      } else {
        prevToken = token;
      }

      if (prevToken != null) {
        TextPart currentPart;

        float fontScale = !(sup || sub) ? 1f : 0.65f;
        int fontId;

        if (mono) {
          fontId = boldItalicFontId;
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
