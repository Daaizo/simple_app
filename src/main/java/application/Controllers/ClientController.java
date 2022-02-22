package application.Controllers;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import users.Client;
import users.Product;
import users.ProductTable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientController extends Controller {

    private int cartLabelXPosition;
    private int cartLabelYPosition;

    @FXML
    private AnchorPane ebooksAnchorPane, gamesAnchorPane;
    @FXML
    private Button ebooksButton, gamesButton, goBackButton;
    @FXML
    private Label cartQuantityLabel;
    @FXML
    private StackPane cartNotification, starNotification;
    @FXML
    private Pane categoryPickingPane;
    @FXML
    private TableColumn<ProductTable, String> gamesNameColumn, gamesSubcategoryColumn, ebooksNameColumn, ebooksSubcategoryColumn;
    @FXML
    private TableColumn<ProductTable, Double> gamesPriceColumn, ebooksPriceColumn;
    @FXML
    private TableColumn<ProductTable, String> ebooksStarButtonColumn, ebooksCartButtonColumn, gamesCartButtonColumn, gamesStarButtonColumn;
    @FXML
    private TableView<ProductTable> gamesTableView, ebooksTableView;

    @FXML
    public void initialize() {
        prepareScene();
        this.cartLabelXPosition = 60;
        this.cartLabelYPosition = 2;
        starNotification = createNotification(new Label("     Star button clicked"));
        cartNotification = createNotification(new Label("     Item added to cart"));
        createAccountButton();
        createGoBackButton();
        createCartButton();
        try {
            displayEbooks();
            displayGames();
            setQuantityLabel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void createAccountButton() {
        Button userAccountInformationButton = createButton("user.png", cartLabelXPosition + 60, cartLabelYPosition);
        userAccountInformationButton.setOnAction(event -> switchScene(event, clientAccountScene));
    }

    void createCartButton() {
        Button shoppingCartButton = createButton("cart.png", cartLabelXPosition, cartLabelYPosition);
        shoppingCartButton.setOnAction(event -> switchScene(event, shoppingCartScene));

    }

    private void createGoBackButton() {
        goBackButton = super.createGoBackButton(event -> {
            categoryPickingPane.setVisible(true);
            ebooksAnchorPane.setVisible(false);
            gamesAnchorPane.setVisible(false);
            goBackButton.setVisible(false);
        });
        goBackButton.fire();
        goBackButton.setVisible(false);
    }

    @FXML
    void gamesButtonClicked() {
        gamesAnchorPane.setVisible(true);
        categoryPickingPane.setVisible(false);
        goBackButton.setVisible(true);
    }

    @FXML
    void ebooksButtonClicked() {
        ebooksAnchorPane.setVisible(true);
        categoryPickingPane.setVisible(false);
        goBackButton.setVisible(true);
    }


    private void setQuantityLabel() throws SQLException {
        checkConnectionWithDb();
        cartQuantityLabel.setLayoutX(cartLabelXPosition + 20);
        int quantity = Client.getQuantityOfProductsInCart(CURRENT_USER_LOGIN, getConnection());
        if (quantity > 9)
            displayLabelWithGivenText(cartQuantityLabel, "9+");
        else displayLabelWithGivenText(cartQuantityLabel, " " + quantity);

    }

    private void fillEbooksColumnsWithData(ObservableList<ProductTable> list) {
        ebooksNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        ebooksPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        ebooksSubcategoryColumn.setCellValueFactory(new PropertyValueFactory<>("productSubcategory"));
        ebooksStarButtonColumn.setCellFactory(buttonInsideCell -> new ButtonInsideTableColumn<>("star.png", "add to favourite", starButtonClicked()));
        ebooksCartButtonColumn.setCellFactory(buttonInsideCell -> cartButtonClicked());
        ebooksTableView.setItems(list);
        showOnlyRowsWithData(ebooksTableView);

    }


    void displayEbooks() throws SQLException {
        checkConnectionWithDb();
        ResultSet products = Product.getProductsFromDatabase(getConnection());
        assert products != null;
        ObservableList<ProductTable> listOfEbooks = ProductTable.getProductsFromCategory(products, "ebooks");
        fillEbooksColumnsWithData(listOfEbooks);
    }

    private void fillGamesColumnsWithData(ObservableList<ProductTable> list) {
        gamesNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        gamesPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        gamesSubcategoryColumn.setCellValueFactory(new PropertyValueFactory<>("productSubcategory"));
        gamesStarButtonColumn.setCellFactory(buttonInsideCell -> new ButtonInsideTableColumn<>("star.png", "add to favourite", starButtonClicked()));
        gamesCartButtonColumn.setCellFactory(buttonInsideCell -> cartButtonClicked());
        gamesTableView.setItems(list);
        showOnlyRowsWithData(gamesTableView);
    }

    void displayGames() throws SQLException {
        checkConnectionWithDb();
        ResultSet products = Product.getProductsFromDatabase(getConnection());
        assert products != null;
        ObservableList<ProductTable> listOfGames = ProductTable.getProductsFromCategory(products, "games");
        fillGamesColumnsWithData(listOfGames);
    }

    private EventHandler<MouseEvent> starButtonClicked() {
        return
                event -> {
                    cartNotification.setVisible(false);
                    showNotification(starNotification, 1200);
                };
    }

    private ButtonInsideTableColumn<ProductTable, String> cartButtonClicked() {
        ButtonInsideTableColumn<ProductTable, String> button = new ButtonInsideTableColumn<>("add_cart.png", "add to cart");
        EventHandler<MouseEvent> eventHandler = event -> {
            String productName = button.getRowId().getProductName();
            starNotification.setVisible(false);
            showNotification(cartNotification, 1200);

            try {
                checkConnectionWithDb();
                Client.addItemToUsersCart(productName, CURRENT_USER_LOGIN, getConnection());
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    setQuantityLabel();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        button.setCssClassId("clientTableviewButtons");

        button.setEventHandler(eventHandler);
        return button;
    }


    public class ButtonInsideTableColumn<T, V> extends TableCell<T, V> {

        private final Button button;
        private final String iconName;
        private EventHandler<MouseEvent> eventHandler;
        private T rowId;
        private String cssId;
        private String cssClassId;

        public void setCssId(String cssId) {
            this.cssId = cssId;
        }

        public void setCssClassId(String classId) {
            this.cssClassId = classId;
        }

        public T getRowId() {
            return rowId;
        }

        public ButtonInsideTableColumn(String iconNameWithExtension, String buttonText, EventHandler<MouseEvent> eventHandler) {
            this.iconName = iconNameWithExtension;
            this.button = new Button(buttonText);
            this.eventHandler = eventHandler;
        }

        public ButtonInsideTableColumn(String iconNameWithExtension, String buttonText) {
            this.iconName = iconNameWithExtension;
            this.button = new Button(buttonText);
            button.fire();
        }

        public void setEventHandler(EventHandler<MouseEvent> eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        protected void updateItem(V item, boolean empty) {
            super.updateItem(item, empty);

            button.setOnAction(mouseEvent -> rowId = getTableView().getItems().get(getIndex()));
            button.setOnMouseClicked(eventHandler);
            button.setGraphic(setImageFromIconsFolder(iconName));
            button.setBackground(Background.EMPTY);
            button.setId(cssId);
            button.getStyleClass().add(cssClassId);
            setGraphic(button);
        }
    }



    //simple hover effects
    @FXML
    void ebookOnMouseEntered() {

        ebooksButton.setStyle("-fx-background-color: #fc766a; -fx-text-fill:  #5B84B1FF;");
    }

    @FXML
    void ebookOnMouseExited() {
        ebooksButton.setStyle("-fx-background-color:  #5B84B1FF; -fx-text-fill: #fc766a;-fx-border-color : #fc766a ;");
    }


    @FXML
    void gamesButtonOnMouseEntered() {
        gamesButton.setStyle("-fx-background-color: #fc766a; -fx-text-fill:  #5B84B1FF;");

    }

    @FXML
    void gamesButtonOnMouseExited() {
        gamesButton.setStyle("-fx-background-color:  #5B84B1FF; -fx-text-fill: #fc766a;-fx-border-color : #fc766a;");

    }

//


}
