package ch.fhnw.oop2.module10;

/**
 * Created by Seb on 08.06.2016.
 */
public enum DepartureState {
    SAVED("Gespeichert"), CHANGED("Geändert"), NEW("Neu");

    private String stateGermanDescription;

    DepartureState (String stateGermanDescription){
        this.stateGermanDescription = stateGermanDescription;
    }

    @Override
    public String toString(){
        return this.stateGermanDescription;
    }
}
