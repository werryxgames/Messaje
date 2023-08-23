package com.werryxgames.messaje;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

/**
 * Class, that represents part of text, used in {@link FormattedText}.
 *
 * @since 1.0
 */
public class TextPart {

  public final TextPart parent;
  public final float fontScale;
  public final FontManager fontManager;
  public final int fontId;
  public final int fontSize;
  public final String text;
  public final Label label;

  public TextPart(TextPart parent, String text, float fontScale, Color color, FontManager fontManager,
      int fontId, int fontSize) {
    this.parent = parent;
    this.fontScale = fontScale;
    this.fontManager = fontManager;
    this.fontId = fontId;
    this.fontSize = fontSize;
    this.text = text;

    LabelStyle labelStyle = new LabelStyle(fontManager.getFont(fontId,
        (int) (fontSize * fontScale)), color);
    this.label = new Label(text, labelStyle);
  }
}
