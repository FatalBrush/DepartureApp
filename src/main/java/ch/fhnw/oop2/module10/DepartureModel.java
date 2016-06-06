package ch.fhnw.oop2.module10;

import com.sun.corba.se.impl.orbutil.graph.Node;
import com.sun.deploy.util.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;


import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Seb on 12.05.2016.
 */
public class DepartureModel {
    private final String FILE_NAME = "olten.csv";
    private final String COL_SEPARATOR = ";";
    private final int MAX_TRAIN_NUMBER = 2000;
    private final int MAX_RAIL_NUMBER = 100;
    private final boolean LOCATED_IN_SAME_FOLDER = false;
    private long lastDepartureEntryId = 0; // verwendet für neue Einträge in departureEntries
    private StringProperty applicationTitle = new SimpleStringProperty("DepartureApp");

    // Flags zur Überwachung von Werte einer Departure
    private boolean departureTimeSelectionOnly = true;
    private boolean toLocationSelectionOnly = true;
    private boolean trainNumberSelectionOnly = true;
    private boolean railNumberSelectionOnly = true;
    private boolean stopOverSelectionOnly = true;

    private final ObservableList<DepartureEntry> departureEntries = FXCollections.observableArrayList(); // Für TableView verwendet
    private final FilteredList<DepartureEntry> filteredDepartureEntries = new FilteredList<DepartureEntry>(departureEntries, p -> true); //Für die Suche
    private final SortedList<DepartureEntry> sortedDepartureEntries = new SortedList<DepartureEntry>(filteredDepartureEntries); // Für die Suche
    private final ObservableList<Command> undoStack = FXCollections.observableArrayList();
    private final ObservableList<Command> redoStack = FXCollections.observableArrayList();

    private final ObjectProperty<DepartureEntry> selectedDeparture = new SimpleObjectProperty<>();
    private final HashMap<String, Integer> fieldsWithErrors = new HashMap<>();
    private final BooleanProperty contentNotSaveable = new SimpleBooleanProperty(); // False = Inhalt kann gespeichert werden
    private final BooleanProperty undoDisabled = new SimpleBooleanProperty();
    private final BooleanProperty redoDisabled = new SimpleBooleanProperty();
    private final DepartureEntry departureEntryProxy = new DepartureEntry();
    private final StringProperty totalAmountOfEntries = new SimpleStringProperty();
    private final StringProperty currentlyShownAmountOfEntries = new SimpleStringProperty();

    private final ChangeListener propertyChangeListenerForUndoSupport = (observable, oldValue, newValue) -> {
        redoStack.clear(); // Klassisches Verhalten von Redo, wenn erneut geändert wird
        // Änderung zuoberst auf undoStack hinzufügen
        undoStack.add(0, new ValueChangeCommand(DepartureModel.this, (Property) observable, oldValue, newValue));
    };

    /**
     * Constructor des Models
     */
    public DepartureModel(){
        departureEntries.addAll(readCSVDepartureFile(LOCATED_IN_SAME_FOLDER));
        createProxyBindings();
        setTotalAmountOfEntries(String.valueOf(departureEntries.size()));
        undoDisabled.bind(Bindings.isEmpty(undoStack));
        redoDisabled.bind(Bindings.isEmpty(redoStack));
    }

