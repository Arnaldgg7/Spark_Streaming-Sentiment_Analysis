package exercise_2;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;
import twitter4j.Status;
import java.util.Arrays;

public class Exercise_2 {

	public static void get10MostPopularHashtagsInLast5min(JavaDStream<Status> statuses) {

		//Section 4.2.1

		JavaDStream<String> words = statuses
				.flatMap(t -> Arrays.asList(t.getText().split("\\s+")).iterator());

		JavaDStream<String> hashTags = words
				.filter(t -> t.startsWith("#"));
		//hashTags.print();


		//Section 4.2.2

		JavaPairDStream<String, Integer> tuples = hashTags.
				mapToPair(t -> new Tuple2<>(t, 1));

		JavaPairDStream<String, Integer> counts = tuples.
				reduceByKeyAndWindow((Function2<Integer, Integer, Integer>) (t1, t2) -> t1 + t2,
				(Function2<Integer, Integer, Integer>) (t1, t2) -> t1 - t2,
				new Duration(60*5*1000),
				new Duration(1*1000));

		//counts.print();

		//Section 4.2.3

		JavaPairDStream<Integer, String> swappedCounts = counts.mapToPair(Tuple2::swap);

		JavaPairDStream<Integer, String> sortedCounts = swappedCounts.transformToPair(t -> t.sortByKey(false));

		// We are getting the top 10 hashtags for each micro-batch (RDD) we are processing:
		sortedCounts.foreachRDD(t -> {
			System.out.println("Top 10 hashtags:");
			t.take(10).forEach(System.out::println);
		});
	}

}