Running the Autobahn Client tests
=================================

JSR-356 javax.websocket Client tests for [Autobahn WebSocket Protocol TestSuite](http://autobahn.ws/testsuite/)

Install the Autobahn wstest command line tool
---------------------------------------------

The full installation instructions can be found at [http://autobahn.ws/testsuite/installation]

    $ sudo apt-get install python python-dev python-twisted
    $ sudo apt-get install python-setuptools
    $ sudo easy_install autobahntestsuite

Run the Autobahn Fuzzing Server
-------------------------------

    $ wstest --mode=fuzzingserver

Let this run in a terminal window of its own.

Run the javax.websocket client tests against Autobahn
-----------------------------------------------------

Pick a Profile

  * `jetty`
  * `tomcat`
  * `tyrus`

Switch to another terminal window and run.

    $ cd javax.websocket-websocket-client
    $ mvn clean install
    $ mvn exec:exec -P{profile}

Once the test is complete, look in `reports/clients/index.html` for the result.

Tip: you can run all of the profiles and compare against each this way:

Start a fresh `fuzzingserver` and let it run for the entirety of these steps.

    $ cd javax.websocket-websocket-client
    $ mvn clean install
    $ mvn exec:exec -Pjetty
    $ mvn exec:exec -Ptomcat
    $ mvn exec:exec -Ptyrus
    
Now the report in `reports/clients/index.html` will show the results from all 3 profiles


