# AoC



## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## run

`lein repl`

```
user=> (ns aoc.server)
aoc.server=> (start)
```

# api

## target_pressure


```json
{
"Action": "Anselm",
"Comment": "Get a select structure (object) of target pressures merged from current todos",
"TaskName": "anselm_get_target_pressures",
"RequestPath": "target_pressures"
}
```

curl http://localhost:50005/target_pressure

```json
{
  "ToExchange": {
    "Continue_mesaurement.Bool": false
  }
}
```
