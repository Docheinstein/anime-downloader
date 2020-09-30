package org.docheinstein.animedownloader.ui.base;

import javafx.scene.Parent;
import org.docheinstein.animedownloader.commons.utils.FxUtil;
import org.docheinstein.animedownloader.commons.utils.ResourceUtil;

/**
 * Entity that represents a controller able to allocate its content
 * from an asset.
 */
public interface InstantiableController {

    /**
     * Creates the root node of this controller.
     * @return the root node
     */
    default Parent createNode() {
        System.out.println("Creating node for asset: " + getFXMLAsset());
        System.out.println("-> URL: " + ResourceUtil.getAssetURL(getFXMLAsset()));
        return FxUtil.createNode(this, ResourceUtil.getAssetURL(getFXMLAsset()));
    }

    /**
     * Returns the FXML asset to use as root node for this controller.
     * @return the FXML asset
     */
    String getFXMLAsset();

}
