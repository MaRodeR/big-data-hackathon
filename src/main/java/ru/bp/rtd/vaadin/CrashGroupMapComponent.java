package ru.bp.rtd.vaadin;

import com.vaadin.data.HasValue;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.vaadin.addon.leaflet.LCircle;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LOpenStreetMapLayer;
import ru.bp.rtd.domain.CrashGroup;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.ArrayList;
import java.util.List;

public class CrashGroupMapComponent extends VerticalLayout {

    private GBCrashAnalyzerService gbCrashAnalyzerService;
    private String accidentsFilePath;

    private final LMap map;
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
        map.addBaseLayer(new LOpenStreetMapLayer(), "CarCrashes");

        HorizontalLayout parametersPanel = new HorizontalLayout();
        parametersPanel.setHeightUndefined();
        parametersPanel.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        parametersPanel.addComponent(new Label("time"));
        parametersPanel.setMargin(new MarginInfo(false, true));
        Slider slider = new Slider(0, 23);
        slider.setWidth("200px");
        slider.addValueChangeListener((HasValue.ValueChangeListener<Double>) event -> {
            int hourValue = event.getValue().intValue();
            List<CrashGroup> crashGroups = this.gbCrashAnalyzerService.getGroupCrashesByHourOfDay(this.accidentsFilePath, hourValue);
            crashGroupComponents.forEach(map::removeComponent);
            crashGroupComponents.clear();
            crashGroups.stream().map(CrashGroupMapComponent::createCrashGroupComponent).forEach(component -> {
                map.addComponent(component);
                crashGroupComponents.add(component);
            });
        });

        parametersPanel.addComponent(slider);
        addComponent(parametersPanel);

        addComponent(map);
        setExpandRatio(map, 1.0F);
    }

    private static Component createCrashGroupComponent(CrashGroup crashGroup) {
        LCircle circle = new LCircle(crashGroup.getCenterLatitude(), crashGroup.getCenterLongitude(), crashGroup.getRadius());
        circle.setColor("#F03730");
        return circle;
    }

}
