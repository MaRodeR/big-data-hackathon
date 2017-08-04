package ru.bp.rtd.vaadin;

import com.vaadin.data.HasValue;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.vaadin.addon.leaflet.*;
import ru.bp.rtd.domain.CrashGroup;
import ru.bp.rtd.domain.Point;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.ArrayList;
import java.util.List;

public class CrashGroupMapComponent extends VerticalLayout {

    private GBCrashAnalyzerService gbCrashAnalyzerService;
    private String accidentsFilePath;

    private final LMap map;
    private final Slider hourSlider;
    private List<Component> crashGroupComponents = new ArrayList<>();

    public CrashGroupMapComponent(GBCrashAnalyzerService gbCrashAnalyzerService, String accidentsFilePath) {

        this.gbCrashAnalyzerService = gbCrashAnalyzerService;
        this.accidentsFilePath = accidentsFilePath;

        setSizeFull();
        setMargin(false);
        setSpacing(false);

        map = new LMap();
        map.setCenter(52.5, -3);
        map.setZoomLevel(7);
        map.addBaseLayer(new LWmsLayer(), "CarCrashes");

        HorizontalLayout parametersPanel = new HorizontalLayout();
        parametersPanel.setHeightUndefined();
        parametersPanel.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        parametersPanel.addComponent(new Label("time"));
        parametersPanel.setMargin(new MarginInfo(false, true));

        hourSlider = new Slider(-1, 23);
        hourSlider.setValue(-1D);
        hourSlider.setWidth("200px");
        hourSlider.addValueChangeListener((HasValue.ValueChangeListener<Double>) event -> updateGroupCrashes());

        map.addMoveEndListener(event -> updateGroupCrashes());

        parametersPanel.addComponent(hourSlider);
        addComponent(parametersPanel);

        addComponent(map);
        setExpandRatio(map, 1.0F);
    }

    private static Component createCrashGroupComponent(CrashGroup crashGroup) {
        LCircle circle = new LCircle(crashGroup.getCenterLatitude(), crashGroup.getCenterLongitude(), crashGroup.getRadius());
        circle.setColor("#F03730");
        circle.setFillColor("#F03730");
        circle.setOpacity(0.3);
        circle.setFillOpacity(0.4);
        return circle;
    }

    private void updateGroupCrashes() {
        List<CrashGroup> crashGroups = new ArrayList<>();
        int hour = hourSlider.getValue().intValue();
        if (hour >= 0) {
            Double zoomLevel = map.getZoomLevel();
            double maxDistance = zoomLevel <= 7 ? 80000 : (80000 / ((zoomLevel - 7) * 2));

            Point northEastCorner = null;
            Point southWestCorner = null;

            if (zoomLevel > 8) {
                northEastCorner = new Point(map.getBounds().getNorthEastLat(), map.getBounds().getNorthEastLon());
                southWestCorner = new Point(map.getBounds().getSouthWestLat(), map.getBounds().getSouthWestLon());
            }

            crashGroups.addAll(this.gbCrashAnalyzerService.getGroupCrashesByHourOfDay(
                    this.accidentsFilePath, hour, maxDistance, southWestCorner, northEastCorner));
        }

        crashGroupComponents.forEach(map::removeComponent);
        crashGroupComponents.clear();
        crashGroups.stream().map(CrashGroupMapComponent::createCrashGroupComponent).forEach(component -> {
            map.addComponent(component);
            crashGroupComponents.add(component);
        });
    }

}
