package x.spirit;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import x.spirit.sandglass.DataImporter;
import x.spirit.queryexec.MeshworkQueryExecutor;

/**
 * Hello world!
 *
 */
public class App {

    public static ApplicationContext ctx = null;

    private static Options prepareCLI() {
        // create the Options
        Options options = new Options();
        options.addOption("h", "help", false, "Show this help message.");
        options.addOption("f", "file", true, "If \"f\" is provided, then the data importer will start to run.");
        options.addOption("i", false, "Only for data importer, when \"i\" is passed, then the table will be initialized.");
        options.addOption("b", "buffer", true, "buffer size.");
        options.addOption("q", "query", true, "Specify the vertex id you want to query, the QueryExecutor will start to work");
        options.addOption("n", true, "Number of repetitions for query.");
        options.addOption("c", false, "query the edge count instead of edges");
        options.addOption("r", false, "query the edges from high degree nodes randomly");
        options.addOption("d", "destination", true, "Specify the destination vertex id you want to query. " +
                "If it's empty, then all edges will be selected.");
        options.addOption(null, "bfs", true, "Doing a bfs search with option value as the depth");
        return options;
    }


    public static void main(String[] args) {

        initSpring();


        // create the command line parser
        CommandLineParser parser = new GnuParser();

        try {
            Options options = prepareCLI();
            System.out.println(options.toString());
            CommandLine line = parser.parse(options, args);
            if (line.hasOption('h')){
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("mesh", options);
            } else if (line.hasOption('f')) {
                String fileName = line.getOptionValue('f');
                System.out.println("filename = " + fileName);
                DataImporter<?> importer = (DataImporter<?>) ctx.getBean("vertexDataImporter");
                importer.setStartPos(0L);
                importer.setEndPos(Long.MAX_VALUE);
                if (line.hasOption('b')) {
                    String bfStr = line.getOptionValue('b');
                    if (StringUtils.isEmpty(bfStr)) {
                        importer.setPageCount(100L);
                    } else {
                        importer.setPageCount(Long.valueOf(bfStr));
                    }
                }
                importer.getOtherParams().put("vertexFile", fileName);
                importer.getOtherParams().put("initTable", "false");
                if (line.hasOption('i')) {
                    importer.getOtherParams().put("initTable", "true");
                }
                importer.execute();
            } else if (line.hasOption('q')) {
                MeshworkQueryExecutor queryExecutor = (MeshworkQueryExecutor) ctx.getBean("queryExecutor");
                String sId = line.getOptionValue('q');
                int rounds = 10;
                String n = line.getOptionValue('n');
                if (StringUtils.isNotBlank(n)) {
                    rounds = Integer.valueOf(n.trim());
                }
                long sourceId = Long.valueOf(sId);
                if (line.hasOption('r')) {
                    for (int i = 0; i < rounds; i++) {
                        queryExecutor.randomHiDegree();
                    }
                } else if (line.hasOption('d')) {
                    String desId = line.getOptionValue('d');
                    long destinationId = Long.valueOf(desId);
                    for (int i = 0; i < rounds; i++) {
                        queryExecutor.queryOneEdge(sourceId, destinationId);
                    }
                } else if (line.hasOption("bfs")) {
                    String bfs = line.getOptionValue("bfs");
                    int depth = Integer.valueOf(bfs.trim());
                    queryExecutor.bfsSearch(sourceId, depth);
                } else if (line.hasOption('c')) {
                    for (int i = 0; i < rounds; i++) {
                        queryExecutor.queryEdgeCount(sourceId);
                    }
                } else {
                    for (int i = 0; i < rounds; i++) {
                        queryExecutor.queryAllEdges(sourceId);
                    }
                }
            }

        } catch (ParseException e) {
            System.out.println("Unexpected exception:" + e.getMessage());
        }
    }

    private static void initSpring() {

        ctx = new ClassPathXmlApplicationContext(new String[]{
                "applicationContext.xml"
        });
    }
}
