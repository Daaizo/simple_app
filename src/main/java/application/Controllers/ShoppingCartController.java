package application.Controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import users.Client;
import users.Order;
import users.Product;
import users.ProductTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class ShoppingCartController extends Controller {

    private String paymentMethod = null;
    private double totalOrderValue;

    @FXML
    private TableColumn<ProductTable, String> cartNameColumn, cartPriceColumn, cartValueColumn, plusButtonColumn, minusButtonColumn, deleteButtonColumn;
    @FXML
    private TableColumn<ProductTable, Integer> cartQuantityColumn;
    @FXML
    private TableView<ProductTable> cartTableView;
    @FXML
    private Label emptyCart, totalValueLabel, titleLabel;
    @FXML
    private ScrollPane paymentMethodsPane;
    @FXML
    private Button clearCartButton;


    @FXML
    public void initialize() {
        prepareScene();
        createGoBackButton(event -> switchScene(event, clientScene));
        createClearCartButton();
        try {
            displayProducts();
            setTotalValueLabel();
            setPaymentMethods();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createClearCartButton() {
        clearCartButton = createButton("delete.png", 800, 80);
        clearCartButton.setText("  Delete whole cart");
        clearCartButton.setId("clearCartButton");
        clearCartButton.setMinWidth(200);
        clearCartButton.setOnAction(event -> {
            Optional<ButtonType> buttonCLicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "", "Delete", "Do you want to delete whole cart ?");
            if (alertButtonClicked(buttonCLicked, ButtonType.OK)) {
                clearShoppingCart();
                reloadTableView(cartTableView);
            }
        });
    }

    @Override
    protected void showOnlyRowsWithData(TableView<?> tableView) {
        tableView.setMaxHeight(320);
        tableView.setFixedCellSize(70);
        tableView.prefHeightProperty().bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(50));
    }

    void setTotalValueLabel() throws SQLException {
        checkConnectionWithDb();
        this.totalOrderValue = Client.getTotalValueOfShoppingCart(CURRENT_USER_LOGIN, getConnection());
        totalValueLabel.setText(totalOrderValue + CURRENCY);
        totalValueLabel.setId("displayLabel");
    }

    void displayProducts() throws SQLException {
        checkConnectionWithDb();
        ResultSet products = Product.getProductFromCartAndSetValueBasedOnQuantity(getConnection(), CURRENT_USER_LOGIN);
        assert products != null;
        ObservableList<ProductTable> listOfProducts = ProductTable.getProductsFromShoppingCart(products);
        if (listOfProducts.isEmpty()) {
            displayLabelWithGivenText(emptyCart, "SHOPPING CART IS EMPTY");
            cartTableView.setVisible(false);
            totalValueLabel.setVisible(false);
            titleLabel.setVisible(false);
            clearCartButton.setVisible(false);
        } else {
            cartTableView.setVisible(true);
            clearCartButton.setVisible(true);
            fillShoppingCartColumnsWithData(listOfProducts);
            cartTableView.setItems(listOfProducts);
            showOnlyRowsWithData(cartTableView);

        }

    }

    private void fillShoppingCartColumnsWithData(ObservableList<ProductTable> list) {
        cartNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));
        cartValueColumn.setCellValueFactory(new PropertyValueFactory<>("productTotalValue"));
        plusButtonColumn.setCellFactory(buttonCreation -> plusButtonClicked());
        minusButtonColumn.setCellFactory(buttonCreation -> minusButtonClicked());
        deleteButtonColumn.setCellFactory(buttonCreation -> deleteButtonClicked());
        cartTableView.setItems(list);


    }

    private void reloadTableView(TableView<ProductTable> tableView) {
        tableView.getItems().clear();
        try {
            displayProducts();
            setTotalValueLabel();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void confirmationAlert(String productName, String productQuantity) throws SQLException {
        Optional<ButtonType> buttonClicked;
        if (productQuantity.compareTo("1") == 0) {
            buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "DELETING PRODUCT FROM CART",
                    "Confirmation",
                    "Do you want to delete '" + productName + "' from cart");
        } else {
            buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "DELETING PRODUCT FROM CART",
                    "Confirmation",
                    "Do you want to delete " + productQuantity + " pieces of '" + productName + "' from cart");
        }
        if (alertButtonClicked(buttonClicked, ButtonType.OK)) {
            Client.setQuantityOfProduct(CURRENT_USER_LOGIN, productName, "-quantity", getConnection());
            // there is a trigger in database which deletes products from cart when quantity is equal 0
        }
    }

    private ClientController.ButtonInsideTableColumn<ProductTable, String> plusButtonClicked() {
        ClientController.ButtonInsideTableColumn<ProductTable, String> button = new ClientController().new ButtonInsideTableColumn<>("plus.png", "");
        EventHandler<MouseEvent> buttonClicked = mouseEvent -> {
            String productName = button.getRowId().getProductName();
            try {
                checkConnectionWithDb();
                Client.setQuantityOfProduct(CURRENT_USER_LOGIN, productName, "+1", getConnection());
                reloadTableView(cartTableView);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        };
        button.setEventHandler(buttonClicked);
        button.setCssClassId("clientTableviewButtons");
        button.setStyle("-fx-alignment : center-right;");
        return button;
    }

    private boolean isQuantityEqualOne(String productName, Connection connection) throws SQLException {
        return Client.getQuantityOfProductInCart(CURRENT_USER_LOGIN, productName, connection) == 1;
    }


    private ClientController.ButtonInsideTableColumn<ProductTable, String> minusButtonClicked() {
        ClientController.ButtonInsideTableColumn<ProductTable, String> button = new ClientController().new ButtonInsideTableColumn<>("minus.png", "");
        EventHandler<MouseEvent> buttonClicked = mouseEvent -> {
            String productName = button.getRowId().getProductName();
            try {
                checkConnectionWithDb();
                if (isQuantityEqualOne(productName, getConnection())) {
                    confirmationAlert(productName, "1");
                } else {
                    Client.setQuantityOfProduct(CURRENT_USER_LOGIN, productName, "-1", getConnection());
                }
                reloadTableView(cartTableView);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        };
        button.setEventHandler(buttonClicked);
        button.setCssClassId("clientTableviewButtons");
        button.setStyle("-fx-alignment : center-right;");
        return button;
    }

    private ClientController.ButtonInsideTableColumn<ProductTable, String> deleteButtonClicked() {
        ClientController.ButtonInsideTableColumn<ProductTable, String> button = new ClientController().new ButtonInsideTableColumn<>("delete.png", "delete from cart");
        EventHandler<MouseEvent> buttonClicked = mouseEvent -> {
            String productName = button.getRowId().getProductName();
            try {
                confirmationAlert(productName, Client.getQuantityOfProductInCart(CURRENT_USER_LOGIN, productName, getConnection()) + "");
                reloadTableView(cartTableView);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }


        };
        button.setCssClassId("clientTableviewButtons");
        button.setEventHandler(buttonClicked);
        return button;
    }

    private void setPaymentMethods() throws SQLException {
        int numberOfButtonsInLine = 2;
        double buttonPadding = 20;
        ResultSet paymentMethods = Product.getPaymentMethods(getConnection());
        ToggleGroup groupOfRadioButtons = new ToggleGroup();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(buttonPadding));
        grid.setHgap(buttonPadding);
        grid.setVgap(buttonPadding);
        int j = 0;
        int i = 0;
        while (Objects.requireNonNull(paymentMethods).next()) {
            RadioButton button = new RadioButton(paymentMethods.getString(2));
            button.setToggleGroup(groupOfRadioButtons);
            button.setOnAction(event -> this.paymentMethod = button.getText());
            grid.add(button, i, j);
            if (i < numberOfButtonsInLine - 1) {
                i++;
            } else {
                i = 0;
                j++;
            }
        }
        paymentMethodsPane.setContent(grid);
    }

    protected Optional<ButtonType> createAndShowAlert(ButtonType buttonType1, ButtonType buttonType2) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", buttonType1, buttonType2);
        alert.setHeaderText("Do you want to pay now ?");
        alert.setTitle("Payment");
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconsUrl + "transparentLogo.png"));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        dialogPane.getStyleClass().add("alert");
        return alert.showAndWait();
    }

    @FXML
    void placeOrderButtonClicked() {
        if (paymentMethod == null) {
            createAndShowAlert(Alert.AlertType.WARNING,
                    "Payment",
                    "Payment method required", "You have to choose payment method before placing an order!");

        } else {
            Optional<ButtonType> buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION,
                    "Are you sure about placing an order ?",
                    "Placing an order",
                    "Total order value : " + totalOrderValue + CURRENCY);
            if (alertButtonClicked(buttonClicked, ButtonType.OK)) {

                ButtonType now = new ButtonType("I want to pay now");
                ButtonType later = new ButtonType("I want to pay later");
                Optional<ButtonType> buttonTypeClicked = createAndShowAlert(
                        now, later);
                if (alertButtonClicked(buttonTypeClicked, now)) {
                    placeOrder("In progress");

                } else if (alertButtonClicked(buttonTypeClicked, later)) {
                    placeOrder("Waiting for payment");
                }
                showNotification(createNotification(new Label("Order placed")), 3500);
                clearShoppingCart();
                reloadTableView(cartTableView);
            }
        }
    }

    private boolean alertButtonClicked(Optional<ButtonType> alertButton, ButtonType buttonType) {
        return alertButton.isPresent() && alertButton.get() == buttonType;
    }


    private void placeOrder(String orderStatusName) {

        Order order = new Order(paymentMethod, orderStatusName.toLowerCase(), CURRENT_USER_LOGIN);
        try {
            order.createOrder(getConnection());
            order.setOrderId(getConnection());
            order.setOrderProducts(getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearShoppingCart() {
        try {
            Client.deleteWholeCart(CURRENT_USER_LOGIN, getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
