package ch.fhnw.oop2.module10;

import javafx.collections.ObservableList;
import org.junit.*;
import static junit.framework.Assert.*;

/**
 * Created by Seb on 31.05.2016.
 */
public class DepartureModelTest {
    private DepartureModel departureModel;

    @Before
    public void setUp(){
        departureModel = new DepartureModel();
    }

    /**
     * Testet ob die totale Anzahl (String) an Entries korrekt gesetzt wird
     */
    @Test
    public void testTotalAmountOfEntries(){
        // given
        departureModel.getDepartureEntries().clear();

        // when
        departureModel.addNewDepartureEntry();
        departureModel.addNewDepartureEntry();
        departureModel.addNewDepartureEntry();
        departureModel.addNewDepartureEntry();
        // selektiere ersten Eintrag...
        departureModel.setSelectedDeparture(departureModel.getDepartureEntries().get(0));
        departureModel.removeDeparture(); // lösche diesen..
        departureModel.addNewDepartureEntry();

        // then
        assertEquals("4", departureModel.getTotalAmountOfEntries());
    }

    /**
     * Testet ob das CSV korrekt eingelesen wird.
     */
    @Test
    public void testReadCSVDepartureFile(){
        // given
        // olten.csv im Resources von Test

        // when
        departureModel.setSelectedDeparture(departureModel.getDepartureEntries().get(6)); // Stichprobe

        // then
        assertEquals(departureModel.getSelectedDeparture().getTime24hFormat(), "00:37");
        assertEquals(departureModel.getSelectedDeparture().getTrainNumber(), "IC 1096");
        assertEquals(departureModel.getSelectedDeparture().getDestinationStation(), "Basel SBB");
        assertEquals(departureModel.getSelectedDeparture().getStopOverStations(), "Olten  00:37 - Liestal  00:52 - Basel SBB  01:03");
        assertEquals(departureModel.getSelectedDeparture().getRailNumber(), "-");
    }

    /**
     * Testet die Validierung der Abfahrtszeit.
     */
    @Test
    public void testValidateDepartureTime(){
        // given
        String departureTime1 = "04:20";
        String departureTime2 = "-2:00";
        String departureTime3 = "00:00:";
        String departureTime4 = "A:60";
        String departureTime5 = "30:70";
        String departureTime6 = "15:-2";
        String departureTime7 = "-2:-5";
        String departureTime8 = "00:15";

        // when

        // then
        assertEquals(true, departureModel.validateDepartureTime(departureTime1));
        assertEquals(false, departureModel.validateDepartureTime(departureTime2));
        assertEquals(false, departureModel.validateDepartureTime(departureTime3));
        assertEquals(false, departureModel.validateDepartureTime(departureTime4));
        assertEquals(false, departureModel.validateDepartureTime(departureTime5));
        assertEquals(false, departureModel.validateDepartureTime(departureTime6));
        assertEquals(false, departureModel.validateDepartureTime(departureTime7));
        assertEquals(true, departureModel.validateDepartureTime(departureTime8));
    }

    /**
     * Testet die Validierung des Ankunftorts
     */
    @Test
    public void testValidateToLocation(){
        // given
        String toLocation1 = "Zürich HB";
        String toLocation2 = "Bern 12";
        String toLocation3 = "";
        String toLocation4 = "Biel/Bienne";
        String toLocation5 = "Genf (Genneva)";
        String toLocation6 = "Interlaken Ost,";

        // when

        // then
        assertEquals(true ,departureModel.validateToLocation(toLocation1));
        assertEquals(false ,departureModel.validateToLocation(toLocation2));
        assertEquals(false ,departureModel.validateToLocation(toLocation3));
        assertEquals(true ,departureModel.validateToLocation(toLocation4));
        assertEquals(true ,departureModel.validateToLocation(toLocation5));
        assertEquals(false, departureModel.validateToLocation(toLocation6));
    }

