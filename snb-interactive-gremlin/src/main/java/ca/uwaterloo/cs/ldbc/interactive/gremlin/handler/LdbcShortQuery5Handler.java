package ca.uwaterloo.cs.ldbc.interactive.gremlin.handler;

import ca.uwaterloo.cs.ldbc.interactive.gremlin.Entity;
import ca.uwaterloo.cs.ldbc.interactive.gremlin.GremlinDbConnectionState;
import ca.uwaterloo.cs.ldbc.interactive.gremlin.GremlinUtils;
import com.ldbc.driver.DbConnectionState;
import com.ldbc.driver.DbException;
import com.ldbc.driver.OperationHandler;
import com.ldbc.driver.ResultReporter;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcShortQuery5MessageCreator;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcShortQuery5MessageCreatorResult;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by apacaci on 7/20/16.
 */
public class LdbcShortQuery5Handler implements OperationHandler<LdbcShortQuery5MessageCreator, DbConnectionState> {
    @Override
    public void executeOperation(LdbcShortQuery5MessageCreator ldbcShortQuery5MessageCreator, DbConnectionState dbConnectionState, ResultReporter resultReporter) throws DbException {
        Client client = ((GremlinDbConnectionState) dbConnectionState).getClient();
        Map<String, Object> params = new HashMap<>();
        params.put("label1", Entity.POST.getName());
        params.put("label2", Entity.COMMENT.getName());
        params.put("post_id", GremlinUtils.makeIid(Entity.POST, ldbcShortQuery5MessageCreator.messageId()));
        params.put("comment_id", GremlinUtils.makeIid(Entity.COMMENT, ldbcShortQuery5MessageCreator.messageId()));

        String statement = "  t = g.V().has(label1, 'iid', post_id); if(!t.clone().hasNext()) t = g.V().has(label2, 'iid', comment_id); t.out('hasCreator')";

        List<Result> results = null;
        try {
            results = client.submit(statement, params).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DbException("Remote execution failed", e);
        }

        Vertex creator = results.get(0).getVertex();

        LdbcShortQuery5MessageCreatorResult result = new LdbcShortQuery5MessageCreatorResult(GremlinUtils.getSNBId(creator), creator.<String>property("firstName").value(), creator.<String>property("lastName").value());

        resultReporter.report(1, result, ldbcShortQuery5MessageCreator);

    }
}
