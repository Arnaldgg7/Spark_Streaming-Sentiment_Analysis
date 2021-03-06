# Spark_Streaming-Sentiment_Analysis
A Spark Streaming program that ranks top-10 worldwide tweets each second, and performs sentiment analysis to assess the positivity or negativity of all tweets.

Firstly, this Spark Streaming program performs a rank of top-10 hashtags in all tweets worldwide, every second, by means of of a direct connection to the Twitter API. Therefore, as Spark Streaming uses a micro-batch streaming processing system, we are slicing the entire stream of tweets worldwide in RDDs of 1 second (so, getting all tweets published every second) and sorting them by frequency of appearance of the existing hashtags within them.

Secondly, we go even further in the analysis of the information that the stream of tweets gives us, and we use Information Retrieval techniques by means of 3 dictionaries (included in the program):
- Stop Words.
- Positive Words.
- Negative Words.

With this information and the stream of incoming tweets, we analyze each tweet to determine if it holds an overall positive, negative or even neutral sentiment. Finally, we rank each tweet accordingly and we conclude if it ends up being positive, negative or neutral. Here, we use a micro-batch of 5 seconds to balance the processing complexity of each tweet and the number of incoming tweets to process in each micro-batch.
