package mr.x.commons.dao;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * JdbcTemplateFactory 单写多读，根据权重进行分配read jdbctemplate
 * Created by Reilost on 14-3-3.
 */
public class JdbcTemplateFactory {
    private int[] readJdbcTemplatesWeights;
    private int readJdbcTemplatesCount = 0;

    private static int lastServer = -1;
    private final static int GCD = 1;
    private static int currentWeight = 0;
    private static int readJdbcTemplatesMaxWeight = 0;
    private LegoJdbcTemplate writeJdbcTemplate;

    private List<LegoJdbcTemplate> readJdbcTemplates;

    public JdbcTemplateFactory(LegoJdbcTemplate writeJdbcTemplate, List<LegoJdbcTemplate> readJdbcTemplates) {
        setWriteJdbcTemplate(writeJdbcTemplate);
        setReadJdbcTemplates(readJdbcTemplates);
    }

    public LegoJdbcTemplate getWriteJdbcTemplate() {
        return writeJdbcTemplate;
    }

    private void setWriteJdbcTemplate(LegoJdbcTemplate writeJdbcTemplate) {
        this.writeJdbcTemplate = writeJdbcTemplate;
    }

    private void setReadJdbcTemplates(List<LegoJdbcTemplate> readJdbcTemplates) {
        this.readJdbcTemplates = Lists.newCopyOnWriteArrayList();
        Collections.sort(readJdbcTemplates, new Comparator<LegoJdbcTemplate>() {
            @Override
            public int compare(LegoJdbcTemplate singleDataSource, LegoJdbcTemplate singleDataSource2) {
                Integer a = singleDataSource.getWeight();
                Integer b = singleDataSource2.getWeight();
                return a.compareTo(b);
            }
        });
        this.readJdbcTemplates.addAll(readJdbcTemplates);
        configRouterSlave();
    }

    private void configRouterSlave() {
        this.readJdbcTemplatesCount = this.readJdbcTemplates.size();
        this.readJdbcTemplatesWeights = wrapWeightsArray(this.readJdbcTemplates);
        JdbcTemplateFactory.readJdbcTemplatesMaxWeight = this.readJdbcTemplatesWeights[this.readJdbcTemplatesWeights.length - 1];
    }

    private int[] wrapWeightsArray(List<LegoJdbcTemplate> jdbcTemplates) {
        int[] weights = new int[jdbcTemplates.size()];
        int i = 0;
        for (LegoJdbcTemplate legoJdbcTemplate : jdbcTemplates) {
            weights[i] = legoJdbcTemplate.getWeight();
            i++;
        }
        Arrays.sort(weights);
        return weights;
    }


    public LegoJdbcTemplate getReadJdbcTemplate()  {
        while (true) {
            lastServer = (lastServer + 1) % readJdbcTemplatesCount;
            if (lastServer == 0) {
                currentWeight = currentWeight - GCD;
                if (currentWeight <= 0) {
                    currentWeight = readJdbcTemplatesMaxWeight;
                    if (currentWeight == 0)
                        return null;
                }
            }
            if (readJdbcTemplatesWeights[lastServer] >= currentWeight) {
                LegoJdbcTemplate legoJdbcTemplate = readJdbcTemplates.get(lastServer);
                return legoJdbcTemplate;
            }
        }

    }
}
