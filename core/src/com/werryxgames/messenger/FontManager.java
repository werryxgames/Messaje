package com.werryxgames.messenger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.concurrent.ConcurrentHashMap;

class FontData
{
    int fontType;
    int size;

    FontData(int fontType, int size)
    {
        this.fontType = fontType;
        this.size = size;
    }

    @Override
    public final boolean equals(Object object)
    {
        if (!(object instanceof FontData fontData))
        {
            return false;
        }

        return this.fontType == fontData.fontType && this.size == fontData.size;
    }

    @Override
    public final int hashCode()
    {
        return (this.fontType << 16) + this.size;
    }
}

/**
 * FontManager is used to fastly get already generated BitmapFonts from any class.
 *
 * @since 1.0
 */
public class FontManager
{
    private final FreeTypeFontGenerator[] fontGenerators;
    /**
     * Map with all generated BitmapFonts.
     *
     * @since 1.0
     */
    public ConcurrentHashMap<FontData, BitmapFont> fontMap;
    /**
     * Font parameter.
     * If you want to change any property of font,
     * change fontParameter before first ${link FontManager#getFont(int, int)}.
     *
     * @since 1.0
     */
    public FreeTypeFontParameter fontParameter;

    /**
     * Constructor for FontManager.
     *
     * @param fontPaths
     *     Paths to fonts. i == fontId in fontPaths[i].
     * @since 1.0
     */
    public FontManager(String... fontPaths)
    {
        this.fontGenerators = new FreeTypeFontGenerator[fontPaths.length];
        int fontPathsLength = fontPaths.length;

        for (int i = 0; i < fontPathsLength; i++)
        {
            //noinspection ObjectAllocationInLoop
            this.fontGenerators[i] = new FreeTypeFontGenerator(Gdx.files.internal(fontPaths[i]));
        }

        this.fontMap = new ConcurrentHashMap<>(64);
        this.fontParameter = new FreeTypeFontParameter();
    }

    /**
     * Returns generated font.
     * If font is not generated yet, generates and returns.
     *
     * @param fontType
     *     ID of font.
     * @param size
     *     Size of font.
     * @return Font.
     * @since 1.0
     */
    public final BitmapFont getFont(int fontType, int size)
    {
        FontData fontData = new FontData(fontType, size);

        if (this.fontMap.containsKey(fontData))
        {
            return this.fontMap.get(fontData);
        }

        FreeTypeFontGenerator fontGenerator = this.fontGenerators[fontType];
        this.fontParameter.size = size;
        //noinspection HardcodedFileSeparator
        this.fontParameter.characters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890, .?!@#$%^&*()" +
            "-=_+\\/`~[]{}|'\";" +
            ":<>йцукенгшщзхъфывапролджэячсмитьбюёЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮЁ•●";
        BitmapFont font = fontGenerator.generateFont(this.fontParameter);
        TextureRegion region = font.getRegion();
        Texture texture = region.getTexture();
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        this.fontMap.put(fontData, font);
        return font;
    }

    /**
     * Destroys all disposable objects.
     * Required to be called at the end of program.
     *
     * @since 1.0
     */
    public final void dispose()
    {
        for (BitmapFont font : this.fontMap.values())
        {
            font.dispose();
        }

        for (FreeTypeFontGenerator fontGenerator : this.fontGenerators)
        {
            fontGenerator.dispose();
        }
    }
}
