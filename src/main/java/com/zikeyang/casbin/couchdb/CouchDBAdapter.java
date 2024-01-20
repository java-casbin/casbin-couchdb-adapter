package com.zikeyang.casbin.couchdb;

import static org.casbin.jcasbin.util.Util.splitCommaDelimited;
import com.zikeyang.casbin.couchdb.filter.Filter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.exception.CasbinAdapterException;
import org.casbin.jcasbin.model.Assertion;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.FilteredAdapter;
import org.casbin.jcasbin.util.Util;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

@Slf4j
public class CouchDBAdapter implements FilteredAdapter {
    private final CouchDbClient couchDbClient;
    private final String policyID;
    private boolean filtered;
    public CouchDBAdapter(CouchDbClient couchDbClient, String policyID) {
        if (couchDbClient == null) {
            throw new IllegalArgumentException("CouchDbClient cannot be null.");
        }
        this.couchDbClient = couchDbClient;
        this.policyID = policyID;
    }

    private PolicyDoc getPolicyDoc() throws CasbinAdapterException {

        if (!couchDbClient.contains(policyID)) {
            throw new CasbinAdapterException("Policy document does not exist.");
        }
        log.info("Get policy doc from {}", policyID);

        return couchDbClient.find(PolicyDoc.class, policyID);
    }

    public static void loadPolicyObject(Policy p, Model model) {
        model.addPolicy(p.getSec(), p.getPtype(), p.getRule());
    }

    @Override
    public void loadFilteredPolicy(Model model, Object filter) throws CasbinAdapterException {
        if (!(filter instanceof Filter)) {
            throw new CasbinAdapterException("Invalid filter type.");
        }

        for (Policy policy : getPolicyDoc().getPolicies()) {
            if (((Filter) filter).filterPolicy(policy)) {
                loadPolicyObject(policy, model);
            }
        }

        filtered = true;
    }

    @Override
    public boolean isFiltered() {
        return filtered;
    }

    @Override
    public void loadPolicy(Model model) {
        Optional.ofNullable(getPolicyDoc().getPolicies()).ifPresent(policies ->
                policies.forEach(
                        policy ->
                                loadPolicyObject(policy, model)));
    }

    private List<String> getModelPolicy(Model model, String ptype) {
        List<String> policy = new ArrayList<>();
        model.model.get(ptype).forEach((k, v) -> {
            List<String> p =
                    v.policy.parallelStream().map(x -> k + ", " + Util.arrayToString(x)).collect(Collectors.toList());
            policy.addAll(p);
        });
        return policy;
    }

    @Override
    public void savePolicy(Model model) {
    }

    @Override
    public void addPolicy(String sec, String ptype, List<String> rule) {
        PolicyDoc policyDoc = getPolicyDoc();

        // Create a new Policy object and set its properties
        Policy policy = new Policy();
        policy.setSec(sec);
        policy.setPtype(ptype);
        policy.setRule(rule);

        // Add the new Policy object to the PolicyDoc object
        policyDoc.getPolicies().add(policy);

        updatePolicy(policyDoc);
    }

    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        PolicyDoc policyDoc = getPolicyDoc();

        // Remove the Policy object from the PolicyDoc object
        policyDoc.getPolicies().removeIf(
                policy -> policy.getSec().equals(sec) && policy.getPtype().equals(ptype) && policy.getRule()
                        .equals(rule));

        updatePolicy(policyDoc);
    }

    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        PolicyDoc policyDoc = getPolicyDoc();

        // Remove the Policy objects that match the filter from the PolicyDoc object
        policyDoc.getPolicies().removeIf(
                policy -> policy.getSec().equals(sec) && policy.getPtype().equals(ptype) && Arrays.equals(
                        policy.getRule().subList(fieldIndex, fieldIndex + fieldValues.length).toArray(), fieldValues));

        updatePolicy(policyDoc);
    }

    private void updatePolicy(PolicyDoc policyDoc) {
        couchDbClient.update(policyDoc);
    }
}
