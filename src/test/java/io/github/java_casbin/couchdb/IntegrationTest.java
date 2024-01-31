package io.github.java_casbin.couchdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;
import org.lightcouch.Response;

@Slf4j
public class IntegrationTest {
    private static CouchDbClient couchDbClient;
    private static Model model;

    String policy = """
            p, admin, domain1, data1, read
            p, admin, domain1, data1, write
            p, admin, domain2, data2, read
            p, admin, domain2, data2, write
                            
            g, alice, admin, domain1
            g, bob, admin, domain2""";

    @BeforeEach
    public void setUp() {
        // Create a CouchDbClient object
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("test_db")
                .setCreateDbIfNotExist(true)
                .setUsername("admin")
                .setPassword("admin")
                .setProtocol("http")
                .setHost("localhost")
                .setPort(5984)
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        couchDbClient = new CouchDbClient(properties);

        // Create a Model object and load model from string
        model = new Model();
        String text = """
                [request_definition]
                r = sub, dom, obj, act

                [policy_definition]
                p = sub, dom, obj, act

                [role_definition]
                g = _, _, _

                [policy_effect]
                e = some(where (p.eft == allow))

                [matchers]
                m = g(r.sub, p.sub, r.dom) && r.dom == p.dom && r.obj == p.obj && r.act == p.act""";
        model.loadModelFromText(text);

    }

    @AfterEach
    public void tearDown() {
        couchDbClient.context().deleteDB("test_db", "delete database");
    }

    @Test
    public void testPolicyDoc() {
        PolicyDoc policyDoc = new PolicyDoc(policy);

        Response response = couchDbClient.save(policyDoc);
        log.info("Save policy doc to {}", response.getId());

        policyDoc = couchDbClient.find(PolicyDoc.class, response.getId());
        assertEquals(policyDoc.getId(), response.getId());
    }

    @Test
    public void testLoadAndEnforcePolicy() {
        PolicyDoc policyDoc = new PolicyDoc(policy);
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        assertTrue(enforcer.enforce("alice", "domain1", "data1", "read"));
        enforcer.savePolicy();
    }

    @Test
    public void testRBACManagement() {
        PolicyDoc policyDoc = new PolicyDoc();
        Response res = couchDbClient.save(policyDoc);
        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        enforcer.addPermissionForUser("admin", "domain1", "data1", "read");
        enforcer.addPermissionForUser("user", "domain1", "data2", "read");
        enforcer.addRoleForUserInDomain("alice", "admin", "domain1");
        enforcer.addRoleForUserInDomain("bob", "user", "domain1");

        assertTrue(enforcer.enforce("alice", "domain1", "data1", "read"));
        assertFalse(enforcer.enforce("alice", "domain1", "data2", "read"));
        assertFalse(enforcer.enforce("bob", "domain1", "data1", "read"));

        enforcer.addPermissionForUser("admin", "domain1", "data2", "read");
        assertTrue(enforcer.enforce("alice", "domain1", "data2", "read"));
    }

    @Test
    public void testReloadPolicy() {
        PolicyDoc policyDoc = new PolicyDoc(policy);
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        assertTrue(enforcer.enforce("alice", "domain1", "data1", "read"));
        assertFalse(enforcer.enforce("alice", "domain1", "newdata", "read"));
        enforcer.savePolicy();

        Enforcer newEnforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        assertTrue(newEnforcer.enforce("alice", "domain1", "data1", "read"));
        newEnforcer.addPermissionForUser("admin", "domain1", "newdata", "read");
        assertTrue(newEnforcer.enforce("alice", "domain1", "newdata", "read"));
    }

    @Test
    public void testMultiEnforcer() {
        PolicyDoc policyDoc = new PolicyDoc(policy);
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer1 = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        assertTrue(enforcer1.enforce("alice", "domain1", "data1", "read"));
        assertFalse(enforcer1.enforce("alice", "domain1", "newdata", "read"));
        enforcer1.savePolicy();

        Enforcer enforcer2 = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        assertTrue(enforcer2.enforce("alice", "domain1", "data1", "read"));
        enforcer2.addPermissionForUser("admin", "domain1", "newdata", "read");
        assertTrue(enforcer1.enforce("alice", "domain1", "newdata", "read"));
    }

    @Test
    public void testDeleteData() {
        PolicyDoc policyDoc = new PolicyDoc(policy);
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        enforcer.removeFilteredPolicy(2, "data1");

        assertFalse(enforcer.enforce("alice", "domain1", "data1", "read"));
        assertFalse(enforcer.enforce("alice", "domain1", "data1", "write"));
    }

    @Test
    public void testRevokePermission() {
        PolicyDoc policyDoc = new PolicyDoc(policy);
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        enforcer.addPermissionForUser("alice", "default", "data1", "read");
        assertTrue(enforcer.enforce("alice", "default", "data1", "read"));
        enforcer.removeFilteredPolicy(0, "alice", "default", "data1");
        assertFalse(enforcer.enforce("alice", "default", "data1", "read"));
        assertTrue(
                enforcer.enforce("alice", "domain1", "data1", "read")); // Test that other permissions are not affected
    }

    @Test
    public void testSearchPermission() {
        PolicyDoc policyDoc = new PolicyDoc();
        Response res = couchDbClient.save(policyDoc);

        Enforcer enforcer = new Enforcer(model, new CouchDBAdapter(couchDbClient, res.getId()));

        enforcer.addPermissionForUser("alice", "default", "data1", "write");
        enforcer.addPermissionForUser("bob", "default", "data2", "write");
        enforcer.addPermissionForUser("bob", "default", "data2", "read");

        var list = enforcer.getFilteredPolicy(0, "alice", "default", "data1");

        assertEquals("[[alice, default, data1, write]]", list.toString());
        assertEquals("[[bob, default, data2, read]]", enforcer.getFilteredPolicy(0, "", "", "", "read").toString());
    }
}

