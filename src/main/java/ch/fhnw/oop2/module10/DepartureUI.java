package ch.fhnw.oop2.module10;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * Created by Seb on 12.05.2016.
 */
public class DepartureUI extends BorderPane {
    private final DepartureModel departurePM;
    private SplitPane contentSplitPane = new SplitPane();
    /* Attribute für den Editor-Bereich rechts editorPane*/
    private GridPane editorPane = new GridPane();
    private Label departure;
    private TextField departureTimeText;
    private Label toLocation;
    private TextField toLocationText;
    private Label trainNumber;
    private TextField trainNumberText;
    private Label railNumber;
    private TextField railNumberText;
    private Label stopover;
    private TextArea stopoverArea;
    /* Attribute für die Leiste oben topPane*/
    private HBox topPane = new HBox();
    private Button saveButton;
    private Button addNew;
    private Button removeSelected;
    private Button undo;
    private Button redo;
    private Button train;
    private Button flipDisplay;
    private Button breakDisplay;
    private Button playDisplay;
    private TextField searchInput;
    /* Attribute für den Bereich links leftVBox*/
    private VBox leftVBox = new VBox();
    private TableView<DepartureEntry> leftTableView;
    private Label elementCounter;

    public DepartureUI(DepartureModel departurePM){
        this.departurePM = departurePM;
        leftTableView = getTableView();
        initializeControls();
        layoutControls();
        addEventHandlers();
        addValueChangeListeners();
        addBindings();
    }

    private void initializeControls(){
        /* editorPane Controls initialisieren */
        this.departure =  new Label("Abfahrt");
        this.departureTimeText = new TextField(); // bsp. 00:35
        this.toLocation = new Label("nach");
        this.toLocationText = new TextField(); // bsp. Zürich HB
        this.trainNumber = new Label("Zugnummer");
        this.trainNumberText = new TextField(); // bsp. ICN 1549
        this.railNumber = new Label("Gleis");
        this.railNumberText = new TextField(); // bsp. 2
        this.stopover = new Label("Zwischenhalte");
        this.stopoverArea = new TextArea(); // bsp. Olten 00:35 - Aarau 00:43 ...

        /* topPane Controls initialisieren */
        Image iconSaveButton = new Image(getClass().getResourceAsStream("/icon_save.PNG"));
        saveButton = new Button("", new ImageView(iconSaveButton));

        Image iconAddNew = new Image(getClass().getResourceAsStream("/icon_new.png"));
        addNew = new Button("", new ImageView(iconAddNew));

        Image iconRemoveSelected = new Image(getClass().getResourceAsStream("/icon_remove.png"));
        removeSelected = new Button("", new ImageView(iconRemoveSelected));

        Image iconUndo = new Image(getClass().getResourceAsStream("/icon_undo.png"));
        undo = new Button("", new ImageView(iconUndo));

        Image iconRedo = new Image(getClass().getResourceAsStream("/icon_redo.png"));
        redo = new Button("", new ImageView(iconRedo));

        Image iconTrain = new Image(getClass().getResourceAsStream("/icon_train.png"));
        train = new Button("", new ImageView(iconTrain));

        Image iconFlip = new Image(getClass().getResourceAsStream("/icon_flip-display.png"));
        flipDisplay = new Button("", new ImageView(iconFlip));

        Image iconBreak = new Image(getClass().getResourceAsStream("/icon_break.png"));
        breakDisplay = new Button("", new ImageView(iconBreak));

        Image iconPlay = new Image(getClass().getResourceAsStream("/icon_play.png"));
        playDisplay = new Button("", new ImageView(iconPlay));

        searchInput = new TextField();

        /* leftVBox Controls */
        this.elementCounter = new Label("Anzahl Elemente");
    }

