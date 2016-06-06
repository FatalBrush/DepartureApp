package ch.fhnw.oop2.module10;

import javafx.beans.property.Property;

/**
 * Created by Seb on 06.06.2016.
 */
public class ValueChangeCommand<T> implements Command {
    private final DepartureModel model;
    private final Property<T> property;
    private final T oldValue;
    private final T newValue;

    public ValueChangeCommand(DepartureModel model, Property<T> property, T oldValue, T newValue){
        this.model = model;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void undo() {
        model.setPropertyValueWithoutUndoSupport(property, oldValue);
    }

    @Override
    public void redo() {
        model.setPropertyValueWithoutUndoSupport(property, newValue);
    }
}
