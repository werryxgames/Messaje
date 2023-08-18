package com.werryxgames.messenger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import java.io.File;

/**
 * Default game interface styles.
 *
 * @since 1.0
 */
public class UiStyle {
  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextField}.
   * Not for use.
   *
   * @since 1.0
   */
  public static final TextFieldStyle textFieldStyle = new TextFieldStyle();
  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.Label}.
   * Not for use.
   *
   * @since 1.0
   */
  public static final LabelStyle labelStyle = new LabelStyle();
  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextButton}.
   * Not for use.
   *
   * @since 1.0
   */
  public static final TextButtonStyle textButtonStyle = new TextButtonStyle();
  /**
   * Disabled style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextButton}.
   * Not for use.
   *
   * @since 1.0
   */
  public static final TextButtonStyle textButtonStyleDisabled = new TextButtonStyle();
  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.ScrollPane}.
   * Not for use.
   *
   * @since 1.0
   */
  public static final ScrollPaneStyle scrollPaneStyle = new ScrollPaneStyle();

  /**
   * Fills default styles with default values.
   *
   * @since 1.0
   */
  public static void create() {
    textFieldStyle.fontColor = new Color(0xd9d9d9ff);
    textFieldStyle.background = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(Gdx.files.internal("ui" + File.separator + "input.png")),
            5,
            5,
            5,
            5
        )
    );
    textFieldStyle.cursor = new TextureRegionDrawable(
        ResourceLoader.loadTexture(Gdx.files.internal("ui" + File.separator + "input_cursor.png"))
    );
    textFieldStyle.selection = new TextureRegionDrawable(
        ResourceLoader.loadTexture(
            Gdx.files.internal("ui" + File.separator + "input_selection.png"))
    );
    textFieldStyle.disabledBackground = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(Gdx.files.internal(
                "ui" + File.separator + "input_disabled.png")),
            5,
            5,
            5,
            5
        )
    );
    textFieldStyle.disabledFontColor = new Color(0xa3a3a3ff);
    textFieldStyle.messageFont = textFieldStyle.font;
    textFieldStyle.messageFontColor = textFieldStyle.fontColor;
    textFieldStyle.focusedBackground = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "input_focused.png")),
            5,
            5,
            5,
            5
        )
    );
    textFieldStyle.focusedFontColor = new Color(0xe6e6e6ff);

    labelStyle.fontColor = new Color(0xa3a3a3ff);

    textButtonStyle.up = textFieldStyle.background;
    textButtonStyle.down = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "input_pressed.png")),
            5,
            5,
            5,
            5
        )
    );
    textButtonStyle.focused = textFieldStyle.focusedBackground;
    textButtonStyle.downFontColor = new Color(0xcececeff);
    textButtonStyle.fontColor = textFieldStyle.fontColor;
    textButtonStyle.focusedFontColor = textFieldStyle.focusedFontColor;
    textButtonStyleDisabled.up = textFieldStyle.disabledBackground;
    textButtonStyleDisabled.down = textFieldStyle.disabledBackground;
    textButtonStyleDisabled.focused = textFieldStyle.disabledBackground;
    textButtonStyleDisabled.downFontColor = textFieldStyle.disabledFontColor;
    textButtonStyleDisabled.fontColor = textFieldStyle.disabledFontColor;
    textButtonStyleDisabled.focusedFontColor = textFieldStyle.disabledFontColor;

    scrollPaneStyle.background = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "scrollpane.png")),
            1,
            1,
            1,
            1
        )
    );
    scrollPaneStyle.hScroll = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "scrollpane_hscroll.png")),
            1,
            1,
            1,
            1
        )
    );
    scrollPaneStyle.vScroll = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "scrollpane_vscroll.png")),
            1,
            1,
            1,
            1
        )
    );
    scrollPaneStyle.hScrollKnob = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "scrollpane_hscrollknob.png")),
            1,
            1,
            1,
            1
        )
    );
    scrollPaneStyle.vScrollKnob = new NinePatchDrawable(
        new NinePatch(
            ResourceLoader.loadTexture(
                Gdx.files.internal("ui" + File.separator + "scrollpane_vscrollknob.png")),
            1,
            1,
            1,
            1
        )
    );
    scrollPaneStyle.corner = new TextureRegionDrawable(
        ResourceLoader.loadTexture(
            Gdx.files.internal("ui" + File.separator + "scrollpane_corner.png"))
    );
  }

  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextField}.
   *
   * @param fontType fontId for {@link FontManager}.
   * @param fontSize font size for {@link FontManager}.
   * @return Style.
   * @since 1.0
   */
  public static TextFieldStyle getTextFieldStyle(FontManager fontManager, int fontType,
                                                 int fontSize) {
    TextFieldStyle style = new TextFieldStyle(textFieldStyle);
    style.font = fontManager.getFont(fontType, fontSize);
    return style;
  }

  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.Label}.
   *
   * @param fontType fontId for {@link FontManager}.
   * @param fontSize font size for {@link FontManager}.
   * @return Style.
   * @since 1.0
   */
  public static LabelStyle getLabelStyle(FontManager fontManager, int fontType, int fontSize) {
    LabelStyle style = new LabelStyle(labelStyle);
    style.font = fontManager.getFont(fontType, fontSize);
    return style;
  }

  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextButton}.
   *
   * @param fontType fontId for {@link FontManager}.
   * @param fontSize font size for {@link FontManager}.
   * @return Style.
   * @since 1.0
   */
  public static TextButtonStyle getTextButtonStyle(FontManager fontManager, int fontType,
                                                   int fontSize) {
    TextButtonStyle style = new TextButtonStyle(textButtonStyle);
    style.font = fontManager.getFont(fontType, fontSize);
    return style;
  }

  /**
   * Disabled style for {@link com.badlogic.gdx.scenes.scene2d.ui.TextButton}.
   *
   * @param fontType fontId for {@link FontManager}.
   * @param fontSize font size for {@link FontManager}.
   * @return Style.
   * @since 1.0
   */
  public static TextButtonStyle getTextButtonStyleDisabled(FontManager fontManager, int fontType,
                                                           int fontSize) {
    TextButtonStyle style = new TextButtonStyle(textButtonStyleDisabled);
    style.font = fontManager.getFont(fontType, fontSize);
    return style;
  }

  /**
   * Default style for {@link com.badlogic.gdx.scenes.scene2d.ui.ScrollPane}.
   *
   * @return Style.
   * @since 1.0
   */
  public static ScrollPaneStyle getScrollPaneStyle() {
    return new ScrollPaneStyle(scrollPaneStyle);
  }
}
