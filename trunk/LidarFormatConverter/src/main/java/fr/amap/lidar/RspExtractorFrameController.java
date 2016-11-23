/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class RspExtractorFrameController implements Initializable {

    @FXML
    private HBox hboxSelection;
    @FXML
    private TreeView<LidarScan> treeViewLidarProjectContent;

    @FXML
    private void onActionButtonImportSelectedScans(ActionEvent event) {

        selectedScans = getSelectedScans(root, new ArrayList<>());
        stage.close();
    }

    @FXML
    private void onActionMenuItemSelectAll(ActionEvent event) {

        root.setSelected(true);
    }

    @FXML
    private void onActionMenuItemSelectNone(ActionEvent event) {
        root.setSelected(false);
    }

    final CheckBoxTreeItem<LidarScan> root = new CheckBoxTreeItem<>(new LidarScan(null, null, "Scan positions"));
    private List<LidarScan> selectedScans;
    private Stage stage;
    private Rsp rsp;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        selectedScans = new ArrayList<>();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private List<LidarScan> getSelectedScans(CheckBoxTreeItem item, List<LidarScan> indices) {

        ObservableList<CheckBoxTreeItem> childrens = item.getChildren();

        for (CheckBoxTreeItem children : childrens) {

            if (children.isLeaf()) {

                if (children.isSelected()) {
                    indices.add((LidarScan) children.getValue());
                }

            } else {
                getSelectedScans(children, indices);
            }
        }

        return indices;
    }

    private void selectLeavesContainingString(CheckBoxTreeItem node, String string, boolean contains) {

        ObservableList<CheckBoxTreeItem> childrens = node.getChildren();

        for (CheckBoxTreeItem children : childrens) {

            if (children.isLeaf()) {

                if (((LidarScan) children.getValue()).getName().contains(string)) {
                    children.setSelected(contains);
                } else {
                    children.setSelected(!contains);
                }
            } else {
                selectLeavesContainingString(children, string, contains);
            }
        }
    }

    public List<LidarScan> getSelectedScans() {
        return selectedScans;
    }

    public HBox getHboxSelection() {
        return hboxSelection;
    }

    public TreeView<LidarScan> getTreeViewLidarProjectContent() {
        return treeViewLidarProjectContent;
    }

    public CheckBoxTreeItem<LidarScan> getRoot() {
        return root;
    }

}
