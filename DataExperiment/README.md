# DataExperiment.

## To Import Data:

`cd bin`

`./mesh.sh -f <file path> -b <buffer size>`

## To Observe current working status:

`cd logs`

`tail -f info.log | grep "statis"`

## To Shutdown the Importer:

`cd bin`

`./dataImportConsole.sh stop`

## To Execute a query for the number of all edges incident from vertex n

`cd bin`

`./mesh -q n -c`

## To Execute a query for all edges incident from vertex n

`cd bin`

`./mesh -q n`

## To Execute a query for the edge incident from vertex a to vertex b

`cd bin`

`./mesh -q a -d b`