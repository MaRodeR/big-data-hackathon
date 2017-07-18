package ru.bp.rtd.vaadin;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.*;

public class CrashMapComponent extends VerticalLayout {

    public CrashMapComponent() {

        setSizeFull();
        setMargin(false);
        setSpacing(false);

        HorizontalLayout parametersPanel = new HorizontalLayout();
        parametersPanel.setHeightUndefined();
        parametersPanel.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        parametersPanel.addComponent(new Label("time"));
        parametersPanel.setMargin(new MarginInfo(false, true));
        Slider slider = new Slider(0, 23);
        slider.setWidth("200px");
        parametersPanel.addComponent(slider);
        addComponent(parametersPanel);

        GoogleMap googleMap = new GoogleMap(null, null, null);
        googleMap.setCenter(new LatLon(52.5, -3));
        googleMap.setZoom(7);
        googleMap.setSizeFull();
        googleMap.setHeight("100%");
        addComponent(googleMap);
        setExpandRatio(googleMap, 1.0F);
    }
}
