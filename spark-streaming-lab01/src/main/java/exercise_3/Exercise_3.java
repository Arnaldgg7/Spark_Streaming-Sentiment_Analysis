package exercise_3;

import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;
import scala.Tuple5;
import twitter4j.Status;
import java.util.*;
import java.util.stream.Collectors;

public class Exercise_3 {

	public static void sentimentAnalysis(JavaDStream<Status> statuses) {
		JavaPairDStream<Long, String> eng_tweets = statuses
				.filter(Objects::nonNull) // Checking for global 'nulls'
				.filter(t -> t.getText() != null) // Checking for 'nulls' just in the text, as sometimes happens.
				.filter(t -> LanguageDetector.isEnglish(t.getText()))
				.mapToPair(t -> new Tuple2<Long, String>(t.getId(), t.getText()))
				.mapValues(text -> text.replaceAll("[^a-zA-Z\\s]", "")
						.trim().toLowerCase())
				.filter(t -> t._2.length() > 0); // Checking for empty texts after the replacement.

		JavaPairDStream<Long, String> stem_tweets = eng_tweets
				.mapValues(t -> {
					List<String> tweet_stop_words = Arrays.stream(t.split("\\s+"))
							.filter(StopWords.getWords()::contains).collect(Collectors.toList());
					for (String word : tweet_stop_words) {
						t = t.replaceAll("\\s+"+word+"\\s+", " ");
					}
					return t;
				})
					// Checking that the Stop Words have not yielded an empty tweet:
				.filter(t -> t._2.trim().length() > 0);


		JavaPairDStream<Tuple2<Long,String>, Float> pos_tweets = stem_tweets.mapToPair(t -> {
			String[] tweet_words = t._2.split("\\s+");
			float n = tweet_words.length;
			float pos_words = Arrays.stream(tweet_words)
					.filter(PositiveWords.getWords()::contains).count();
			return new Tuple2<>(t, pos_words/n);
		}).filter(t -> t._2 != 0);


		JavaPairDStream<Tuple2<Long,String>, Float> neg_tweets = stem_tweets.mapToPair(t -> {
			String[] tweet_words = t._2.split("\\s+");
			float n = tweet_words.length;
			float neg_words = Arrays.stream(tweet_words)
					.filter(NegativeWords.getWords()::contains).count();
			return new Tuple2<>(t, neg_words/n);
		}).filter(t -> t._2 != 0);

		JavaDStream<Tuple5<Long, String, Float, Float, String>> final_joined =
				pos_tweets.join(neg_tweets).map(t -> {
					String sentiment;
					if (t._2._1 > t._2._2) {
						sentiment = "positive";
					}
					else if (t._2._1 < t._2._2) {
						sentiment = "negative";
					}
					else {
						sentiment = "neutral";
					}
					return new Tuple5<>(t._1._1, t._1._2, t._2._1, t._2._2, sentiment);
				});

		// Assessing if incoming tweets are talking about positive or negative sentiments, and even giving
		// a mark about 'how positive' or 'how negative' is each tweet:
		final_joined.print();

	}

}