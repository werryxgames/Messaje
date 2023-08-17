package com.werryxgames.messenger;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * Loads resources.
 *
 * @since 1.0
 */
public class ResourceLoader
{
    /**
     * Default filter for {@link ResourceLoader#scaleTexture(FileHandle, int, int)}.
     *
     * @since 1.0
     */
    public static Filter defaultFilter = Filter.BiLinear;
    /**
     * Default texture filter for {@link ResourceLoader#loadTexture(FileHandle)}.
     *
     * @since 1.0
     */
    public static TextureFilter defaultTextureFilter = TextureFilter.Linear;

    /**
     * Loads texture and scales it to specified size.
     *
     * @param file
     *     File of texture.
     * @param width
     *     Scaled width.
     * @param height
     *     Scaled height.
     * @param filter
     *     Filter for loaded texture.
     * @return Scaled texture.
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public static Texture scaleTexture(FileHandle file, int width, int height, Filter filter)
    {
        Pixmap originalImage = new Pixmap(file);
        Pixmap scaledImage = new Pixmap(width, height, originalImage.getFormat());
        scaledImage.setFilter(filter);
        scaledImage.drawPixmap(
            originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), 0, 0,
            scaledImage.getWidth(), scaledImage.getHeight()
        );
        Texture texture = new Texture(scaledImage);
        originalImage.dispose();
        scaledImage.dispose();
        return texture;
    }

    /**
     * Loads texture and scales it to specified size.
     * Filter for scaled texture is {@link ResourceLoader#defaultFilter}.
     *
     * @param file
     *     File of texture.
     * @param width
     *     Scaled width.
     * @param height
     *     Scaled height.
     * @return Scaled texture.
     * @since 1.0
     */
    @SuppressWarnings("unused")
    public static Texture scaleTexture(FileHandle file, int width, int height)
    {
        Pixmap originalImage = new Pixmap(file);
        Pixmap scaledImage = new Pixmap(width, height, originalImage.getFormat());
        scaledImage.setFilter(ResourceLoader.defaultFilter);
        scaledImage.drawPixmap(
            originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), 0, 0,
            scaledImage.getWidth(), scaledImage.getHeight()
        );
        Texture texture = new Texture(scaledImage);
        originalImage.dispose();
        scaledImage.dispose();
        return texture;
    }

    /**
     * Loads texture.
     * Filter for loaded texture is {@link ResourceLoader#defaultTextureFilter}.
     *
     * @param file
     *     File of texture.
     * @return Loaded texture.
     * @since 1.0
     */
    public static Texture loadTexture(FileHandle file)
    {
        Texture texture = new Texture(file);
        texture.setFilter(ResourceLoader.defaultTextureFilter, ResourceLoader.defaultTextureFilter);
        return texture;
    }
}
