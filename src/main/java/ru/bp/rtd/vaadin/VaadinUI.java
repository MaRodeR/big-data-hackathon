package ru.bp.rtd.vaadin;


import com.vaadin.annotations.Theme;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.server.*;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.*;
import java.util.stream.Collectors;

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

        VerticalLayout vsLayout = getVSComponents();

        tabSheet.addTab(chartLayout, "charts");
        tabSheet.addTab(new CrashMapComponent(gbCrashAnalyzerService, accidentsFilePath), "map");
        tabSheet.addTab(new CrashGroupMapComponent(gbCrashAnalyzerService, accidentsFilePath), "crash groups");
        tabSheet.addTab(vsLayout, "VS battle");
        tabSheet.addTab(getPieComponents(), "Pie");

        setContent(tabSheet);

    }

    private VerticalLayout getVSComponents() {
        VerticalLayout verticalLayout = new VerticalLayout();

        List<String> makes = gbCrashAnalyzerService.getColValue(carModelsFile, "make");
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
            rightVLayout.removeAllComponents();
            rightVLayout.addComponent(comboBoxRight);
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


        verticalLayout.addComponent(horizontalLayout);


        HorizontalLayout vsAgeOfCarHorizontalLayout = new HorizontalLayout();
        VerticalLayout leftAgeOfCarVLayout = new VerticalLayout();
        List<Integer> ages = gbCrashAnalyzerService.getIntColValue(carModelsFile, "Age_of_Vehicle");

         Collections.sort(ages);
        ComboBox<Integer> comboBoxLeftAgeOfCars = new ComboBox<>();
        comboBoxLeftAgeOfCars.setItems(ages);

        leftAgeOfCarVLayout.addComponent(comboBoxLeftAgeOfCars);
        vsAgeOfCarHorizontalLayout.addComponent(leftAgeOfCarVLayout);

        verticalLayout.addComponent(vsAgeOfCarHorizontalLayout);

        return verticalLayout;
    }

    private VerticalLayout getPieComponents() {
        VerticalLayout verticalLayout = new VerticalLayout();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");


        Map<Integer, String> pointOfImpactMapping = new HashMap<>();

        pointOfImpactMapping.put(0, "Did not impact");

        pointOfImpactMapping.put(1, "Front");
        pointOfImpactMapping.put(2, "Back");
        pointOfImpactMapping.put(3, "Offside");
        pointOfImpactMapping.put(4, "Nearside");

        HighChart chartMale = getPieMaleFemaleChart(1, 12, pointOfImpactMapping, "1st point of impact - Male");
        horizontalLayout.addComponent(chartMale);
        HighChart chartFemale = getPieMaleFemaleChart(2, 12, pointOfImpactMapping, "1st point of impact - Female");
        horizontalLayout.addComponent(chartFemale);
        verticalLayout.addComponent(horizontalLayout);

        Map<Integer, String> overturningMapping = new HashMap<>();
        overturningMapping.put(0, "None");
        overturningMapping.put(1, "Skidded");
        overturningMapping.put(2, "Skidded and overturned");
        overturningMapping.put(3, "Jackknifed");
        overturningMapping.put(4, "Jackknifed and overturned");
        overturningMapping.put(5, "Overturned");

        HorizontalLayout overturningLayout = new HorizontalLayout();
        overturningLayout.setWidth("100%");
        HighChart chartOverMale = getPieMaleFemaleChart(1, 8, overturningMapping, "Overturning and Skidding - Male");
        overturningLayout.addComponent(chartOverMale);
        HighChart chartOverFemale = getPieMaleFemaleChart(2, 8, overturningMapping, "Overturning and Skidding - Female");
        overturningLayout.addComponent(chartOverFemale);
        verticalLayout.addComponent(overturningLayout);


        Map<Integer, String> journeyPurposeMapping = new HashMap<>();
        journeyPurposeMapping.put(1, "Journey as part of work");
        journeyPurposeMapping.put(2, "Commuting to/from work");
        journeyPurposeMapping.put(3, "Taking pupil to/from school");
        journeyPurposeMapping.put(4, "Pupil riding to/from school");
        journeyPurposeMapping.put(5, "Other");
        journeyPurposeMapping.put(6, "Not known");


        HorizontalLayout journeyPurposeLayout = new HorizontalLayout();
        journeyPurposeLayout.setWidth("100%");
        HighChart chartJourneyMale = getPieMaleFemaleChart(1, 14, journeyPurposeMapping, "Journey purpose - Male");
        journeyPurposeLayout.addComponent(chartJourneyMale);
        HighChart chartJourneyFemale = getPieMaleFemaleChart(2, 14, journeyPurposeMapping, "Journey purpose- Female");
        journeyPurposeLayout.addComponent(chartJourneyFemale);
        verticalLayout.addComponent(journeyPurposeLayout);


        Map<Integer, String> homeAreaMapping = new HashMap<>();
        homeAreaMapping.put(1, "Urban area");
        homeAreaMapping.put(2, "Small town");
        homeAreaMapping.put(3, "Rural");



        HorizontalLayout homeAreaLayout = new HorizontalLayout();
        homeAreaLayout.setWidth("100%");
        HighChart homeAreaMale = getPieMaleFemaleChart(1, 21, homeAreaMapping, "Home Area - Male");
        homeAreaLayout.addComponent(homeAreaMale);
        HighChart homeAreaFemale = getPieMaleFemaleChart(2, 21, homeAreaMapping, "Home Area - Female");
        homeAreaLayout.addComponent(homeAreaFemale);
        verticalLayout.addComponent(homeAreaLayout);

        Map<Integer, String> vehicleManouvreMapping = new HashMap<>();

        vehicleManouvreMapping.put(	1	,"Reversing");
        vehicleManouvreMapping.put(	2	,"Parked");
        vehicleManouvreMapping.put(	3	,"Waiting to go - held up");
        vehicleManouvreMapping.put(	4	,"Slowing or stopping");
        vehicleManouvreMapping.put(	5	,"Moving off");
        vehicleManouvreMapping.put(	6	,"U-turn");
        vehicleManouvreMapping.put(	7	,"Turning left");
        vehicleManouvreMapping.put(	8	,"Waiting to turn left");
        vehicleManouvreMapping.put(	9	,"Turning right");
        vehicleManouvreMapping.put(	10	,"Waiting to turn right");
        vehicleManouvreMapping.put(	11	,"Changing lane to left");
        vehicleManouvreMapping.put(	12	,"Changing lane to right");
        vehicleManouvreMapping.put(	13	,"Overtaking moving vehicle - offside");
        vehicleManouvreMapping.put(	14	,"Overtaking static vehicle - offside");
        vehicleManouvreMapping.put(	15	,"Overtaking - nearside");
        vehicleManouvreMapping.put(	16	,"Going ahead left-hand bend");
        vehicleManouvreMapping.put(	17	,"Going ahead right-hand bend");
        vehicleManouvreMapping.put(	18	,"Going ahead other");

        HorizontalLayout manouvreLayout = new HorizontalLayout();
        manouvreLayout.setWidth("100%");
        HighChart manouvreMale = getPieMaleFemaleChart(1, 5, vehicleManouvreMapping, "Vehicle Manouvre - Male");
        manouvreLayout.addComponent(manouvreMale);
        HighChart manouvreFemale = getPieMaleFemaleChart(2, 5, vehicleManouvreMapping, "Vehicle Manouvre - Female");
        manouvreLayout.addComponent(manouvreFemale);
        verticalLayout.addComponent(manouvreLayout);

        return  verticalLayout;

    }

    private HighChart getPieMaleFemaleChart(int value, int descriminatorColumnNumber, Map<Integer, String> mapping, String pieTitle) {
        HighChart chart = new HighChart();
        StringBuilder result = new StringBuilder();


        Map<Integer, Integer> crashes = gbCrashAnalyzerService.getMaleFemaleStats(carModelsFile, value, descriminatorColumnNumber);
        for (Integer pointOfImpact : crashes.keySet()) {
            result.append("['"+mapping.get(pointOfImpact) + "', "+ crashes.get(pointOfImpact) +"],");
        }

        chart.setHcjs("var options = {\n" +
                "    chart: {\n" +
                "        type: 'pie',\n" +
                "        options3d: {\n" +
                "            enabled: true,\n" +
                "            alpha: 45,\n" +
                "            beta: 0\n" +
                "        }\n" +
                "    },\n" +
                "    title: {\n" +
                "        text: '"+ pieTitle +"' \n" +
                "    },\n" +
                "    tooltip: {\n" +
                "        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'\n" +
                "    },\n" +
                "    plotOptions: {\n" +
                "        pie: {\n" +
                "            allowPointSelect: true,\n" +
                "            cursor: 'pointer',\n" +
                "            depth: 35,\n" +
                "            dataLabels: {\n" +
                "                enabled: true,\n" +
                "                format: '{point.name}'\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    series: [{\n" +
                "        type: 'pie',\n" +
                "        name: 'Percent',\n" +
                "        data: [\n" + result.toString()+
                "        ]\n" +
                "    }]\n" +
                "};");
        return chart;
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
                "    }, yAxis: {min: 0, max: 10000}, " +
                "\n" +
                "    series: [ {name: 'Count', data: [" + series + "]" +
                "    }]};");
        return chart;
    }
}