    private void layoutControls(){
        /* editorPane layouten */
        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setMinWidth(150);
        colLabel.setMaxWidth(150);
        ColumnConstraints colInputFields = new ColumnConstraints();
        colInputFields.setHgrow(Priority.ALWAYS);
        colInputFields.setMinWidth(270);
        editorPane.getColumnConstraints().addAll(colLabel, colInputFields);

        RowConstraints rc = new RowConstraints();
        rc.setVgrow(Priority.ALWAYS);
        editorPane.getRowConstraints().addAll(rc, rc, rc, rc, rc, rc, rc);

        editorPane.add(departure, 0, 0);
        editorPane.add(departureTimeText, 1, 0);
        editorPane.add(toLocation, 0, 1);
        editorPane.add(toLocationText, 1, 1);
        editorPane.add(trainNumber, 0, 2);
        editorPane.add(trainNumberText, 1, 2);
        editorPane.add(railNumber, 0, 3);
        editorPane.add(railNumberText, 1, 3);
        editorPane.add(stopover, 0, 4);
        editorPane.add(stopoverArea, 1, 4);

        /* topPane layouten */
        topPane.getStyleClass().add("topPane");

        Tooltip saveButtonTooltip = new Tooltip("Speichern");
        saveButton.setTooltip(saveButtonTooltip);

        Tooltip addNewToolTip = new Tooltip("Neuen Eintrag hinzufügen");
        addNew.setTooltip(addNewToolTip);

        Tooltip removeSelectedToolTip = new Tooltip("Selektierten Eintrag löschen");
        removeSelected.setTooltip(removeSelectedToolTip);

        Tooltip undoToolTip = new Tooltip("Schritt zurück");
        undo.setTooltip(undoToolTip);

        Tooltip redoToolTip = new Tooltip("Schritt nach vorne");
        redo.setTooltip(redoToolTip);

        Tooltip searchInputToolTip = new Tooltip("Geben Sie einen Suchbegriff ein");
        searchInput.setTooltip(searchInputToolTip);

        topPane.getChildren().addAll(saveButton, addNew, removeSelected, undo, redo, train, flipDisplay, breakDisplay, playDisplay, searchInput);

        /* leftVBox layouten */
        leftVBox.setMinWidth(315);
        leftVBox.getChildren().addAll(leftTableView, elementCounter);
        leftVBox.setVgrow(leftTableView, Priority.ALWAYS);
        leftVBox.setVgrow(elementCounter, Priority.NEVER);

        /* BorderPane & SplitPane layouten */
        contentSplitPane.getItems().addAll(leftVBox, editorPane);
        contentSplitPane.setDividerPosition(0, 0.1); // Damit der Divider nach links geht
        this.setCenter(contentSplitPane);
        this.setTop(topPane);
    }

    private void addEventHandlers(){
        /* topPane Events */
        addNew.setOnAction(e -> {
            departurePM.addNewDepartureEntry();
            // gleich nach unten scrollen und neue Selektion setzen
            leftTableView.scrollTo(leftTableView.getItems().size()-1);
            departurePM.setSelectedDeparture(leftTableView.getItems().get(leftTableView.getItems().size()-1));
        });
        removeSelected.setOnAction(e -> departurePM.removeDeparture());
        saveButton.setOnAction(e -> {
            departurePM.saveDepartureEntries();
            // Nach oben scrollen und neue Selektion setzen
            leftTableView.scrollTo(leftTableView.getItems().get(0));
            departurePM.setSelectedDeparture(leftTableView.getItems().get(0));
        });
    }

    private void addValueChangeListeners(){
        /* leftVBox Listeners */
        leftTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> departurePM.setSelectedDeparture(newValue));
        leftTableView.getSelectionModel().select(0); // Initial erste Zeile wählen (wegen der Validierung wichtig!)

