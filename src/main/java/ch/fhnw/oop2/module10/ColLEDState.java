package ch.fhnw.oop2.module10;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Seb on 12.05.2016.
 */
public class ColLEDState extends TableCell<DepartureEntry, String> {
    private static final Map<String, Image> COL_CIRCLE_PICTURE = new HashMap<>();
    private static final Insets ICON_INSET = new Insets(1, 8, 1, 5);

    @Override
    protected void updateItem(String item, boolean empty){
        super.updateItem(item, empty);
        setGraphic(null);
        setText(null);
        if(item != null && !empty){
            Image img = COL_CIRCLE_PICTURE.get(item);
            if (img == null){
                img = new Image(getClass().getResource("/mark_" + item + ".png")
                        .toExternalForm(), 18, 18, true, true, true);
                COL_CIRCLE_PICTURE.put(item, img);
            }
            ImageView imageView = new ImageView(img);
            setGraphic(imageView);
            setTooltip(new Tooltip(item));
            setAlignment(Pos.CENTER);
            setPadding(ICON_INSET);
        }
    }
}