    /**
     * Testet die Validierung der Zugnummer.
     */
    @Test
    public void testValidateTrainNumber(){
        // given
        String trainNumber1 = "IC 12";
        String trainNumber2 = "EC 12";
        String trainNumber3 = "ICN 12";
        String trainNumber4 = "ICE 12";
        String trainNumber5 = "ICE -12";
        String trainNumber6 = "ICE " + String.valueOf(departureModel.getMAX_TRAIN_NUMBER()+1);
        String trainNumber7 = "ICE " + String.valueOf(departureModel.getMAX_TRAIN_NUMBER()-1);
        String trainNumber8 = "12";
        String trainNumber9 = "ICE";
        String trainNumber10 = "";
        String trainNumber11 = "12 ICE";

        // when

        // then
        assertEquals(true, departureModel.validateTrainNumber(trainNumber1));
        assertEquals(true, departureModel.validateTrainNumber(trainNumber2));
        assertEquals(true, departureModel.validateTrainNumber(trainNumber3));
        assertEquals(true, departureModel.validateTrainNumber(trainNumber4));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber5));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber6));
        assertEquals(true, departureModel.validateTrainNumber(trainNumber7));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber8));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber9));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber10));
        assertEquals(false, departureModel.validateTrainNumber(trainNumber11));
    }

    /**
     * Testet die Validierung der Gleisnummer.
     */
    @Test
    public void testValidateRailNumber(){
        // given
        String railNumber1 = "-";
        String railNumber2 = "15";
        String railNumber3 = "-40";
        String railNumber4 = String.valueOf(departureModel.getMAX_RAIL_NUMBER()+1);
        String railNumber5 = "";

        // when

        // then
        assertEquals(true, departureModel.validateRailNumber(railNumber1));
        assertEquals(true, departureModel.validateRailNumber(railNumber2));
        assertEquals(false, departureModel.validateRailNumber(railNumber3));
        assertEquals(false, departureModel.validateRailNumber(railNumber4));
        assertEquals(false, departureModel.validateRailNumber(railNumber5));
    }

    /**
     * Testet die Validierung der Zwischenhalte.
     */
    @Test
    public void testValidateStopOvers(){
        // given
        String stopOvers1 = "";
        String stopOvers2 = "Olten  00:33 - Zürich HB  01:04";

        // when

        // then
        assertEquals(false, departureModel.validateStopOvers(stopOvers1));
        assertEquals(true, departureModel.validateStopOvers(stopOvers2));
    }

    /**
     * Testet den Proxy für die Bindings.
     */
    @Test
    public void testDepartureEntryProxy(){
        // given
        DepartureEntry entry1 = departureModel.getNewDepartureEntry();
        entry1.setDestinationStation("Bern");
        entry1.setEntryId(1);
        entry1.setIconColLED("Nicht gespeichert");
        entry1.setRailNumber("5");
        entry1.setStopOverStations("Olten 00:30");
        entry1.setTime24hFormat("00:30");
        entry1.setTrainNumber("ICE 12");

        // when
        departureModel.setSelectedDeparture(entry1);

        // then
        assertEquals(entry1.getDestinationStation(), departureModel.getSelectedDeparture().getDestinationStation());
        assertEquals(entry1.getEntryId(), departureModel.getSelectedDeparture().getEntryId());
        assertEquals(entry1.getIconColLED(), departureModel.getSelectedDeparture().getIconColLED());
        assertEquals(entry1.getRailNumber(), departureModel.getSelectedDeparture().getRailNumber());
        assertEquals(entry1.getStopOverStations(), departureModel.getSelectedDeparture().getStopOverStations());
        assertEquals(entry1.getTime24hFormat(), departureModel.getSelectedDeparture().getTime24hFormat());
        assertEquals(entry1.getTrainNumber(), departureModel.getSelectedDeparture().getTrainNumber());
    }

    /**
     * Testet ob eine Modifikation am Proxy eine Modifikation an der selektierten DepartureEntry hat.
     */
    @Test
    public void testDepartureEntryProxyModification(){
        // given
        departureModel.setSelectedDeparture(departureModel.getNewDepartureEntry());
        assertEquals(departureModel.getSelectedDeparture().getDestinationStation(), "Zürich HB"); // Initialer Wert, hier prüfen falls im Code anders

        // when
        departureModel.getDepartureEntryProxy().setDestinationStation("Bern");

        // then
        assertEquals(departureModel.getSelectedDeparture().getDestinationStation(), "Bern");
    }

    /**
     * Testet den Umgang mit der HashMap welche fehlerhafte Felder drin hat.
     */
    @Test
    public void testFieldWithError(){
        // given
        String wrongDepartureFieldValue = "-2:00";
        String wrongTrainNumberValue = "ICEEE 123";
        String wrongLocationValue = "Zürich 12";
        String wrongRailNumber = "-5";
        String wrongStopOver = "";

        // when
        departureModel.validateDepartureTime(wrongDepartureFieldValue);
        departureModel.validateTrainNumber(wrongTrainNumberValue);
        departureModel.validateToLocation(wrongLocationValue);
        departureModel.validateRailNumber(wrongRailNumber);
        departureModel.validateStopOvers(wrongStopOver);

        // then
        assertEquals(departureModel.getFieldsWithErrors().size(), 5);


        // when
        String railNumbercorrected = "5";
        departureModel.validateRailNumber(railNumbercorrected);

        // then
        assertEquals(departureModel.getFieldsWithErrors().size(), 4);
    }

    /**
     * Testet ob ein Suchfilter korrekt die Ergebnisse beeinflusst.
     */
    @Test
    public void testSearchTerm(){
        // given
        String searchKey = "Bern";

        // when
        departureModel.searchTerm(searchKey);

        // then
        assertEquals(2, departureModel.getFilteredDepartureEntries().size());
        // Nur 2 Einträge habe als Zielort Bern drin

        // given
        searchKey = "";

        // when
        departureModel.searchTerm(searchKey);

        // then
        assertEquals(11, departureModel.getFilteredDepartureEntries().size());
        // Alle Einträge sind 11 in der Anzahl
    }

    /**
     * Testet ob mehrfache Undos korrekt ausgeführt werden
     */
    @Test
    public void testMultipleUndo(){
        // given
        departureModel.setSelectedDeparture(departureModel.getDepartureEntries().get(0));
        // Initial hat dieser 00:00
        departureModel.getSelectedDeparture().setTime24hFormat("00:15");
        departureModel.getSelectedDeparture().setTime24hFormat("00:30");

        // when
        departureModel.undo();
        departureModel.undo();

        // then
        assertEquals("00:00", departureModel.getSelectedDeparture().getTime24hFormat());

    }

    /**
     * Testet ob ein einfaches Undo korrekt ausgeführt wird
     */
    @Test
    public void testSingleUndo(){
        // given
        departureModel.setSelectedDeparture(departureModel.getDepartureEntries().get(0));
        // Initial hat dieser IC 747
        departureModel.getSelectedDeparture().setTrainNumber("IC 7474");

        // when
        departureModel.undo();

        // then
        assertEquals("IC 747", departureModel.getSelectedDeparture().getTrainNumber());
    }

    /**
     * Testet ob mehrfache Redos korrekt ausgeführt werden
     */
    @Test
    public void testMultipleRedo(){
        // given
        departureModel.setSelectedDeparture(departureModel.getDepartureEntries().get(0));
        // Initial hat dieser 00:00
        departureModel.getSelectedDeparture().setTime24hFormat("00:15");
        departureModel.getSelectedDeparture().setTime24hFormat("00:30");

        // when
        departureModel.undo();
        departureModel.undo();
        departureModel.redo();
        departureModel.redo();

        // then
        assertEquals("00:30", departureModel.getSelectedDeparture().getTime24hFormat());
    }

    @After
    public void cleanUp(){
        departureModel.getDepartureEntries().clear();
    }
}
