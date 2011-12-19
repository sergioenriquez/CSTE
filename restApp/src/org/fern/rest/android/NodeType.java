package org.fern.rest.android;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the different types of nodes.
 * 
 * @author Sergio Enriquez
 * @author Andrew Hays
 */
public enum NodeType {
    /*
     * TODO (NodeType) is there any better way to be able to "work backwards"
     * than initializing a static Map?
     */
    NODE_TASK_NAME("taskName"), NODE_TASK_TYPE("taskType"), NODE_TASK_DETAIL(
            "taskDetail"), NODE_TASK_STATUS("taskStatus"), NODE_TASK_PRIORITY(
            "taskPriority"), NODE_TASK_ACTIVATION_TIME("taskActivationTime"), NODE_TASK_EXPIRATION_TIME(
            "taskExpirationTime"), NODE_TASK_ADDITION_TIME("taskAdditionTime"), NODE_TASK_MODIFICATION_TIME(
            "taskModificationTime"), NODE_TASK_PROGRESS("taskProgress"), NODE_TASK_PROCESS_PROGRESS(
            "processProgress"), NODE_TASK_DEPENDENT_TASK("dependentTask"), NODE_TASK_ESTIMATED_DURATION(
            "estimatedDuration"), NODE_LINK("link"), NODE_TASK_TAG("taskTag"), NODE_NAME(
            "name"), NODE_START("tm");

    private final String                       nodeName;
    private static final Map<String, NodeType> reverse;

    static {
        Map<String, NodeType> reverseInit = new HashMap<String, NodeType>();

        reverseInit.put("taskName", NODE_TASK_NAME);
        reverseInit.put("taskType", NODE_TASK_TYPE);
        reverseInit.put("taskDetail", NODE_TASK_DETAIL);
        reverseInit.put("taskStatus", NODE_TASK_STATUS);
        reverseInit.put("taskPriority", NODE_TASK_PRIORITY);
        reverseInit.put("taskActivationTime", NODE_TASK_ACTIVATION_TIME);
        reverseInit.put("taskExpirationTime", NODE_TASK_EXPIRATION_TIME);
        reverseInit.put("taskAdditionTime", NODE_TASK_ADDITION_TIME);
        reverseInit.put("taskModificationTime", NODE_TASK_MODIFICATION_TIME);
        reverseInit.put("taskProgress", NODE_TASK_PROGRESS);
        reverseInit.put("processProgress", NODE_TASK_PROCESS_PROGRESS);
        reverseInit.put("dependentTask", NODE_TASK_DEPENDENT_TASK);
        reverseInit.put("estimatedDuration", NODE_TASK_ESTIMATED_DURATION);
        reverseInit.put("link", NODE_LINK);
        reverseInit.put("taskTag", NODE_TASK_TAG);
        reverseInit.put("name", NODE_NAME);
        reverseInit.put("tm", NODE_START);

        reverse = Collections.unmodifiableMap(reverseInit);
    }

    NodeType(String nodeName) {
        this.nodeName = nodeName;
    }

    public static NodeType fromNodeName(String in) {
        if (reverse.containsKey(in)) {
            return reverse.get(in);
        } else {
            return null;
        }
    }

    public String nodeName() {
        return this.nodeName;
    }

    public String toString() {
        return this.nodeName;
    }
}