    /**
     * Entfernt alte Bindings und erstellt neue Bindings mit dem Proxy-Objekt.
     */
    public void createProxyBindings(){
        selectedDeparture.addListener((observable, oldValue, newValue) -> {
            // alte Bindings entfernen
            if(oldValue != null){
                departureEntryProxy.time24hFormatProperty().unbindBidirectional(oldValue.time24hFormatProperty());
                departureEntryProxy.destinationStationProperty().unbindBidirectional(oldValue.destinationStationProperty());
                departureEntryProxy.trainNumberProperty().unbindBidirectional(oldValue.trainNumberProperty());
                departureEntryProxy.railNumberProperty().unbindBidirectional(oldValue.railNumberProperty());
                departureEntryProxy.stopOverStationsProperty().unbindBidirectional(oldValue.stopOverStationsProperty());
                disableUndoSupport(oldValue);
            }

            // neue Bindings erstellen
            if(newValue != null){
                departureEntryProxy.time24hFormatProperty().bindBidirectional(newValue.time24hFormatProperty());
                departureEntryProxy.destinationStationProperty().bindBidirectional(newValue.destinationStationProperty());
                departureEntryProxy.trainNumberProperty().bindBidirectional(newValue.trainNumberProperty());
                departureEntryProxy.railNumberProperty().bindBidirectional(newValue.railNumberProperty());
                departureEntryProxy.stopOverStationsProperty().bindBidirectional(newValue.stopOverStationsProperty());
                enableUndoSupport(newValue);
            }
        });
    }

    /**
     * Entfernt Listeners von einer DepartureEntry und somit den Undo-Support.
     * @param departureEntry entsprechendes departure entry
     */
    private void disableUndoSupport(DepartureEntry departureEntry){
        departureEntry.time24hFormatProperty().removeListener(propertyChangeListenerForUndoSupport);
        departureEntry.destinationStationProperty().removeListener(propertyChangeListenerForUndoSupport);
        departureEntry.trainNumberProperty().removeListener(propertyChangeListenerForUndoSupport);
        departureEntry.railNumberProperty().removeListener(propertyChangeListenerForUndoSupport);
        departureEntry.stopOverStationsProperty().removeListener(propertyChangeListenerForUndoSupport);
    }

    /**
     * Fügt Listeners einer DepartureEntry hinzu und somit den Undo-Support.
     * @param departureEntry entsprechendes departure entry
     */
    private void enableUndoSupport(DepartureEntry departureEntry){
        departureEntry.time24hFormatProperty().addListener(propertyChangeListenerForUndoSupport);
        departureEntry.destinationStationProperty().addListener(propertyChangeListenerForUndoSupport);
        departureEntry.trainNumberProperty().addListener(propertyChangeListenerForUndoSupport);
        departureEntry.railNumberProperty().addListener(propertyChangeListenerForUndoSupport);
        departureEntry.stopOverStationsProperty().addListener(propertyChangeListenerForUndoSupport);
    }

