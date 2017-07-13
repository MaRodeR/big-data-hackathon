package ru.bp.rtd.vaadin;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

    @Autowired
    private GBCrashAnalyzerService gbCrashAnalyzerService;

    private String carModelsFile = this.getClass().getClassLoader().getResource("samples/crashes/gb/2015_Make_Model.csv").getFile();
    private String vehiclesFile = this.getClass().getClassLoader().getResource("samples/crashes/gb/Vehicles_2015.csv").getFile();

    @Override
    protected void init(VaadinRequest request) {

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        VerticalLayout chartLayout = new VerticalLayout();
        chartLayout.addComponent(createMostDangerousCarsChart());
        chartLayout.addComponent(createCrashesCountByDriversAgeChart());
        tabSheet.addTab(chartLayout, "charts");

        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setSizeFull();

        GoogleMap googleMap = new GoogleMap(null, null, null);
        googleMap.setCenter(new LatLon(51.478565, -0.181239));
        googleMap.setZoom(10);
        googleMap.setSizeFull();
        mapLayout.addComponent(googleMap);

        tabSheet.addTab(mapLayout, "map");


        setContent(tabSheet);
    }

    private HighChart createMostDangerousCarsChart() {
        List<Row> rows = gbCrashAnalyzerService.getMostDangerousCars(carModelsFile);
        String categories = "";
        String series = "";
        for (Row row : rows) {
            String make = row.getString(0);

            if (!"NULL".equalsIgnoreCase(make)) {
                Long year = row.getLong(1);
                categories += "'" + make + "'" + ",";
                series += "{name: '" + make + "', y: " + year + "},";
            }
        }

        HighChart chart = new HighChart();

        chart.setHcjs("var options = { title: {  text: 'Crashes by make'}, chart: {\n" +
                "        type: 'column', " +
                "    },\n" +
                "\n" +
                "    xAxis: {\n" +
                "        categories: [" + categories + "]" +
                "    },\n" +
                "\n" +
                "    series: [ {name: 'Make', data: [" + series + "]" +
                "    }]};");
        return chart;
    }

    private HighChart createCrashesCountByDriversAgeChart() {
        Map<Integer, Integer> crashesByAge = gbCrashAnalyzerService.getCrashesCountByDriversAge(vehiclesFile);

        ArrayList<Integer> ages = new ArrayList<>(crashesByAge.keySet());
        Collections.sort(ages);


        String categories = "";
        String series = "";

        for (Integer age : ages) {
            categories += "'" + age + "+',";
            series += "{name: '" + age + "+', y: " + crashesByAge.get(age) + "},";
        }

        HighChart chart = new HighChart();

        chart.setHcjs("var options = { title: {  text: 'Crashes by driver age'}, chart: {\n" +
                "        type: 'column', " +
                "    },\n" +
                "\n" +
                "    xAxis: {\n" +
                "        categories: [" + categories + "]" +
                "    },\n" +
                "\n" +
                "    series: [ {name: 'Driver age', data: [" + series + "]" +
                "    }]};");
        return chart;
    }
}