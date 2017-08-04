package ru.bp.rtd.vaadin;

import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import org.apache.spark.sql.Row;
import org.vaadin.highcharts.HighChart;
import ru.bp.rtd.services.GBCrashAnalyzerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ChartsTab extends VerticalLayout {

    private boolean isInitialized = false;

    private final GBCrashAnalyzerService gbCrashAnalyzerService;
    private final String carModelsFile;
    private final String vehiclesFile;

    public ChartsTab(GBCrashAnalyzerService gbCrashAnalyzerService, String carModelsFile, String vehiclesFile) {
        this.gbCrashAnalyzerService = gbCrashAnalyzerService;
        this.carModelsFile = carModelsFile;
        this.vehiclesFile = vehiclesFile;

        Button button = new Button("show data");
        button.addClickListener((Button.ClickListener) event -> {
            if (!isInitialized) {
                GridLayout chartsLayout = new GridLayout(1, 1);
                chartsLayout.addComponent(createMostDangerousCarsChart());
                chartsLayout.addComponent(createCrashesCountByDriversAgeChart());
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
}
