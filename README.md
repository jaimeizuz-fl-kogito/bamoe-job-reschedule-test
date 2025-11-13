# Issue: Rescheduling a failed job doesn't reexecute the underlying node instance
## Description
The ```"HelloWorld"``` BPMN workflow contains a Service Task, ```"Dummy Service Task"```, which is marked with isAsync=true. The Service Task executes the Java method ```org.bamoe.test.DummyServiceTask.dummyExecutionByProcessInstanceId```, which throws an exception the first two times it's executed
(see code in ```org.bamoe.test.DummyServiceTask.dummyExecutionByProcessInstanceId```).

By default, the property ```kogito.jobs-service.maxNumberOfRetries``` is set to ```1```, so the Service Task will execute twice and will finally throw an exception. Thus, only a manual reschedule will make it complete correctly.

## How to reproduce:
1. Start the Kogito App in dev mode: ```mvn "-Pdevelopment" clean quarkus:dev```
   
2. Start a new process instance and save the Id from the response body:
```
curl --location 'http://localhost:8080/HelloWorld' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data-raw '{}'
```

3. After the Service Task fails twice, find the right Job Id using the Process Instance Id:
```
curl --location 'http://localhost:8080/graphql' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data '{
    "operationName": "getJobsByProcessInstanceId",
    "variables": {
        "processInstanceId": "<Here your ProcessInstanceId>"
    },
    "query": "query getJobsByProcessInstanceId($processInstanceId: String) {\n  Jobs(where: {processInstanceId: {equal: $processInstanceId}}) {\n    id\n    processId\n    processInstanceId\n    rootProcessId\n    status\n    expirationTime\n    priority\n    callbackEndpoint\n    repeatInterval\n    repeatLimit\n    scheduledId\n    retries\n    lastUpdate\n    endpoint\n    nodeInstanceId\n    executionCounter\n    __typename\n  }\n}\n"
}'
```

3. Reschedule the Job using the current time (or a time before the current date):
```
curl --location 'http://localhost:8080/graphql' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--data '{
    "operationName": "handleJobReschedule",
    "variables": {
        "jobId": "<Here your JobId>",
        "data": "{\"expirationTime\":\"2025-11-13T10:00:00.000Z\",\"repeatInterval\":0,\"repeatLimit\":0}"
    },
    "query": "mutation handleJobReschedule($jobId: String, $data: String) {\n  JobReschedule(id: $jobId, data: $data)\n}\n"
}'
```

## Expected behavior
After a manual reschedule of the job in ```ERROR``` status, the job status changes to ```EXECUTED```, the node instance is retriggered, ```Service Task completed correctly``` is shown in the application logs, and the process ends successfully in ```COMPLETED``` state.

## Current behavior
The status of the rescheduled job changes to ```EXECUTED``` but the node instance is not executed and the process instance remains in ```ERROR``` state.