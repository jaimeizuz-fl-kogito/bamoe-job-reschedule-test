package org.bamoe.test;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

@ApplicationScoped
public class DummyServiceTask {

    Logger logger = Logger.getLogger(DummyServiceTask.class);
    private Map<String, Integer> executionCount = new HashMap<String, Integer>();

    public void dummyExecutionByProcessInstanceId(String processInstanceId) {
        executionCount.put(
            processInstanceId,
            executionCount.getOrDefault(processInstanceId, 0) + 1);

        logger.info("Local execution counter for processInstanceId  [" + processInstanceId + "] = " + executionCount.get(processInstanceId));

        if(executionCount.get(processInstanceId) < 3) {
            throw new RuntimeException("SORRY, REPEAT PLEASE");
        } else {
            logger.info("Service Task completed correctly");
            executionCount.remove(processInstanceId);
        }
    }    
}
