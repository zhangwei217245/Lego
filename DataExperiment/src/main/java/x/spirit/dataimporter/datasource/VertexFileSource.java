package x.spirit.dataimporter.datasource;

import mr.x.commons.utils.ApiLogger;
import mr.x.meshwork.edge.Edge;
import x.spirit.sandglass.DataSource;
import x.spirit.sandglass.PostProcessException;
import x.spirit.sandglass.PreProcessException;
import x.spirit.sandglass.ResultModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangwei on 4/14/15.
 */

public class VertexFileSource extends DataSource<Edge>{

    public static final String NODE_PREFIX="vertex";

    protected String filePath = null;

    BufferedReader bw = null;

    @Override
    public void preAction(long startPosPerThread, long endPosPerThread) throws PreProcessException {
        long currentPos = -1L;
        if (bw == null) {
            filePath = this.otherParams.get("vertexFile");
            File vertexFile = new File(filePath);
            System.out.println("preAction: file = " + vertexFile.getPath());
            try {
                bw = new BufferedReader(new FileReader(vertexFile));
                String line = null;
                System.out.printf("%d %d\n", currentPos, startPosPerThread);
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
                    long sourceId = Long.valueOf(vertexstrs[0].replace(NODE_PREFIX, ""));
                    long destinationId = Long.valueOf(vertexstrs[1].replace(NODE_PREFIX, ""));
                    String ext_info_str = vertexstrs[2];
                    Edge edge = new Edge(sourceId, destinationId, 1);
                    edge.setExt_info_str(ext_info_str);
                    System.out.println("[LOADING]" + edge.toString());
                    ApiLogger.info("[LOADING]" + edge.toString());
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
