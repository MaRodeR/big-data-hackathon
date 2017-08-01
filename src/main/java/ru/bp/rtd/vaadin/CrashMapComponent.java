package ru.bp.rtd.vaadin;

import com.vaadin.data.HasValue;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.*;
import ru.bp.rtd.domain.CarCrash;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CrashMapComponent extends VerticalLayout {

    private GBCrashAnalyzerService gbCrashAnalyzerService;
    private String accidentsFilePath;

    private final GoogleMap crashMap;

    public CrashMapComponent(GBCrashAnalyzerService gbCrashAnalyzerService, String accidentsFilePath) {

        this.gbCrashAnalyzerService = gbCrashAnalyzerService;
        this.accidentsFilePath = accidentsFilePath;

        setSizeFull();
        setMargin(false);
        setSpacing(false);

        crashMap = new GoogleMap(null, null, null);
        crashMap.setCenter(new LatLon(52.5, -3));
        crashMap.setZoom(7);
        crashMap.setSizeFull();
        crashMap.setHeight("100%");

        HorizontalLayout parametersPanel = new HorizontalLayout();
        parametersPanel.setHeightUndefined();
        parametersPanel.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        parametersPanel.addComponent(new Label("time"));
        parametersPanel.setMargin(new MarginInfo(false, true));


        ArrayList<ComboboxItem> severityItems = new ArrayList<>();
        severityItems.add(new ComboboxItem(1,"Fatal"));
        severityItems.add(new ComboboxItem(2,"Serious"));
        severityItems.add(new ComboboxItem(3,"Slight"));


        ComboBox<ComboboxItem> cbSeverity = new ComboBox();
        cbSeverity.setItems(severityItems);
        cbSeverity.setItemCaptionGenerator(ComboboxItem::getName);
        parametersPanel.addComponent(cbSeverity);

        Slider slider = new Slider(0, 23);
        slider.setWidth("200px");
        slider.addValueChangeListener((HasValue.ValueChangeListener<Double>) event -> {
            int hourValue = event.getValue().intValue();
            new ArrayList<>(crashMap.getMarkers()).forEach(crashMap::removeMarker);

            List<CarCrash> crashes = this.gbCrashAnalyzerService.getCrashesByHourOfDay(this.accidentsFilePath, hourValue, cbSeverity.getValue().value);
            crashes.stream().map(CrashMapComponent::createCrashMarker).forEach(crashMap::addMarker);
        });
        parametersPanel.addComponent(slider);



        addComponent(parametersPanel);

        addComponent(crashMap);
        setExpandRatio(crashMap, 1.0F);
    }

    private static GoogleMapMarker createCrashMarker(CarCrash carCrash) {
        return new GoogleMapMarker(carCrash.getId(),
                new LatLon(carCrash.getLatitude(), carCrash.getLongitude()),
                false, (carCrash.isPoliceForce() == 1) ? "https://cdn0.iconfinder.com/data/icons/transport-filled-outline/2048/338_-_Police_Car-32.png" : "https://cdn0.iconfinder.com/data/icons/car-crash/500/accident-32.png");
    }

    public class ComboboxItem implements Serializable {

        private int value;
        private String name;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String toString() {
            return getName() + " " + getId();
        }

        public ComboboxItem(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

}
