# Arvind's forex proxy

### Concept
The idea is to create a local proxy that polls forex rates and stores it locally in the cache.
The polling is set to a default of 90 seconds since we're rate limited to 1000 calls per day. 90 seconds is the default as a day has 86400 seconds and 86400 seconds/90 = 960 which is slightly < than our rate limit.

This project uses a shared memory via a Ref to store the currency rates.

**Assumption**: We are only going to need to store abcUSD pairs like GBPUSD. The cross pairs like EURCHF rates can be derived through the EURUSD AND CHFUSD rates. 


### Design
The design contains the following classes:

* RateClientProxy (to make the API calls based on the UrlConfig)
* RateWriter (responsible for writing into the shared memory) 
* Main app (schedules based on the PollConfig)
* RateService (reads the rates from the shared memory and returns to our API)
* SharedState (a map that contains the latest set of forex rates)

### How to run

Pull the code to your local. Use `sbt compile` to compile.
Use `sbt test` to run unit tests

Run the docker image of paidy-one-frame service using `docker run -p 8080:8080 paidyinc/one-frame`
Use `sbt run` to run the project. This will start the server on port 8081.
You can make a GET request to http://0.0.0.0:8081/rates?from=GBP&to=SGD to fetch the rate.

**Ambiguities** - if external API is down, what do we do with the result? Do we return the stale (> 5 min) results?
Do we return not found? Perhaps it makes more sense to send the forex rate with the timestamp so we delegate this choice to the consumer.
This is the choice I have taken.

**Future Extensions** - we can store the cross pair information directly in the cache if it's commonly accessed by the app
In case the list of forex pairs that is needed is very large and isn't supported in a single GET request, we can also have a timestamped/LRU cache based on recent requests, that polls only a subset of pairs each time and uses adhoc queries for the ones not found in the LRU cache.
Another alternative is we split pairs in batches (our current design of 90 sec. polling allows us atleast 2 different alternating batches) 
and makes adhoc requests to new pairs directly to the api 
