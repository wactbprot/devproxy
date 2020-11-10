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

curl http://localhost:8009/target_pressure

```json
{
  "ToExchange": {
    "Continue_mesaurement.Bool": false
  }
}
```


## cal_ids

```json
{
"Action": "Anselm",
"Comment": "Get calibration ids from Anselm api",
"TaskName": "anselm_get_cal_ids",
"RequestPath": "cal_ids"
}
   
```

curl http://localhost:8009/cal_ids

```json
{
  "ToExchange": {
    "Ids": "cal-2020-se3-ik-4007_0001@cal-2020-se3-ik-4025_0002"
  }, 
  "ids": [
    "cal-2020-se3-ik-4007_0001", 
    "cal-2020-se3-ik-4025_0002"
  ]
}

```
