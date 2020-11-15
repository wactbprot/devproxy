
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

## target_pressure [GET]


```json
{
"Action": "Anselm",
"Comment": "Get a select structure (object) of target pressures merged from current todos",
"TaskName": "anselm_get_target_pressures",
"RequestPath": "target_pressure"
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
or

```json
{
 "ToExchange":{
		"Target_pressure.Selected":1.0,
		"Target_pressure.Unit":"Pa",
		"Continue_mesaurement.Bool":true}
}
```

## cal_ids [GET]

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

## save_dut_branch [POST]


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


## save_maintainer [POST]


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

`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.Maintainer"}' -X POST http://localhost:8009/save_maintainer`


```json
{"ok":true,"warn":"no maintainer selected"}
```
or

```json
{"ok":true,"revs":["11-abad27e1f4f8cd0a35870310d84f096e"]}
```

## save_gas [POST]

```json
{
"Action": "Anselm",
"Comment": "Save name of gas.",
"TaskName": "anselm_save_gas",
"RequestPath": "save_gas",
"Value": {
    "DocPath": "Calibration.Measurement.AuxValues.Gas"
     }
}
```

`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.AuxValues.Gas"}' -X POST http://localhost:8009/save_gas`


```json
{"ok":true,"warn":"no gas selected"}
```
or

```json
{"ok":true,"revs":["12-fa88485ff30bcb544cba85c8d1d61ad9"]}
```


## dut_max [POST]

```json
{
"Action": "Anselm",
"Comment": "Get maximum pressure for each dut branch in the current configuration.",
"TaskName": "anselm_get_dut_max",
"RequestPath": "dut_max",
"FromExchange": {
	"@target_pressure": "Target_pressure.Selected",
	"@target_unit": "Target_pressure.Unit"
	},
"Value": {
	"DocPath": "Calibration.Measurement.Values.Position",
	"Target_pressure_value": "@target_pressure",
	"Target_pressure_unit": "@target_unit"
	}
}
```

### 50005:
`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.Valves.Position", "Target_pressure_value": "10", "Target_pressure_unit":"Pa" }' -X POST http://localhost:50005/dut_max`

```json
"ToExchange": {
    "Dut_A": {
      "Type": "dut_max_a",
      "Unit": "Pa",
      "Value": 133.322
    },
    "Dut_B": {
      "Type": "dut_max_b",
      "Unit": "Pa",
      "Value": 0.0
    },
    "Dut_C": {
      "Type": "dut_max_c",
      "Unit": "Pa",
      "Value": 0.0
    },
    "Set_Dut_A": "open",
    "Set_Dut_B": "close",
    "Set_Dut_C": "close"
  }
}
```
### 8009:
`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.Valves.Position", "Target_pressure_value": 10.099, "Target_pressure_unit":"Pa" }' -X POST http://localhost:8009/dut_max`

```json
{"ToExchange":{
	"Dut_A":{"Unit":"Pa","Display":"0.1mbar","Value":10.0,"Type":"dut_max_a"},
	"Dut_B":{"Unit":"Pa","Display":"10Torr","Value":1333.22,"Type":"dut_max_b"},
	"Dut_C":{"Unit":"Pa","Display":"SRG","Value":2.0,"Type":"dut_max_c"},
	"Set_Dut_A":"close",
	"Set_Dut_B":"open",
	"Set_Dut_C":"close"}
	}
```

## target_pressures


`curl http://localhost:50005/target_pressures`

### 50005:

```json
{
  "ToExchange": {
    "Target_pressure": {
      "Caption": "target pressure", 
      "Select": [
        {
          "display": "1.00e+00 Pa", 
          "value": "1.00e+00"
        }, 
        {
          "display": "2.00e+00 Pa", 
          "value": "2.00e+00"
        }, 
        {
          "display": "3.00e+00 Pa", 
          "value": "3.00e+00"
        }, 
        {
          "display": "5.00e+00 Pa", 
          "value": "5.00e+00"
        }, 
        {
          "display": "7.00e+00 Pa", 
          "value": "7.00e+00"
        }, 
        {
          "display": "1.00e+01 Pa", 
          "value": "1.00e+01"
        }, 
        {
          "display": "2.00e+01 Pa", 
          "value": "2.00e+01"
        }, 
        {
          "display": "3.00e+01 Pa", 
          "value": "3.00e+01"
        }, 
        {
          "display": "5.00e+01 Pa", 
          "value": "5.00e+01"
        }, 
        {
          "display": "7.00e+01 Pa", 
          "value": "7.00e+01"
        }, 
        {
          "display": "1.00e+02 Pa", 
          "value": "1.00e+02"
        }
      ], 
      "Selected": "1.00e+00", 
      "Unit": "Pa"
    }
  }
}
```
### 8009:

