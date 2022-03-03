package application.Controllers;

import application.Main;
import dataBase.SqlConnection;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public abstract class Controller {
    public static String CURRENT_USER_LOGIN;
    public static final String CURRENCY = " $";
    public final String loginScene = "/application/FXML/loginGUI.fxml";
    public final String registrationScene = "/application/FXML/registerGUI.fxml";
    public final String adminScene = "/application/FXML/adminGUI.fxml";
    public final String clientScene = "/application/FXML/clientGUI.fxml";
    public final String shoppingCartScene = "/application/FXML/shoppingCartGUI.fxml";
    public final String clientAccountScene = "/application/FXML/clientAccountGUI.fxml";
    public final URL iconsUrl = getClass().getResource("/application/Icons/");
    public final URL cssUrl = getClass().getResource("/application/style.css");
    private SqlConnection instance;
    public HashMap<String, String> loginValues = Main.loginValues; // username - key, password - value
    @FXML
    protected AnchorPane anchor;


    public void prepareScene() {
        AnchorPane mainAnchor = setAnchorSizeAndColors();
        mainAnchor.getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        mainAnchor.getChildren().addAll(createHorizontalLine(), setSmallLogoInCorner());
        createExitButton();
        createLogoutButton();
    }

    protected ImageView setSmallLogoInCorner() {
        ImageView logo = setImageFromIconsFolder("transparentLogo.png");
        logo.setY(5);
        logo.setX(5);
        return logo;
    }

    protected void showOnlyRowsWithData(TableView<?> tableView) {
        tableView.setMaxHeight(510);
        tableView.setFixedCellSize(55);
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(40));
    }

    protected AnchorPane createHorizontalLine() {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setMinSize(1050, 3);
        anchorPane.setLayoutX(0);
        anchorPane.setLayoutY(52);
        anchorPane.setStyle("-fx-background-color :  #fc766a");
        return anchorPane;
    }

    protected Button createButton(String imageName, int x, int y) {
        int sideLength = 49;
        Button button = new Button();
        button.getStyleClass().add("topButtons");
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setMaxSize(sideLength + 2, sideLength);
        button.setPrefSize(sideLength + 2, sideLength);
        button.setGraphic(setImageFromIconsFolder(imageName));
        button.setOnMouseClicked(mouseEvent -> anchor.requestFocus());
        button.setOnAction(event -> anchor.requestFocus());
        anchor.getChildren().add(button);
        return button;
    }

    protected Button createGoBackButton(EventHandler<ActionEvent> event) {
        Button goBackButton = createButton("back-button.png", 5, 65);
        goBackButton.setOnAction(event);
        return goBackButton;
    }

    protected void createExitButton() {
        Button closeButton = createButton("close.png", 1000, 2);
        closeButton.setId("exitButton");
        closeButton.setOnAction(actionEvent -> Platform.exit());
    }

    private void createLogoutButton() {
        Button logoutButton = createButton("logout.png", 948, 2);
        logoutButton.setOnAction(actionEvent -> switchScene(actionEvent, loginScene));
    }


    AnchorPane setAnchorSizeAndColors() {
        anchor.setStyle("-fx-border-color :  #fc766a; -fx-border-width : 2px;-fx-background-color : #5B84B1FF ");
        anchor.setMinSize(1050, 694);
        anchor.setMaxSize(1050, 694);
        return anchor;
    }

    public ImageView setImageFromIconsFolder(String iconName) {
        return new ImageView(iconsUrl + iconName);
    }


    @FXML
    void Dragging() {
        Stage stage = (Stage) anchor.getScene().getWindow();
        anchor.setOnMousePressed(pressEvent -> anchor.setOnMouseDragged(dragEvent -> {
            stage.setX(dragEvent.getScreenX() - pressEvent.getSceneX());
            stage.setY(dragEvent.getScreenY() - pressEvent.getSceneY());
        }));
    }

    public void switchScene(ActionEvent event, String url) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(url)));
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene newScene = new Scene(root);
            window.setScene(newScene);
            window.show();
        } catch (IOException e) {
            System.out.println("error with switching scene");
            e.printStackTrace();
        }
    }

    private void showConnectionAlertAndWait() {
        while (instance.getConnection() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Connection to data base failed. Reconnect and try again.");
            alert.showAndWait();
            instance = SqlConnection.createInstance();
        }
    }

    public void checkConnectionWithDb() {
        instance = SqlConnection.createInstance();
        if (instance == null) showConnectionAlertAndWait();
    }

    public Connection getConnection() {
        return this.instance.getConnection();
    }

    public void updateHashMapWithLoginValues(String login, String pass) {
        loginValues.put(login, pass);
    }

    protected void colorField(TextField field, Label label, Color color) {

        DropShadow shadow = new DropShadow();
        shadow.setBlurType(BlurType.THREE_PASS_BOX);
        shadow.setSpread(0.60);
        shadow.setColor(color);
        shadow.setWidth(25);
        shadow.setHeight(25);
        shadow.setRadius(10);
        field.setEffect(shadow);
        field.setOnMouseClicked(mouseEvent -> {
            basicTheme(field);
            label.setVisible(false);
        });


    }


    protected void displayLabelWithGivenText(Label label, String text) {
        label.setText(text);
        label.setVisible(true);
    }

    protected void basicTheme(TextField field, Label label) {
        //resetting theme, used to undo red border on registration and login
        Glow glow = new Glow();
        field.setEffect(glow);
        label.setVisible(false);
    }

    protected void basicTheme(TextField field) {
        //resetting theme, used to undo red border on registration and login
        Glow glow = new Glow();
        field.setEffect(glow);
    }


    protected StackPane createNotification(Label label) {
        StackPane pane = new StackPane();
        ImageView image = setImageFromIconsFolder("check.png");
        pane.setId("notification");
        label.setId("notificationLabel");
        label.setPadding(new Insets(0, 0, 0, 47));
        pane.getChildren().addAll(label, image);
        label.setWrapText(true);
        StackPane.setAlignment(label, Pos.CENTER);
        StackPane.setAlignment(image, Pos.CENTER_LEFT);
        pane.setLayoutX(720);
        pane.setLayoutY(75);
        pane.setVisible(false);
        anchor.getChildren().add(pane);
        return pane;

    }

    protected Optional<ButtonType> createAndShowAlert(Alert.AlertType type, String headerText, String title, String contextText) {
        Alert alert = new Alert(type, contextText);
        alert.setHeaderText(headerText);
        alert.setTitle(title);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconsUrl + "transparentLogo.png"));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        dialogPane.getStyleClass().add("alert");
        return alert.showAndWait();
    }

    protected Optional<ButtonType> createAndShowAlert(ButtonType buttonType1, ButtonType buttonType2, String headerText, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", buttonType1, buttonType2);
        alert.setHeaderText(headerText);
        alert.setTitle(title);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconsUrl + "transparentLogo.png"));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        dialogPane.getStyleClass().add("alert");
        return alert.showAndWait();
    }

    protected Optional<ButtonType> deleteProductAlert(String productName, String productQuantity, String fromWhere) {
        Optional<ButtonType> buttonClicked;
        if (productQuantity.compareTo("1") == 0) {
            buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "DELETING PRODUCT FROM " + fromWhere.toUpperCase(),
                    "Confirmation",
                    "Do you want to delete '" + productName + "' from " + fromWhere);
        } else {
            buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "DELETING PRODUCT FROM " + fromWhere.toUpperCase(),
                    "Confirmation",
                    "Do you want to delete " + productQuantity + " pieces of '" + productName + "' from " + fromWhere);
        }
        return buttonClicked;
    }

    protected void showNotification(StackPane notification, double timeDuration) {
        notification.setVisible(false);
        notification.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(timeDuration), notification);
        fade.setFromValue(1000);
        fade.setToValue(0);
        fade.setCycleCount(1);
        fade.setAutoReverse(true);
        fade.play();

    }
}
