# Testing

## selenium

The testing is split into 2 parts : 

* unit tests for the backend logic (limited)
* selenium tests for the frontend


## How to run the selenium tests.

The selenium tests require a docker container + the standard keycloack instance for the roles. The tests are only executed when you specify the ```-P selenium``` maven profile.