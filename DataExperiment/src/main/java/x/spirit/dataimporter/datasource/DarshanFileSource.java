package x.spirit.dataimporter.datasource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mr.x.commons.utils.ApiLogger;
import x.spirit.graphdbbenchmark.Edge;
import x.spirit.sandglass.DataSource;
import x.spirit.sandglass.PostProcessException;
import x.spirit.sandglass.PreProcessException;
import x.spirit.sandglass.ResultModel;

/**
 *
 * @author zhangwei
 */
public class DarshanFileSource extends DataSource<Edge>{

    protected String filePath = null;

    BufferedReader bw = null;

    @Override
    public void preAction(long startPosPerThread, long endPosPerThread) throws PreProcessException {
        long currentPos = -1L;
        if (bw == null) {
            filePath = this.otherParams.get("vertexFile");
            File vertexFile = new File(filePath);
            try {
                bw = new BufferedReader(new FileReader(vertexFile));
                String line = null;
                while (currentPos < (startPosPerThread - 1L)) {
                    if ((line = bw.readLine()) != null) {
                        currentPos=currentPos+1L;
                    }
                }
            } catch (IOException e) {
                throw new PreProcessException("IOException", e);
            }
        }
    }

    @Override
    public ResultModel<Edge> readData(long startPos, long endPos) {
        List<Edge> edges = new ArrayList<>();
        try {
            String line = null;
            while ((line = bw.readLine()) != null) {
                String[] vertexstrs = line.split("\\s+");
                if (vertexstrs.length >= 3) {
                    Edge edge = new Edge(vertexstrs[0], vertexstrs[1], vertexstrs[2]);
                    ApiLogger.debug("[LOADING]" + edge.toString());
                    edges.add(edge);
                    long pagecount = (endPos - startPos + 1L);
                    if (edges.size() >= (pagecount)) {
                        break;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            ApiLogger.error("Cannot read vertex file: " , e);
        }

        final ResultModel<Edge> rm = new ResultModel<>();
        rm.setResult(edges);

        return rm;
    }

    @Override
    public void postAction(long startPos, long endPos) throws PostProcessException {
        try {
            bw.close();
        } catch (IOException e) {
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
}
