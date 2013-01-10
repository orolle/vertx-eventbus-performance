vertx-eventbus-performance v0.1
==========================

Use EventbusPerformance to start the performance test. Its monitoring the MessagePingPong verticle.

For non-cluster:
Set EventbusPerformance.INSTANCES to your number of cores, which deploys that number of MessagePingPong on the eventbus.

For cluster:
Set EventbusPerformance.INSTANCES=1 and use the commandline to start your number of cores vert.x environments.
Use following parameters:

[vertx env 1] -cluster -cluster-host <your local ip of an interface> -cluster-port 25501

[vertx env 2] -cluster -cluster-host <your local ip of an interface> -cluster-port 25502

under linux you could use "sudo tcptrack -i lo" which shows the tcp traffic on loopback network interface where vertx envs will communicate over.
