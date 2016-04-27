# COEN 272 - Web Search and Information Retrieval
# Project 1 - Web Crawler

There are three .jar files for running the project provided in out/artifacts.

You can run the programs as follows:
1. java -jar webcrawler.jar <.csv file>
2. java -jar contentmanager.jar
3. java -jar wordanalyzer.jar

The .csv file provided as an argument for the webcrawler should be a .csv file that is formatted as follows:

	<seed URL>,<# of pages to crawl>,[<domain name>]

ex.

	http://yahoo.com,10

OR

	http://yahoo.com,10,http://yahoo.com
