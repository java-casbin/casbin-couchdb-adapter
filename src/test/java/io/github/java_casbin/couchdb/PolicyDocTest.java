package io.github.java_casbin.couchdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PolicyDocTest {
    @Test
    public void testSerDePolicyDoc() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a PolicyDoc object and set its properties
        PolicyDoc policyDoc = new PolicyDoc();
        policyDoc.setId("test_id");
        policyDoc.setRev("test_rev");
        Policy policy = new Policy();
        policy.setSec("p");
        policy.setPtype("p1");
        policy.setRule(Arrays.asList("alice", "data1", "read"));
        policyDoc.setPolicies(List.of(policy));

        // Serialize the PolicyDoc object to a JSON string
        String jsonString = objectMapper.writeValueAsString(policyDoc);
        log.info("jsonString: {}", jsonString);

        // Deserialize the JSON string back to a PolicyDoc object
        PolicyDoc deserializedPolicyDoc = objectMapper.readValue(jsonString, PolicyDoc.class);

        // Verify that the original and deserialized PolicyDoc objects are equal
        assertEquals(policyDoc, deserializedPolicyDoc);
    }

    @Test
    public void testEmptyPolicyDoc() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create an empty PolicyDoc object
        PolicyDoc policyDoc = new PolicyDoc();

        // Serialize the PolicyDoc object to a JSON string
        String jsonString = objectMapper.writeValueAsString(policyDoc);

        // Deserialize the JSON string back to a PolicyDoc object
        PolicyDoc deserializedPolicyDoc = objectMapper.readValue(jsonString, PolicyDoc.class);

        // Verify that the original and deserialized PolicyDoc objects are equal
        assertEquals(policyDoc, deserializedPolicyDoc);
    }

    @Test
    public void testMultiplePolicies() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a PolicyDoc object with multiple policies
        PolicyDoc policyDoc = new PolicyDoc();
        Policy policy1 = new Policy();
        policy1.setSec("p");
        policy1.setPtype("p1");
        policy1.setRule(Arrays.asList("alice", "data1", "read"));
        Policy policy2 = new Policy();
        policy2.setSec("p");
        policy2.setPtype("p2");
        policy2.setRule(Arrays.asList("bob", "data2", "write"));
        policyDoc.setPolicies(Arrays.asList(policy1, policy2));

        // Serialize the PolicyDoc object to a JSON string
        String jsonString = objectMapper.writeValueAsString(policyDoc);

        // Deserialize the JSON string back to a PolicyDoc object
        PolicyDoc deserializedPolicyDoc = objectMapper.readValue(jsonString, PolicyDoc.class);

        // Verify that the original and deserialized PolicyDoc objects are equal
        assertEquals(policyDoc, deserializedPolicyDoc);
    }
    @Test
    public void testLoadPolicyStringToPolicyDoc() {
        String policy = """
            p, admin, domain1, data1, read
            p, admin, domain1, data1, write
            p, admin, domain2, data2, read
            p, admin, domain2, data2, write

            g, alice, admin, domain1
            g, bob, admin, domain2""";

        PolicyDoc policyDoc = new PolicyDoc(policy);

        // Assert that the id and rev of the PolicyDoc are null
        assertNull(policyDoc.getId());
        assertNull(policyDoc.getRev());

        // Assert that the policies list is not null and has the correct size
        assertNotNull(policyDoc.getPolicies());
        assertEquals(6, policyDoc.getPolicies().size());

        // Assert the properties of each policy
        assertPolicy(policyDoc.getPolicies().get(0), "p", "p", Arrays.asList("admin","domain1", "data1", "read"));
        assertPolicy(policyDoc.getPolicies().get(1), "p", "p", Arrays.asList("admin","domain1", "data1", "write"));
        assertPolicy(policyDoc.getPolicies().get(2), "p", "p", Arrays.asList("admin","domain2", "data2", "read"));
        assertPolicy(policyDoc.getPolicies().get(3), "p", "p", Arrays.asList("admin","domain2", "data2", "write"));
        assertPolicy(policyDoc.getPolicies().get(4), "g", "g", Arrays.asList("alice", "admin", "domain1"));
        assertPolicy(policyDoc.getPolicies().get(5), "g", "g", Arrays.asList("bob", "admin", "domain2"));
    }

    private void assertPolicy(Policy policy, String expectedSec, String expectedPtype, List<String> expectedRule) {
        assertEquals(expectedSec, policy.getSec());
        assertEquals(expectedPtype, policy.getPtype());
        assertEquals(expectedRule, policy.getRule());
    }

}
