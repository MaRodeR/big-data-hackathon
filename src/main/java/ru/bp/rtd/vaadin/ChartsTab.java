package ru.bp.rtd.vaadin;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import org.apache.spark.sql.Row;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.domain.RoadDriverAgeCrash;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ChartsTab extends VerticalLayout {

    private boolean isInitialized = false;

    private final GBCrashAnalyzerService gbCrashAnalyzerService;
    private final String carModelsFile;
    private final String vehiclesFile;
    private final String accidentsFile;

    public ChartsTab(GBCrashAnalyzerService gbCrashAnalyzerService, String carModelsFile, String vehiclesFile, String accidentsFile) {
        this.gbCrashAnalyzerService = gbCrashAnalyzerService;
        this.carModelsFile = carModelsFile;
        this.vehiclesFile = vehiclesFile;
        this.accidentsFile = accidentsFile;

        Button button = new Button("show data");
        button.addClickListener((Button.ClickListener) event -> {
            if (!isInitialized) {
                VerticalLayout chartsLayout = new VerticalLayout();
                chartsLayout.addComponent(createMostDangerousCarsChart());
                chartsLayout.addComponent(createCrashesCountByDriversAgeChart());
                chartsLayout.addComponent(creatCrashesByRoadTypeAndDriverAgeChart());
                ChartsTab.this.addComponent(chartsLayout);
                isInitialized = true;
            }
        });
        this.addComponent(button);
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

    private HighChart creatCrashesByRoadTypeAndDriverAgeChart() {
        List<RoadDriverAgeCrash> crashes = gbCrashAnalyzerService.getCrashesByRoadTypeAndDriverAge(vehiclesFile, accidentsFile);
        HighChart chart = new HighChart();

        Set<String> categories = crashes.stream().map(RoadDriverAgeCrash::getRoadType).collect(toSet());
        List<String> series = crashes.stream()
                .map(RoadDriverAgeCrash::getAge)
                .collect(Collectors.toSet())
                .stream()
                .sorted(Integer::compareTo)
                .filter(value -> value > 0)
                .map(age -> {
                    Optional<String> data = categories.stream()
                            .map(s -> crashes.stream()
                                    .filter(crash -> crash.getAge() == age && crash.getRoadType().equals(s))
                                    .findFirst().orElse(new RoadDriverAgeCrash().setCrashCount(0)).getCrashCount())
                            .map(Object::toString)
                            .reduce((count1, count2) -> count1 + ", " + count2);
                    return "{ name: " + age + ", data: [" + data.get() + "]}";
                })
                .collect(toList());


        chart.setHcjs("var options = {\n" +
                "    title: {\n" +
                "        text: 'test diagram'\n" +
                "    },\n" +
                "    series: [" + series.stream().reduce((v1, v2) -> v1 + ", " + v2).get() + "]," +
                "    plotOptions: {\n" +
                "        column: {\n" +
                "            stacking: 'normal'\n" +
                "        }\n" +
                "    },\n" +
                "    chart: {\n" +
                "        type: 'column'\n" +
                "    },\n" +
                "    xAxis: {\n" +
                "        categories: [" + categories.stream().map(v -> "'" + v + "'").reduce((v1, v2) -> v1 + ", " + v2).get() + "]\n" +
                "    }\n" +
                "};");
        return chart;
    }
}
