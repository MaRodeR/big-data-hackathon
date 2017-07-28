package ru.bp.rtd.vaadin;


import com.vaadin.annotations.Theme;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.server.*;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

    @Autowired
    private GBCrashAnalyzerService gbCrashAnalyzerService;

    private final String carModelsFile = this.getClass().getClassLoader().getResource("samples/crashes/gb/2015_Make_Model.csv").getFile();
    private final String vehiclesFile = this.getClass().getClassLoader().getResource("samples/crashes/gb/Vehicles_2015.csv").getFile();
    private final String accidentsFilePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/Accidents_2015.csv").getFile();

    @Override
    protected void init(VaadinRequest request) {

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        HorizontalLayout chartLayout = new HorizontalLayout();
        chartLayout.addComponent(createMostDangerousCarsChart());
        chartLayout.addComponent(createCrashesCountByDriversAgeChart());

        List<String> makes = gbCrashAnalyzerService.getMake(carModelsFile);
        VerticalLayout leftVLayout = new VerticalLayout();
        VerticalLayout rightVLayout = new VerticalLayout();

        ComboBox<String> comboBoxLeft = new ComboBox<>();
        comboBoxLeft.setItems(makes);
        comboBoxLeft.addSelectionListener((SingleSelectionListener<String>) singleSelectionEvent -> {
            leftVLayout.removeAllComponents();
            leftVLayout.addComponent(comboBoxLeft);
            HighChart leftChart = createCrashesCountByMakeChart(singleSelectionEvent.getValue());
            leftVLayout.addComponent(leftChart);
        });

        ComboBox<String> comboBoxRight = new ComboBox<>();
        comboBoxRight.setItems(makes);

        comboBoxRight.addSelectionListener(singleSelectionEvent -> {
            HighChart rightChart = createCrashesCountByMakeChart(singleSelectionEvent.getValue());
            rightVLayout.addComponent(rightChart);
        });
        leftVLayout.addComponent(comboBoxLeft);
        rightVLayout.addComponent(comboBoxRight);

        Image vsImage = new Image();
        vsImage.setSource(new ExternalResource("https://avatanplus.com/files/resources/original/592133aed4a1815c29b1e317.png"));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(leftVLayout);
        horizontalLayout.addComponent(vsImage);
        horizontalLayout.addComponent(rightVLayout);


        tabSheet.addTab(chartLayout, "charts");
        tabSheet.addTab(new CrashMapComponent(gbCrashAnalyzerService, accidentsFilePath), "map");
        tabSheet.addTab(new CrashGroupMapComponent(gbCrashAnalyzerService, accidentsFilePath), "crash groups");
        tabSheet.addTab(horizontalLayout, "VS battle");

        setContent(tabSheet);

    }

    private HighChart createMostDangerousCarsChart() {
        List<Row> rows = gbCrashAnalyzerService.getMostDangerousCars(carModelsFile, 10);
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

    private HighChart createCrashesCountByMakeChart(String make) {
        long count = gbCrashAnalyzerService.getCrashesCountByCarMake(carModelsFile, make);

        String categories = "";
        String series = "";

            categories += "'" + make + "+'";
            series += "{name: '" + make + "+', y: " + count + "},";

        HighChart chart = new HighChart();

        chart.setHcjs("var options = { title: {  text: 'Crashes of "+make+"'}, chart: {\n" +
                "        type: 'column', " +
                "    },\n" +
                "\n" +
                "    xAxis: {\n" +
                "        categories: [" + categories + "]" +
                "    },\n" +
                "\n" +
                "    series: [ {name: 'Count', data: [" + series + "]" +
                "    }]};");
        return chart;
    }
}