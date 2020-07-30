Registering cabs here because all the business logic related to how to use cars should be here.

The Cab Emulator is just a pool of cars. This is a different business domain and can keep on adding
new cars/drivers, perform background checks and when everything is ready, register cab to taxi
service.

Cab Emulator could also possibly register a pool of cars to one taxi service and a pool of cars to
another taxi service.

Any driver who wants to go on a vacation could unregister from taxi service for a while but still
would be a part of Cab Emulator.

## APIs

### Add Vertex sample request -
```curl -XPOST localhost:8080/api/v1/maps --data '{"name":"My Place", "latitude":48.235345,"longitude":121.845972,"description":"Second Place","_csrf":"xxxx"}' -H "Content-Type:application/json" -v --cookie "XSRF-TOKEN=xxxx" -H "X-XSRF-TOKEN: xxxx"```