package org.docheinstein.animedownloader.commons.utils;

import javafx.css.Styleable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.docheinstein.commons.asserts.Asserts;
import org.docheinstein.commons.internal.DocCommonsLogger;
import org.docheinstein.commons.types.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Contains utility methods for Java FX. */
public class FxUtil {

    private static final DocCommonsLogger L = DocCommonsLogger.createForTag("{FX_UTIL}");

    public static void setExistent(Node node, boolean existent) {
        if (node == null)
            return;
        node.setVisible(existent);
        node.setManaged(existent);
    }

    /**
     * Creates a new window using the given root element and title
     * and shows it via {@link Stage#showAndWait()} ()}
     * @param root the root node
     * @param title the title
     * @return the created stage
     */
    public static Stage showWindowAndWait(Parent root, String title) {
        Stage stage = createWindow(root, title);
        stage.showAndWait();
        return stage;
    }

    /**
     * Creates a new window using the given root element and title
     * and shows it via {@link Stage#show()}
     * @param root the root node
     * @param title the title
     * @return the created stage
     */
    public static Stage showWindow(Parent root, String title) {
        Stage stage = createWindow(root, title);
        stage.show();
        return stage;
    }

    /**
     * Creates a new window using the given root element and title.
     * @param root the root node
     * @param title the title
     * @return the created stage
     */
    public static Stage createWindow(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        return stage;
    }

    /**
     * Adds a stylesheet to the given node.
     * @param node a node
     * @param stylesheet the stylesheet to add to the given node
     */
    public static void addStylesheet(Parent node, URL stylesheet) {
        node.getStylesheets().add(stylesheet.toExternalForm());
    }

    /**
     * Adds the style class for the given {@link Styleable}.
     * @param obj the styleable object
     * @param styleClass the style class
     *
     * @see #setClass(Styleable, String)
     */
    public static void addClass(Styleable obj, String styleClass) {
        if (!obj.getStyleClass().contains(styleClass))
            obj.getStyleClass().add(styleClass);
    }

    /**
     * Removes every style class from the given {@link Styleable}.
     * @param obj the styleable object
     */
    public static void clearClasses(Styleable obj) {
        obj.getStyleClass().clear();
    }

    /**
     * Sets the style class for the given {@link Styleable} and removes
     * any already existing class.
     * @param obj the styleable object
     * @param styleClass the style class
     *
     * @see #addClass(Styleable, String)
     */
    public static void setClass(Styleable obj, String styleClass) {
        clearClasses(obj);
        addClass(obj, styleClass);
    }
//
//    /**
//     * Converts a {@link BufferedImage} to an {@link Image}.
//     * @param bufferedImage the awt image
//     * @return the java fx image
//     */
//    public static Image awtBufferedImageToImage(BufferedImage bufferedImage) {
//        return SwingFxUtil..toFXImage(bufferedImage, null);
//    }

    /**
     * Creates a {@link Parent} for the given FXML asset and binds it with
     * the given controller.
     * @param controller the controller bound with the node
     * @param fxml the .fxml asset
     * @return the node associated with the given asset, bound with the given controller
     */
    public static Parent createNode(Object controller, URL fxml) {
        Asserts.assertNotNull(controller, "Can't bound to null controller");

        L.out("Creating node for FXML: " + fxml + " bound to controller " +
            controller.getClass().getSimpleName());

        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(controller);
        Parent node = null;
        try {
            node = loader.load();
        } catch (IOException e) {
            L.out("Error occurred while loading FXML for URL: " + fxml + ".\n" +
                StringUtil.toString(e));
            return null;
        }

        Asserts.assertNotNull(node, "Error occurred while loading FXML for URL: " + fxml);

        return node;
    }

    /**
     * Creates an {@link Image} for the given resource.
     * @param inputStream the resource
     * @return an image for the given resource name
     */
    public static Image createImage(InputStream inputStream) {
        return new Image(inputStream);
    }

    /**
     * Attaches a node to an anchor pane using 0 as anchor value for all
     * the directions and removes the already attached nodes.
     * @param parent the parent anchor pane
     * @param child the child node
     *
     * @see #attachToAnchorPane(AnchorPane, Node, double, double, double, double, boolean)
     */
    public static void attachToAnchorPane(AnchorPane parent, Node child) {
        attachToAnchorPane(parent, child, 0);
    }

    /**
     * Attaches a node to an anchor pane using the given anchor value
     * for all the directions and removes the already attached nodes.
     * @param parent the parent anchor pane
     * @param child the child node
     * @param anchor the anchor value to use for all the directions
     *
     * @see #attachToAnchorPane(AnchorPane, Node, double, double, double, double, boolean)
     */
    public static void attachToAnchorPane(AnchorPane parent, Node child,
                                          double anchor) {
        attachToAnchorPane(parent, child, anchor, true);
    }


    /**
     * Attaches a node to an anchor pane using the given anchor value
     * for all the directions.
     * @param parent the parent anchor pane
     * @param anchor the anchor value to use for all the directions
     * @param removeAttachedNodes whether remove the already attached nodes from
     *                            the anchor pane before attach the new child
     *
     * @see #attachToAnchorPane(AnchorPane, Node, double, double, double, double, boolean)
     */
    public static void attachToAnchorPane(AnchorPane parent, Node child,
                                          double anchor, boolean removeAttachedNodes) {
        attachToAnchorPane(
            parent, child, anchor, anchor, anchor, anchor, removeAttachedNodes);
    }


    /**
     * Attaches a node to an anchor pane  using the given anchor values.
     * @param parent the parent anchor pane
     * @param child the child node
     * @param l the left anchor value
     * @param t the top anchor value
     * @param r the right anchor value
     * @param b the bottom anchor value
     * @param removeAttachedNodes whether remove the already attached nodes from
     *                            the anchor pane before attach the new child
     */
    public static void attachToAnchorPane(AnchorPane parent, Node child,
                                          double l, double t, double r, double b,
                                          boolean removeAttachedNodes) {
        AnchorPane.setLeftAnchor(child, l);
        AnchorPane.setRightAnchor(child, r);
        AnchorPane.setTopAnchor(child, t);
        AnchorPane.setBottomAnchor(child, b);

        if (removeAttachedNodes)
            parent.getChildren().clear();

        parent.getChildren().add(child);
    }
}
