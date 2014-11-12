##DDGClient

# Duckduckgo has shutdown or altered their API, so the application no longer works. #

* Android Studio learning project from Coursera course
* Runs on API 7 (Android 2.1 Eclair) or higher
* Look up the dictionary definition of any English word.
* Using REST API via HTTP GET request, sending to Duckduckgo public API
* Asynchronous network access using AsyncTaskLoader (previous @Deprecated implementation using AsyncTask and AsyncTask with wrapper still present in code for reference)
* AsyncTaskLoader communicates returned query results via local broadcast (previous @Deprecated implementation of passed DDGQueryObserver interface still present in code for reference)
* Using org.json.JSONObject to parse json returned by API
