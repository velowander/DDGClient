# Duckduckgo has shutdown their full public API, so this Android application no longer works.
## However, it does showcase how to write an application that uses broadcast messages, communicates with a REST API, accesses the network on a background thread, and parses the JSON response send by a server.

* Android Studio learning project from Coursera course
* Runs on API 7 (Android 2.1 Eclair) or higher
* Look up the dictionary definition of any English word.
* Using REST API via HTTP GET request, sending to Duckduckgo public API
* Asynchronous network access using AsyncTaskLoader (previous @Deprecated implementation using AsyncTask and AsyncTask with wrapper still present in code for reference)
* AsyncTaskLoader communicates returned query results via local broadcast (previous @Deprecated implementation of passed DDGQueryObserver interface still present in code for reference)
* Using org.json.JSONObject to parse json returned by API
