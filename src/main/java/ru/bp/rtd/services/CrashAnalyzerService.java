package ru.bp.rtd.services;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.explode;

@Service
public class CrashAnalyzerService {

    @Autowired
    private JavaSparkContext javaSparkContext;
    @Autowired
    private SparkSession spark;

    public long getCrashCount(String file) {
        Dataset<Row> dataset = spark.read()
                .json(file).select(explode(col("items")).as("item"));

        Dataset<Row> processedDataSet = dataset.select(
                col("item.em_moment_date"),
                col("item.em_moment_time"),
                col("item.em_type_code"),
                col("item.em_type_name"),
                col("item.here_road_constructions"),
                col("item.id"),
                col("item.latitude"),
                col("item.light_type_code"),
                col("item.light_type_name"),
                col("item.longitude"),
                col("item.loss_amount"),
                col("item.loss_child_amount"),
                col("item.motion_influences"),
                col("item.mt_rate_code"),
                col("item.mt_rate_name"),
                col("item.okato_code"),
                col("item.participants"),
                col("item.place_path"),
                col("item.region_code"),
                col("item.region_name"),
                col("item.road_code"),
                col("item.road_drawbacks"),
                col("item.road_loc"),
                col("item.road_loc_m"),
                col("item.road_name"),
                col("item.road_significance_code"),
                col("item.road_significance_name"),
                col("item.road_type_code"),
                col("item.road_type_name"),
                col("item.subject"),
                col("item.suffer_amount"),
                col("item.suffer_child_amount"),
                col("item.tr_area_state_code"),
                col("item.tr_area_state_name"),
                col("item.transp_amount"),
                col("item.vehicles")
        );

        processedDataSet.printSchema();
        processedDataSet.show();

        Dataset<Row> vehicles = dataset.select(
                col("item.vehicles.damage_dispositions"),
                col("item.vehicles.okfs_code"),
                col("item.vehicles.okfs_name"),
                col("item.vehicles.prod_type_code"),
                col("item.vehicles.prod_type_name"),
                col("item.vehicles.rudder_type_code"),
                col("item.vehicles.rudder_type_name"),
                col("item.vehicles.technical_failures"),
                col("item.vehicles.tyre_type_code"),
                col("item.vehicles.tyre_type_name"),
                col("item.vehicles.vl_sort"),
                col("item.vehicles.vl_year")
        );

        vehicles.printSchema();
        vehicles.show();


        return dataset.count();
    }
}
