package x.spirit.dataimporter.datatarget;

import mr.x.meshwork.edge.Edge;
import mr.x.meshwork.edge.GraphStorage;
import org.apache.commons.collections.CollectionUtils;
import x.spirit.sandglass.DataTarget;
import x.spirit.sandglass.PostProcessException;
import x.spirit.sandglass.PreProcessException;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhangwei on 4/14/15.
 *
 * @author zhangwei
 */
public class MeshworkTarget extends DataTarget<Edge>{

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    //@Resource(name = "hybridGraphStorage")
    private GraphStorage graphDao;

    @Override
    public void preAction(Collection<Edge> lst) throws PreProcessException {
        if ((!initialized.get())) {
            System.out.println("Need to Initialize");
            String param = this.otherParams.get("initTable");
            System.out.println("param : " + param);
            Boolean initTable = Boolean.valueOf(param.trim());
            System.out.println("initTable : "+initTable);
            if (initTable) {
                System.out.println("Entering Initializing");
                for (long i = 0L; i < 100; i=i+1L) {
                    System.out.println("For i = " + i);
                    graphDao.initiateEdgesTable(String.valueOf(i), true);
                    System.out.println("Edge Table Initialized for Id : " + i);
                    graphDao.initiateMetadataTable(String.valueOf(i), true);
                    System.out.println("Metadata Table Initialized for Id : " + i);
                }
            }
            initialized.compareAndSet(false, true);
        }
    }

    @Override
    public void writeData(Collection<Edge> lst) {
        if (CollectionUtils.isNotEmpty(lst)) {
            graphDao.createEdges(lst.toArray(new Edge[lst.size()]));
        }
    }

    @Override
    public void postAction(Collection<Edge> lst) throws PostProcessException {

    }

    public GraphStorage getGraphDao() {
        return graphDao;
    }

    public void setGraphDao(GraphStorage graphDao) {
        this.graphDao = graphDao;
    }
}
