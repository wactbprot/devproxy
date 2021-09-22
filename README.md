![devproxy](devproxy_main.png)

(**devproxy**) is a device proxy and interface written in [clojure](https://clojure.org/).

See documentation on [wactbprot.github.io](https://wactbprot.github.io/devproxy/)

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

## configuration

Default configurations are in `resources/conf.edn`. Some entries may be overwritten by environment variables:

* DEVPROXY_FACILITY (fallbacks: DEVHUB_FACILITY, METIS_FACILITY)
* DEVHUB_HOST
* DEVHUB_PORT
* REDIS_HOST
* COUCH_HOST


## systemd

```shell
cd /path/to/devproxy
sudo mkdir /usr/local/share/devproxy
sudo cp devproxy.jar /usr/local/share/devproxy
sudo cp devproxy.service  /etc/systemd/system/
sudo systemctl enable devproxy.service
sudo systemctl start devproxy.service
sudo systemctl status devproxy.service
```

### tools.deps and tools.build

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

