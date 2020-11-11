
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

`curl http://localhost:8009/target_pressure`

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

`curl http://localhost:8009/cal_ids`

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

## save_dut_branch

	  
```json
{
"Action": "Anselm",
"Comment": "Save name of dut branch for all devices.",
"TaskName": "anselm_save_dut",
"RequestPath": "save_dut_branch",
"Value": {
"DocPath": "Calibration.Measurement.AuxValues.Branch"
}
}
```

`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.AuxValues.Branch"}' -X POST http://localhost:8009/save_dut_branch`

```json
{"ok":true,"revs":["9-xxx"]}
```
or

```json
{"ok":true,"warn":"no doc selected"}
```


## save_maintainer

	  
```json
{
"Action": "Anselm",
"Comment": "Save name of maintainer.",
"TaskName": "anselm_save_maintainer",
"RequestPath": "save_maintainer",
"Value": {
	"DocPath": "Calibration.Measurement.Maintainer"
	}
}
```

```

`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.Maintainer"}' -X POST http://localhost:8009/save_maintainer`

```json
{"ok":true,"warn":"no maintainer selected"}
```
or

```json
{"ok":true,"revs":["11-abad27e1f4f8cd0a35870310d84f096e"]}
```