curl http://localhost:8009/target_pressures
```json
{
	"ToExchange": {
		"Target_pressure": {
			"Caption": "target pressure",
			"Select": [{
				"display": "0.013 Pa",
				"value": "0.013"
			}, {
				"display": "0.02 Pa",
				"value": "0.02"
			}, {
				"display": "0.03 Pa",
				"value": "0.03"
			}, {
				"display": "0.05 Pa",
				"value": "0.05"
			}, {
				"display": "0.09 Pa",
				"value": "0.09"
			}, {
				"display": "0.13 Pa",
				"value": "0.13"
			}, {
				"display": "0.2 Pa",
				"value": "0.2"
			}, {
				"display": "0.3 Pa",
				"value": "0.3"
			}, {
				"display": "0.5 Pa",
				"value": "0.5"
			}, {
				"display": "0.9 Pa",
				"value": "0.9"
			}, {
				"display": "1.0 Pa",
				"value": "1.0"
			}, {
				"display": "1.3 Pa",
				"value": "1.3"
			}, {
				"display": "2.0 Pa",
				"value": "2.0"
			}, {
				"display": "3.0 Pa",
				"value": "3.0"
			}, {
				"display": "5.0 Pa",
				"value": "5.0"
			}, {
				"display": "7.0 Pa",
				"value": "7.0"
			}, {
				"display": "9.0 Pa",
				"value": "9.0"
			}, {
				"display": "10.0 Pa",
				"value": "10.0"
			}, {
				"display": "13.0 Pa",
				"value": "13.0"
			}, {
				"display": "20.0 Pa",
				"value": "20.0"
			}, {
				"display": "30.0 Pa",
				"value": "30.0"
			}, {
				"display": "50.0 Pa",
				"value": "50.0"
			}, {
				"display": "70.0 Pa",
				"value": "70.0"
			}, {
				"display": "90.0 Pa",
				"value": "90.0"
			}, {
				"display": "100.0 Pa",
				"value": "100.0"
			}, {
				"display": "130.0 Pa",
				"value": "130.0"
			}],
			"Selected": "0.013",
			"Unit": "Pa"
		}
	}
}
```

## offset_sequences [POST]

```json
{
"Action": "Anselm",
"Comment": "Starts the offset sequence for saving offset samples",
"TaskName": "anselm_offset_sequences",
"RequestPath": "offset_sequences",
"Value": {
	"DocPath": "Calibration.Measurement.AuxValues.Pressure"
 }
}
```

`curl  -H "Content-Type: application/json" -d '{"DocPath": "Calibration.Measurement.AuxValues.Pressure"}' -X POST http://localhost:8009/offset_sequences`



## offset [POST]

```json
{
"Action": "Anselm",
"Comment": "Executes the initialisation and measurement of the offset.",
"TaskName": "anselm_offset",
"RequestPath": "offset",
"FromExchange": {
	"@target_pressure": "Target_pressure.Selected",
	"@target_unit": "Target_pressure.Unit"
},
"Value": {
	"Target_pressure_value": "@target_pressure",
	"Target_pressure_unit": "@target_unit"
}
},
```	 

`curl  -H "Content-Type: application/json" -d '{"Target_pressure_value": "10","Target_pressure_unit": "Pa"}' -X POST http://localhost:8009/offset`


## ind [POST]

```json
{
"Action": "Anselm",
"Comment": "Executes the initialisation and measurement of the indication.",
"TaskName": "anselm_ind",
"RequestPath": "ind",
"FromExchange": {
	"@target_pressure": "Target_pressure.Selected",
	"@target_unit": "Target_pressure.Unit"
},
"Value": {
	"Target_pressure_value": "@target_pressure",
	"Target_pressure_unit": "@target_unit"
  }
}
```


`curl  -H "Content-Type: application/json" -d '{"Target_pressure_value": "10","Target_pressure_unit": "Pa"}' -X POST http://localhost:8009/ind`
