# Overview

![DevProxy](devproxy_main.png)

**DevProxy** is an interface written in [clojure](https://clojure.org/).
**DevProxy** allows the implementation of measurement sequences that are
independent of client or customer devices. It is therefore a **dev**ice **proxy**

```
 ┌────────────────────────────────────────────────────┐
 │                                                    │
 │                      Metis                         │
 │                                                    │
 │                                                    │
 └───────▲────────────────────────────────────▲───────┘
         │                                    │
 ┌───────▼───────────┐                        │
 │                   │                        │
 │      DevProxy     │                        │
 │                   │                        │
 └───────▲───────────┘                        │
         │                                    │
 ┌───────▼────────────────────────────────────▼───────┐
 │                                                    │
 │    DevHub                              DevHub      │
 │                                                    │
 └─▲───▲───▲───────────────────────────▲───▲───▲───▲──┘
   │   │   │                           │   │   │   │
   │   │   │                           │   │   │   │
   │   │   │   ┌───────────────────┐   │   │   │   │
   │   │   │   │                   │   │   │   │   │
   │   │   │   │  DUT 1            │   │   │   │   │
   │   │   │   │                   │   │   │   │   │
   │   │   │   │ ┌───────────────┐ │   │   │   │   │  ┌───────────────────────────────┐
   │   │   └───┼─► TCP device    │ │   │   │   │   │  │                               │
   │   │       │ ┌───────────────┤ │   │   │   │   │  │  Standard                     │
   │   │       └─┴───────────────┴─┘   │   │   │   │  │                               │
   │   │                               │   │   │   │  │      valves, motors           │
   │   │       ┌───────────────────┐   │   │   │   │  │                               │
   │   │       │                   │   │   │   │   │  │      pressure sensors         │
   │   │       │  DUT 2            │   │   │   │   │  │                               │
   │   │       │                   │   │   │   │   │  │      temperature sensors      │
   │   │       │ ┌───────────────┐ │   │   │   │   │  │                               │
   │   └───────┼─► MODBUS device │ │   │   │   │   │  │       ┌───────────────┐       │
   │           │ ┌───────────────┤ │   │   │   │   └──┼───────► VXI11 device 1│       │
   │           └─┴───────────────┴─┘   │   │   │      │       ┌───────────────┤       │
   │                                   │   │   │      │       └───────────────┤       │
   │           ┌───────────────────┐   │   │   └──────┼───────► TCP device 1  │       │
   │           │                   │   │   │          │       ┌───────────────┤       │
   │           │  DUT n            │   │   │          │       └───────────────┤       │
   │           │                   │   │   └──────────┼───────► MODBUS device │       │
   │           │ ┌───────────────┐ │   │              │       ┌───────────────┤       │
   └───────────┼─► TCP device    │ │   │              │       └───────────────┤       │
               │ ┌───────────────┤ │   └──────────────┼───────► TCP device n  │       │
               └─┴───────────────┴─┘                  └───────────────────────┴───────┘
```


# Documentation

[API documentation](https://a75438.berlin.ptb.de/devproxy/docs/index.html)

## Configuration

Default configurations are in `resources/conf.edn`. Some entries may be overwritten by environment variables:

* `DEVPROXY_FACILITY` (fallbacks: DEVHUB_FACILITY, METIS_FACILITY)
* `DEVHUB_HOST` (default `localhost`)
* `DEVHUB_PORT` (default `8009`)
* `REDIS_HOST` (default `localhost`)
* `COUCH_HOST`
* `CAL_USR`
* `CAL_PWD`

# Start

At the [nrepl prompt](https://nrepl.org/nrepl/index.html) type:

```clojure
(start)
```

This starts a server [localhost (default port: 8009)](http://localhost:8009).

The folder `devproxy/target/uberjar/` contains a standalone version of
**DevProxy** (build with `lein uberjar`). Run with:

```shell
java -jar devproxy/target/uberjar/devproxy-x.y.z-standalone.jar
```

## System integration

`systemd` configuration:

```shell
cd /path/to/devproxy
sudo mkdir /usr/local/share/devproxy
sudo cp devproxy.jar /usr/local/share/devproxy
sudo cp devproxy.service  /etc/systemd/system/
sudo systemctl enable devproxy.service
sudo systemctl start devproxy.service
sudo systemctl status devproxy.service
```

### `tools.deps` and `tools.build`

Build a stand alone app with:

```shell
clj -T:build clean
clj -T:build prep
clj -T:build uber
```

Run it with:

```shell
java -jar target/devproxy-x.y.z-standalone.jar
```


## Generate api docs

```shell
clojure -X:dev:codox
```

upload:

```shell
scp -r docs/ bock04@a75438://var/www/html/devproxy/
```

## Notes

* `[clojure-interop/java.nio "1.0.5"]`
* https://cljdoc.org/d/clojure-interop/java.nio/1.0.5
*  overcome `SSL peer shut down incorrectly` error by:
```shell
export JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2
```
