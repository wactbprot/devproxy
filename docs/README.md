# Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

# Start

Run the shell commands:

```shell
git clone https://github.com/wactbprot/devproxy
cd devproxy
lein repl

## =>
## EPL-y 0.4.3, nREPL 0.6.0
## Clojure 1.10.0
## OpenJDK 64-Bit Server VM 11.0.9.1+1-Ubuntu-0ubuntu1.20.04
##     Docs: (doc function-name-here)
##           (find-doc "part-of-name-here")
##   Source: (source function-name-here)
##  Javadoc: (javadoc java-object-or-class-here)
##     Exit: Control+D or (exit) or (quit)
##  Results: Stored in vars *1, *2, *3, an exception in *e

devproxy.server=>
```


At the [nrepl prompt](https://nrepl.org/nrepl/index.html) type:

```clojure
(start)
```
java -jar 


This starts a server [localhost (default port: 8009)](http://localhost:8009).

# Documentation

* [API documentation](./api)
* [Curl examples](./examples.md)


## Sequences

TODO: document what happens if ...

```clojure
(defn auto-init-tasks    [conf row] (tasks-by-pat conf row "auto_init*"))
(defn range-ind-tasks    [conf row] (tasks-by-pat conf row "range_ind*"))
(defn ind-tasks          [conf row] (tasks-by-pat conf row "ind*"))
(defn range-offset-tasks [conf row] (tasks-by-pat conf row "range_offset*"))
(defn offset-tasks       [conf row] (tasks-by-pat conf row "offset*"))
(defn auto-offset-tasks  [conf row] (tasks-by-pat conf row "auto_offset*"))

(defn sequences-tasks
  [conf row]
  (let [i (auto-init-tasks    conf row)
        r (range-offset-tasks conf row)
        o (auto-offset-tasks  conf row)]
  (if (empty? r) (interleave i o) (interleave i r o))))
```
