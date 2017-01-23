# Testing

## selenium

The testing is split into 2 parts : 

* unit tests for the backend logic (limited)
* selenium tests for the frontend


## How to run the selenium tests.

The selenium tests require a docker container + the standard keycloack instance for the roles. The docker container can be started by the ``dockerTest.sh`` script. This will launch a
container where the persistence is linked to the target folder. This way the selenium tests can instrument the container via the ```target/data``` folder, in order to allow each test to
be executed in isolation.

The selenium tests are only executed when you specify the ```-P selenium``` maven profile.