    /**
     * Holt den Dateipfad für eine Datei.
     * @param fileName Name der Datei
     * @param locatedInSameFolder Falls im gleichen Ordner true, In resources = false
     * @return DateiPfad
     */
    private Path getCSVPath(String fileName, boolean locatedInSameFolder){
        try{
            if(!locatedInSameFolder){
                fileName = "/" + fileName;
            }
            return Paths.get(getClass().getResource(fileName).toURI());
        } catch (URISyntaxException e){
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gibt einen String-Stream einer Datei zurück.
     * @param filename Name der Datei
     * @param locatedInSameFolder Falls im gleichen Ordner true, ansonsten in resources = false
     * @return String Stream
     */
    private Stream<String> getStreamOfLinesFromCSV(String filename, boolean locatedInSameFolder){
        try{
            return Files.lines(this.getCSVPath(filename, locatedInSameFolder), StandardCharsets.UTF_8);
        } catch (IOException e){
            throw new IllegalStateException(e);
        }
    }

    /**
     * Erstellt eine Liste von DepartureEntries ausgehend von der CSV Datei.
     * @param locatedInSameFolder true falls CSV im gleichen Ordner, false bei resource
     * @return ArrayList mit DepartureEntries
     */
    private List<DepartureEntry> readCSVDepartureFile(boolean locatedInSameFolder){
        try(Stream<String> stream = getStreamOfLinesFromCSV(FILE_NAME, locatedInSameFolder)){
            return stream.skip(1) // Erste Zeile überspringen
                    .map(s -> new DepartureEntry(s.split(COL_SEPARATOR)))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Gibt ein neues departure entry zurück mit initialen Daten.
     * @return neues departure entry.
     */
    public DepartureEntry getNewDepartureEntry() {
        String[] entry = new String[6];
        entry[0] = String.valueOf(this.getNewDepartureEntryId(LOCATED_IN_SAME_FOLDER));
        entry[1] = "00:00";
        entry[2] = "ICN 01";
        entry[3] = "Zürich HB";
        entry[4] = "Olten  00:00 - Aarau  00:08 - Zürich HB  00:33";
        entry[5] = "-1";
        String state = "Neu";
        DepartureEntry newDepartureEntry = new DepartureEntry(entry, state);
        return newDepartureEntry;
    }

    /**
     * Löscht einen selektierten Eintrag in der TableView.
     */
    public void removeDeparture() {
        departureEntries.remove(getSelectedDeparture());
        setTotalAmountOfEntries(String.valueOf(departureEntries.size()));
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Generiert die nächste freie ID für ein DepartureEntry.
     * @param locatedInSameFolder true falls CSV mit allen Entries im gleichen Ordner ist.
     * @return neue ID.
     */
    public long getNewDepartureEntryId(boolean locatedInSameFolder){
        if(lastDepartureEntryId == 0){ // neue ID noch zu holen...
            List<DepartureEntry> departureEntries = readCSVDepartureFile(locatedInSameFolder);
            lastDepartureEntryId = departureEntries.get(departureEntries.size()-1).getEntryId();
        } else {
            lastDepartureEntryId++;
        }
        return lastDepartureEntryId;
    }

    /**
     * Fügt der ObservableList departureEntries einen neuen Eintrag hinzu.
     */
    public void addNewDepartureEntry() {
        departureEntries.add(getNewDepartureEntry());
        setTotalAmountOfEntries(String.valueOf(departureEntries.size()));
    }



    /**
     * Speichert departureEntries in die Datei olten.csv
     */
    public void saveDepartureEntries() {
        try{
            BufferedWriter fileWriter = Files.newBufferedWriter(getCSVPath(FILE_NAME, LOCATED_IN_SAME_FOLDER));
            // erste Linie schreiben mit den Spalten
            fileWriter.write("#id"+COL_SEPARATOR+"Uhrzeit"+COL_SEPARATOR+"Zugnummer"+COL_SEPARATOR+"in Richtung"+COL_SEPARATOR+"über"+COL_SEPARATOR+"Gleis"+COL_SEPARATOR);
            fileWriter.newLine();
            // ObservableList holen und jeden Eintrag schreiben
            departureEntries.stream().forEach(entry -> {
                try {
                    fileWriter.write(entry.getEntryAsStringLine());
                    fileWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileWriter.close();
        } catch (IOException e){
            throw new IllegalStateException("Speichern fehlgeschlagen");
        }
        // File neu einlesen um die Icons zu aktualisieren
        departureEntries.clear();
        departureEntries.addAll(readCSVDepartureFile(LOCATED_IN_SAME_FOLDER));
        // Stacks löschen für Undo / Redo
        undoStack.clear();
        redoStack.clear();
    }
    
    /**
     * Validiert die Abfahrtszeit.
     * @param departureTime Abfahrtszeit welche in XX:YY Format sein sollte.
     * @return true wenn OK
     */
    public boolean validateDepartureTime(String departureTime){
        String field = "departureTime";
        try{
            if((departureTime.length() - departureTime.replace(":", "").length()) != 1){
                // enthält mehr als ein :
                addFieldWithError(field);
                return false;
            }
            String[] splitDepartureTime = departureTime.split(":");
            if(splitDepartureTime.length == 2 &&
                    Integer.valueOf(splitDepartureTime[0]) < 24 &&
                    Integer.valueOf(splitDepartureTime[0]) > -1 &&
                    splitDepartureTime[0].length()== 2 &&
                    Integer.valueOf(splitDepartureTime[1]) < 60 &&
                    Integer.valueOf(splitDepartureTime[1]) > -1 &&
                    splitDepartureTime[1].length()== 2){
                removeFieldWithError(field);
                return true;
            } else {
                addFieldWithError(field);
                return false;
            }
        } catch (Exception e){
        }
        addFieldWithError(field);
        return false;
    }

    /**
     * Validiert das Ziel einer Abfahrt.
     * @param destination Ort welcher nicht leer sein darf und nur Buchstaben enthalten sollte (Zeichen . - ( ) / erlaubt)
     */
    public boolean validateToLocation(String destination){
        String field = "toLocation";
        // darf nicht leer sein
        if(destination.isEmpty()){
            addFieldWithError(field);
            return false;
        }
        // darf nur Buchstaben enthalten
        for(int i = 0; i < destination.length(); i++){
            if(!Character.isLetter(destination.codePointAt(i))){
                // Spezielle Zeichen trotzdem erlauben
                if(!(destination.charAt(i) == ' ') &&
                        !(destination.charAt(i) == '.') &&
                        !(destination.charAt(i) == '-') &&
                        !(destination.charAt(i) == '(') &&
                        !(destination.charAt(i) == ')') &&
                        !(destination.charAt(i) == '/')){
                    addFieldWithError(field);
                    return false;
                }
            }
        }
        removeFieldWithError(field);
        return true;
    }

    /**
     * Validiert die Zugnummer einer Abfahrt.
     * @param trainNumber Zugnummer die mit IC, EC, ICN, oder ICE beginnt, gefolgt von einer Nummer 0 < x < MAX_TRAIN_NUMBER (führende Nullen erlaubt)
     */
    public boolean validateTrainNumber(String trainNumber){
        String field = "trainNumber";
        // darf nicht leer sein
        if(trainNumber.equals("")){
            addFieldWithError(field);
            return false;
        }
        try{
            String [] tmp = trainNumber.split("\\s+");
            // Darf nur zwei Glieder im Array geben, ansonsten zu viel Abstand!
            if(tmp.length != 2){
                addFieldWithError(field);
                return false;
            }
            // Zugbuchstaben prüfen
            if(tmp[0].equals("IC") || tmp[0].equals("EC") || tmp[0].equals("ICN") || tmp[0].equals("ICE")){
                // OK
            } else {
                addFieldWithError(field);
                return false;
            }
            // Zugnummer prüfen
            if(Integer.valueOf(tmp[1])>0 && Integer.valueOf(tmp[1]) < MAX_TRAIN_NUMBER){
                // OK
            } else {
                addFieldWithError(field);
                return false;
            }
        } catch (Exception e){
            addFieldWithError(field);
            return false;
        }
        removeFieldWithError(field);
        return true;
    }

    /**
     * Validiert die Gleisnummer einer Abfahrt.
     * @param railNumber darf "-" oder eine Zahl 0 < x < MAX_RAIL_NUMBER sein, jedoch nicht leer.
     * @return true falls OK
     */
    public boolean validateRailNumber(String railNumber){
        String field = "railNumber";
        if(railNumber.equals("")){
            addFieldWithError(field);
            return false;
        }
        if(railNumber.equals("-")){
            removeFieldWithError(field);
            return true;
        }
        if(Integer.valueOf(railNumber) > 0 && Integer.valueOf(railNumber) < MAX_RAIL_NUMBER){
            removeFieldWithError(field);
            return true;
        }
        addFieldWithError(field);
        return false;
    }

    /**
     * Validiert die Zwischenhalte.
     * @param stopOvers Zwischenhalte darf nicht leer sein.
     * @return true falls Ok
     */
    public boolean validateStopOvers(String stopOvers){
        String field = "stopOvers";
        if(stopOvers.equals("")){
            addFieldWithError(field);
            return false;
        }
        removeFieldWithError(field);
        return true;
    }

    /**
     * Fügt dem HashMap fieldsWithErrors ein Element hinzu.
     * @param fieldName Name des Eingabefelds welches ein Fehler hat
     */
    public void addFieldWithError(String fieldName){
        this.fieldsWithErrors.put(fieldName, 1); // Die 1 ist irrelevant
        checkContentNotSaveable();
    }

    /**
     * Entfernt ein Element aus dem HashMap fieldsWithErros da kein Fehler mehr in diesem Feld.
     * @param fieldName name des Feldes
     */
    public void removeFieldWithError(String fieldName){
        this.fieldsWithErrors.remove(fieldName);
        checkContentNotSaveable();
    }

    /**
     * Prüft ob das HashMap fieldsWithErros keine Elemente hat und setzt entsprechend den Status.
     */
    public void checkContentNotSaveable(){
        if(fieldsWithErrors.isEmpty()){
            // Keine Fehler vorhanden
            setContentNotSaveable(false);
        } else {
            setContentNotSaveable(true); // true da Inhalt NICHT gespeichert werden kann
        }
    }

    /**
     * Sucht Elemente und setzt entsprechend die filteredDepartureEntries.
     * @param searchKey String nach dem gesucht wird in Spalte Zielort und Zwischenhalte
     */
    public void searchTerm(String searchKey){
        // Prädikat für die Filtered List setzen
        getFilteredDepartureEntries().setPredicate(departureEntry -> {
            String lowCaseSearchKey = searchKey.toLowerCase();

            if(lowCaseSearchKey.equals("")){
                // Nichts zu filtern
                return true; // Alles anzeigen
            }

            if(departureEntry.getDestinationStation().toLowerCase().contains(lowCaseSearchKey)){
                return true;
            }
            return false; // Kein Match gefunden
        });
    }

    /**
     * Setzt das selektierte Departure als "geändert".
     */
    public void markSelectedDepartureAsChanged(){
        this.getSelectedDeparture().setIconColLED("Geändert");
    }

    /**
     * Setzt eine Wertänderung eines Commands um und hebt hierfür zwischenzeitig die Listeners auf.
     * @param property entsprechendes Property welches zu ändern ist.
     * @param newValue Wert für dieses Property
     * @param <T>
     */
    public <T> void setPropertyValueWithoutUndoSupport(Property<T> property, T newValue){
        property.removeListener(propertyChangeListenerForUndoSupport);
        property.setValue(newValue);
        property.addListener(propertyChangeListenerForUndoSupport);
    }


    /**
     * Holt Command aus undoStack und führt diesen aus.
     */
    public void undo(){
        if(undoStack.isEmpty()){
            return;
        }

        Command cmd = undoStack.get(0);
        undoStack.remove(0);
        redoStack.add(0, cmd);

        cmd.redo();
    }

    /**
     * Holt Command aus redoStack und führt diesen aus.
     */
    public void redo(){
        if(redoStack.isEmpty()){
            return;
        }

        Command cmd = redoStack.get(0);
        redoStack.remove(0);
        undoStack.add(0, cmd);

        cmd.redo();
    }

    /**
     * Getter and Setter
     */
    public ObservableList<DepartureEntry> getDepartureEntries() {
        return departureEntries;
    }

    public String getTitle() {
        return applicationTitle.get();
    }

    public StringProperty titleProperty() {
        return applicationTitle;
    }

    public void setTitle(String title) {
        this.applicationTitle.set(title);
    }

    public DepartureEntry getSelectedDeparture() {
        return selectedDeparture.get();
    }

    public ObjectProperty<DepartureEntry> selectedDepartureProperty() {
        return selectedDeparture;
    }

    public void setSelectedDeparture(DepartureEntry selectedDeparture) {
        // Flags setzten, weil nur Selektion erfolgt ist
        departureTimeSelectionOnly = true;
        toLocationSelectionOnly = true;
        trainNumberSelectionOnly = true;
        railNumberSelectionOnly = true;
        stopOverSelectionOnly = true;
        this.selectedDeparture.set(selectedDeparture);
    }

    public HashMap<String, Integer> getFieldsWithErrors() {
        return fieldsWithErrors;
    }

    public boolean getContentNotSaveable() {
        return contentNotSaveable.get();
    }

    public BooleanProperty contentNotSaveableProperty() {
        return contentNotSaveable;
    }

    public void setContentNotSaveable(boolean contentNotSaveable) {
        this.contentNotSaveable.set(contentNotSaveable);
    }

    public DepartureEntry getDepartureEntryProxy() {
        return departureEntryProxy;
    }

    public int getMAX_TRAIN_NUMBER() {
        return MAX_TRAIN_NUMBER;
    }

    public int getMAX_RAIL_NUMBER() {
        return MAX_RAIL_NUMBER;
    }

    public FilteredList<DepartureEntry> getFilteredDepartureEntries() {
        return filteredDepartureEntries;
    }

    public SortedList<DepartureEntry> getSortedDepartureEntries() {
        return sortedDepartureEntries;
    }

    public String getTotalAmountOfEntries() {
        return totalAmountOfEntries.get();
    }

    public StringProperty totalAmountOfEntriesProperty() {
        return totalAmountOfEntries;
    }

    public void setTotalAmountOfEntries(String totalAmountOfEntries) {
        this.totalAmountOfEntries.set(totalAmountOfEntries);
    }

    public String getCurrentlyShownAmountOfEntries() {
        return currentlyShownAmountOfEntries.get();
    }

    public StringProperty currentlyShownAmountOfEntriesProperty() {
        return currentlyShownAmountOfEntries;
    }

    public void setCurrentlyShownAmountOfEntries(String currentlyShownAmountOfEntries) {
        this.currentlyShownAmountOfEntries.set(currentlyShownAmountOfEntries);
    }

    public boolean isDepartureTimeSelectionOnly() {
        return departureTimeSelectionOnly;
    }

    public void setDepartureTimeSelectionOnly(boolean departureTimeSelectionOnly) {
        this.departureTimeSelectionOnly = departureTimeSelectionOnly;
    }

    public boolean isToLocationSelectionOnly() {
        return toLocationSelectionOnly;
    }

    public void setToLocationSelectionOnly(boolean toLocationSelectionOnly) {
        this.toLocationSelectionOnly = toLocationSelectionOnly;
    }

    public boolean isTrainNumberSelectionOnly() {
        return trainNumberSelectionOnly;
    }

    public void setTrainNumberSelectionOnly(boolean trainNumberSelectionOnly) {
        this.trainNumberSelectionOnly = trainNumberSelectionOnly;
    }

    public boolean isRailNumberSelectionOnly() {
        return railNumberSelectionOnly;
    }

    public void setRailNumberSelectionOnly(boolean railNumberSelectionOnly) {
        this.railNumberSelectionOnly = railNumberSelectionOnly;
    }

    public boolean isStopOverSelectionOnly() {
        return stopOverSelectionOnly;
    }

    public void setStopOverSelectionOnly(boolean stopOverSelectionOnly) {
        this.stopOverSelectionOnly = stopOverSelectionOnly;
    }

    public boolean getUndoDisabled() {
        return undoDisabled.get();
    }

    public BooleanProperty undoDisabledProperty() {
        return undoDisabled;
    }

    public void setUndoDisabled(boolean undoDisabled) {
        this.undoDisabled.set(undoDisabled);
    }

    public boolean getRedoDisabled() {
        return redoDisabled.get();
    }

    public BooleanProperty redoDisabledProperty() {
        return redoDisabled;
    }

    public void setRedoDisabled(boolean redoDisabled) {
        this.redoDisabled.set(redoDisabled);
    }
}
