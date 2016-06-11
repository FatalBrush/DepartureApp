package ch.fhnw.oop2.module10;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Seb on 12.05.2016.
 */
public class DepartureEntry {
    private final String COL_SEPARATOR = ";";
    private final IntegerProperty entryId =  new SimpleIntegerProperty();
    private final StringProperty time24hFormat = new SimpleStringProperty();
    private final StringProperty trainNumber = new SimpleStringProperty();
    private final StringProperty destinationStation = new SimpleStringProperty();
    private final StringProperty stopOverStations = new SimpleStringProperty();
    private final StringProperty railNumber = new SimpleStringProperty();
    private final StringProperty iconColLED = new SimpleStringProperty();

    /***
     * Constructor der Klasse mit String[] pro Eintrag.
     * @param entry Array mit einzelnen Werten für einen Eintrag.
     */
    public DepartureEntry(String[] entry){
        this(entry, DepartureState.SAVED.toString());
    }

    /**
     * Constructor der Klasse mit zusätlichem Wert für LED.
     * @param entry Array mit einzelnen Werten für einen Eintrag.
     * @param state Status des Eintrags für LED.
     */
    public DepartureEntry(String[] entry, String state){
        setEntryId(Integer.valueOf(entry[0]));
        setTime24hFormat(entry[1]);
        setTrainNumber(entry[2]); // Merke dass Nr zb ICN 343 sein kann.
        setDestinationStation(entry[3]);
        setStopOverStations(entry[4]);
        try{
            setRailNumber(entry[5]);
        } catch (Exception e){
            int i = 0;
        }

        setIconColLED(state);
    }

    /**
     * Leerer Constructor für "leeres" Objekt
     */
    public DepartureEntry(){
        // nichts machen
    }

    /**
     * Gibt einen String mit allen Informationen eines Eintrags zurück.
     * @return String getrennt mit COL_SEPARATOR
     */
    public String getEntryAsStringLine() {
        String entryAsString = String.join(COL_SEPARATOR,
                String.valueOf(getEntryId()),
                getTime24hFormat(),
                getTrainNumber(),
                getDestinationStation(),
                getStopOverStations(),
                getRailNumberSaveable());
        return entryAsString;
    }

    /**
     * Gibt die Railnumber in einem speicherbaren Format zurück.
     * @return Railnumber
     */
    public String getRailNumberSaveable(){
        String result = getRailNumber();
        if(result.equals("-")){
            return "-1";
        } else {
            return result;
        }
    }

    // Getter und Setter für Properties
    public int getEntryId() {
        return entryId.get();
    }

    public IntegerProperty entryIdProperty() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId.set(entryId);
    }

    public String getTime24hFormat() {
        return time24hFormat.get();
    }

    public StringProperty time24hFormatProperty() {
        return time24hFormat;
    }

    public void setTime24hFormat(String time24hFormat) {
        this.time24hFormat.set(time24hFormat);
    }

    public String getTrainNumber() {
        return trainNumber.get();
    }

    public StringProperty trainNumberProperty() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber.set(trainNumber);
    }

    public String getDestinationStation() {
        return destinationStation.get();
    }

    public StringProperty destinationStationProperty() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation.set(destinationStation);
    }

    public String getStopOverStations() {
        return stopOverStations.get();
    }

    public StringProperty stopOverStationsProperty() {
        return stopOverStations;
    }

    public void setStopOverStations(String stopOverStations) {
        this.stopOverStations.set(stopOverStations);
    }

    public String getRailNumber() {
        return railNumber.get();
    }

    public StringProperty railNumberProperty() {
        return railNumber;
    }

    public void setRailNumber(String railNumber) {
        if(Integer.valueOf(railNumber)<0){
            this.railNumber.set("-");
            return;
        }
        this.railNumber.set(railNumber);
    }

    public String getIconColLED() {
        return iconColLED.get();
    }

    public StringProperty iconColLEDProperty() {
        return iconColLED;
    }

    public void setIconColLED(String iconColLED) {
        this.iconColLED.set(iconColLED);
    }
}
