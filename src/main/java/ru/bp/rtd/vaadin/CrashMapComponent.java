package ru.bp.rtd.vaadin;

import com.vaadin.data.HasValue;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.*;
import ru.bp.rtd.domain.CarCrash;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.ArrayList;
import java.util.Collection;
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
        Slider slider = new Slider(0, 23);
        slider.setWidth("200px");
        slider.addValueChangeListener((HasValue.ValueChangeListener<Double>) event -> {
            int hourValue = event.getValue().intValue();
            new ArrayList<>(crashMap.getMarkers()).forEach(crashMap::removeMarker);

            List<CarCrash> crashes = this.gbCrashAnalyzerService.getCrashesByHourOfDay(this.accidentsFilePath, hourValue);
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
                false);
    }
}
