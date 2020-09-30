package org.docheinstein.animedownloader.commons.utils;


import org.docheinstein.commons.asserts.Asserts;
import org.docheinstein.animedownloader.commons.constants.Config;

import java.io.InputStream;
import java.net.URL;

/**
 * Contains utility methods for load resources,
 * as {@link InputStream} or as {@link URL}.
 */
public class ResourceUtil {

    /**
     * This is kept just for retrieve the class loader that has loaded this
     * class, which can be handy for load resource without a reference to a class.
     **/
    // TODO
    // Find the reason why ResourceUtil.class.getClass()
    // returns null sometimes while from REF.getClass() doesn't
    private static final Object REF = new ResourceUtil();

    /**
     * Returns the {@link URL} associated with the given resource.
     * Note that the used class loader is the one that has loaded this class.
     * @param resource the resource to fetch
     * @return the URL of the resource
     *
     * @see #getResourceURL(Class, String)
     */
    public static URL getResourceURL(String resource) {
        return getResourceURL(REF.getClass(), resource);
    }

    /**
     * Returns the {@link URL} associated with the given resource using the class loader
     * of the given class.
     * @param clazz the class from which load the resource
     * @param resource the resource to fetch
     * @return the URL of the resource
     *
     * @see #getResourceURL(String)
     */
    public static URL getResourceURL(Class clazz, String resource) {
        return clazz.getClassLoader().getResource(resource);
    }

    /**
     * Returns the {@link InputStream} associated with the given resource.
     * Note that the used class loader is the one that has loaded this class.
     * @param resource the resource to fetch
     * @return the URL of the resource
     *
     * @see #getResourceURL(Class, String)
     */
    public static InputStream getResourceStream(String resource) {
        return getResourceStream(REF.getClass(), resource);
    }

    /**
     * Returns the {@link InputStream} associated with the given resource
     * using the class loader of the given class.
     * @param clazz the class from which load the resource
     * @param resource the resource to fetch
     * @return the URL of the resource
     *
     * @see #getResourceURL(String)
     */
    public static InputStream getResourceStream(Class clazz, String resource) {
        Asserts.assertNotNull(clazz, "Can't load resource for null class");
        return clazz.getClassLoader().getResourceAsStream(resource);
    }

    /**
     * Returns the {@link URL} associated with the given asset
     * using the class loader of the given class.
     * @param clazz the class from which load the resource
     * @param resource the asset to fetch
     * @return the URL of the asset
     *
     * @see #getAssetURL(String)
     */
    public static URL getAssetURL(Class clazz, String resource) {
        return getResourceURL(clazz, Config.Resources.ASSETS + resource);
    }

    /**
     * Returns the {@link URL} associated with the given asset
     * using the class loader of this class.
     * @param resource the asset to fetch
     * @return the URL of the asset
     *
     * @see #getAssetURL(Class, String)
     */
    public static URL getAssetURL(String resource) {
        return getResourceURL(Config.Resources.ASSETS + resource);
    }

    /**
     * Returns the {@link URL} associated with the given stylesheet
     * using the class loader of the given class.
     * @param clazz the class from which load the resource
     * @param resource the stylesheet to fetch
     * @return the URL of the stylesheet
     *
     * @see #getStyleURL(String)
     */
    public static URL getStyleURL(Class clazz, String resource) {
        return getResourceURL(clazz, Config.Resources.CSS + resource);
    }

    /**
     * Returns the {@link URL} associated with the given stylesheet
     * using the class loader of this class.
     * @param resource the stylesheet to fetch
     * @return the URL of the stylesheet
     *
     * @see #getStyleURL(Class, String)
     */
    public static URL getStyleURL(String resource) {
        return getResourceURL(Config.Resources.CSS + resource);
    }

    /**
     * Returns the {@link URL} associated with the given image
     * using the class loader of the given class.
     * @param clazz the class from which load the resource
     * @param resource the image to fetch
     * @return the URL of the image
     *
     * @see #getImageURL(String)
     */
    public static URL getImageURL(Class clazz, String resource) {
        return getResourceURL(clazz, Config.Resources.IMAGES + resource);
    }

    /**
     * Returns the {@link URL} associated with the given image
     * using the class loader of this class.
     * @param resource the asset to fetch
     * @return the URL of the image
     *
     * @see #getImageURL(Class, String)
     */
    public static URL getImageURL(String resource) {
        return getResourceURL(Config.Resources.IMAGES + resource);
    }

    /**
     * Returns an {@link InputStream} associated with the given image
     * using the class loader of the given class.
     * @param clazz the class from which load the resource
     * @param resource the image to fetch
     * @return an input stream for the image
     *
     * @see #getImageURL(String)
     * @see #getImageURL(Class, String)
     * @see #getImageStream(String)
     */
    public static InputStream getImageStream(Class clazz, String resource) {
        return getResourceStream(clazz, Config.Resources.IMAGES + resource);
    }

    /**
     * Returns an {@link InputStream} associated with the given image
     * using the class loader of this class.
     * @param resource the image to fetch
     * @return an input stream for the image
     *
     * @see #getImageURL(String)
     * @see #getImageURL(Class, String)
     * @see #getImageStream(Class, String)
     */
    public static InputStream getImageStream(String resource) {
        return getResourceStream(Config.Resources.IMAGES + resource);
    }
}