        /* topPane Listeners */
        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            departurePM.searchTerm(newValue); // Such-Logik aufrufen
        });

        /*  editorPane Listeners */
        departureTimeText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(departurePM.isDepartureTimeSelectionOnly())){
                departurePM.markSelectedDepartureAsChanged();
                if(departurePM.validateDepartureTime(newValue)){
                    departureTimeText.getStyleClass().removeAll("error");
                } else {
                    departureTimeText.getStyleClass().add("error");
                }
            }
            departurePM.setDepartureTimeSelectionOnly(false); // Setze Flag für nächstes "Listening"
        });
        toLocationText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(departurePM.isToLocationSelectionOnly())){
                departurePM.markSelectedDepartureAsChanged();
                if(departurePM.validateToLocation(newValue)){
                    toLocationText.getStyleClass().removeAll("error");
                } else {
                    toLocationText.getStyleClass().add("error");
                }
            }
            departurePM.setToLocationSelectionOnly(false);
        });
        trainNumberText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(departurePM.isTrainNumberSelectionOnly())){
                departurePM.markSelectedDepartureAsChanged();
                if(departurePM.validateTrainNumber(newValue)){
                    trainNumberText.getStyleClass().removeAll("error");
                } else {
                    trainNumberText.getStyleClass().add("error");
                }
            }
            departurePM.setTrainNumberSelectionOnly(false);
        });
        railNumberText.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(departurePM.isRailNumberSelectionOnly())){
                departurePM.markSelectedDepartureAsChanged();
                if(departurePM.validateRailNumber(newValue)){
                    railNumberText.getStyleClass().removeAll("error");
                } else {
                    railNumberText.getStyleClass().add("error");
                }
            }
            departurePM.setRailNumberSelectionOnly(false);
        });
        stopoverArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(departurePM.isStopOverSelectionOnly())){
                departurePM.markSelectedDepartureAsChanged();
                if(departurePM.validateStopOvers(newValue)){
                    stopoverArea.getStyleClass().removeAll("error");
                } else {
                    stopoverArea.getStyleClass().add("error");
                }
            }
            departurePM.setStopOverSelectionOnly(false);
        });
    }

    private void addBindings(){
        /* editorPane Bindings */
        // Stabile Bindings erstellen mit Proxy
        departureTimeText.textProperty().bindBidirectional(departurePM.getDepartureEntryProxy().time24hFormatProperty());
        toLocationText.textProperty().bindBidirectional(departurePM.getDepartureEntryProxy().destinationStationProperty());
        trainNumberText.textProperty().bindBidirectional(departurePM.getDepartureEntryProxy().trainNumberProperty());
        railNumberText.textProperty().bindBidirectional(departurePM.getDepartureEntryProxy().railNumberProperty());
        stopoverArea.textProperty().bindBidirectional(departurePM.getDepartureEntryProxy().stopOverStationsProperty());

        /* topPane Bindings */
        saveButton.disableProperty().bind(departurePM.contentNotSaveableProperty());

        /* leftVBox Bindings */
        leftTableView.disableProperty().bind(departurePM.contentNotSaveableProperty());

        departurePM.getSortedDepartureEntries().comparatorProperty().bind(leftTableView.comparatorProperty()); // Binding mit Sortierter Liste
        leftTableView.setItems(departurePM.getSortedDepartureEntries()); // Sortierte Inhalte zu TableView hinzufügen

        elementCounter.textProperty().bind(departurePM.totalAmountOfEntriesProperty().concat(" Departures vorhanden"));
    }

    /**
     * Erstellt ein TableView für die Daten aus dem CSV.
     * @return TableView
     */
    public TableView<DepartureEntry> getTableView(){
        TableView<DepartureEntry> leftTable = new TableView<>(departurePM.getDepartureEntries()); // ObservableList aus Modell einlesen

        TableColumn<DepartureEntry, String> iconCol = new TableColumn<>("");
        iconCol.setCellValueFactory(param -> param.getValue().iconColLEDProperty());
        iconCol.setCellFactory(param -> new ColLEDState());

        TableColumn<DepartureEntry, String> departureCol = new TableColumn<>("Abfahrt");
        departureCol.setCellValueFactory(param -> param.getValue().time24hFormatProperty());

        TableColumn<DepartureEntry, String> destinationCol = new TableColumn<>("nach");
        destinationCol.setCellValueFactory(param -> param.getValue().destinationStationProperty());

        TableColumn<DepartureEntry, String> trailCole = new TableColumn<>("Gleis");
        trailCole.setCellValueFactory(param -> param.getValue().railNumberProperty());

        leftTable.getColumns().addAll(iconCol, departureCol, destinationCol, trailCole);

        return leftTable;
    }

    /*
    Getter und Setter
     */

    public DepartureModel getDeparturePM() {
        return departurePM;
    }

    public SplitPane getContentSplitPane() {
        return contentSplitPane;
    }

    public void setContentSplitPane(SplitPane contentSplitPane) {
        this.contentSplitPane = contentSplitPane;
    }

    public GridPane getEditorPane() {
        return editorPane;
    }

    public void setEditorPane(GridPane editorPane) {
        this.editorPane = editorPane;
    }

    public Label getDeparture() {
        return departure;
    }

    public void setDeparture(Label departure) {
        this.departure = departure;
    }

    public TextField getDepartureTimeText() {
        return departureTimeText;
    }

    public void setDepartureTimeText(TextField departureTimeText) {
        this.departureTimeText = departureTimeText;
    }

    public Label getToLocation() {
        return toLocation;
    }

    public void setToLocation(Label toLocation) {
        this.toLocation = toLocation;
    }

    public TextField getToLocationText() {
        return toLocationText;
    }

    public void setToLocationText(TextField toLocationText) {
        this.toLocationText = toLocationText;
    }

    public Label getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Label trainNumber) {
        this.trainNumber = trainNumber;
    }

    public TextField getTrainNumberText() {
        return trainNumberText;
    }

    public void setTrainNumberText(TextField trainNumberText) {
        this.trainNumberText = trainNumberText;
    }

    public Label getRailNumber() {
        return railNumber;
    }

    public void setRailNumber(Label railNumber) {
        this.railNumber = railNumber;
    }

    public TextField getRailNumberText() {
        return railNumberText;
    }

    public void setRailNumberText(TextField railNumberText) {
        this.railNumberText = railNumberText;
    }

    public Label getStopover() {
        return stopover;
    }

    public void setStopover(Label stopover) {
        this.stopover = stopover;
    }

    public TextArea getStopoverArea() {
        return stopoverArea;
    }

    public void setStopoverArea(TextArea stopoverArea) {
        this.stopoverArea = stopoverArea;
    }

    public HBox getTopPane() {
        return topPane;
    }

    public void setTopPane(HBox topPane) {
        this.topPane = topPane;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
    }

    public Button getAddNew() {
        return addNew;
    }

    public void setAddNew(Button addNew) {
        this.addNew = addNew;
    }

    public Button getRemoveSelected() {
        return removeSelected;
    }

    public void setRemoveSelected(Button removeSelected) {
        this.removeSelected = removeSelected;
    }

    public Button getUndo() {
        return undo;
    }

    public void setUndo(Button undo) {
        this.undo = undo;
    }

    public Button getRedo() {
        return redo;
    }

    public void setRedo(Button redo) {
        this.redo = redo;
    }

    public Button getTrain() {
        return train;
    }

    public void setTrain(Button train) {
        this.train = train;
    }

    public Button getFlipDisplay() {
        return flipDisplay;
    }

    public void setFlipDisplay(Button flipDisplay) {
        this.flipDisplay = flipDisplay;
    }

    public Button getBreakDisplay() {
        return breakDisplay;
    }

    public void setBreakDisplay(Button breakDisplay) {
        this.breakDisplay = breakDisplay;
    }

    public Button getPlayDisplay() {
        return playDisplay;
    }

    public void setPlayDisplay(Button playDisplay) {
        this.playDisplay = playDisplay;
    }

    public TextField getSearchInput() {
        return searchInput;
    }

    public void setSearchInput(TextField searchInput) {
        this.searchInput = searchInput;
    }

    public VBox getLeftVBox() {
        return leftVBox;
    }

    public void setLeftVBox(VBox leftVBox) {
        this.leftVBox = leftVBox;
    }

    public TableView<DepartureEntry> getLeftTableView() {
        return leftTableView;
    }

    public void setLeftTableView(TableView<DepartureEntry> leftTableView) {
        this.leftTableView = leftTableView;
    }

    public Label getElementCounter() {
        return elementCounter;
    }

    public void setElementCounter(Label elementCounter) {
        this.elementCounter = elementCounter;
    }
}
