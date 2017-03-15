package mr.x.commons.dao;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 继承 JdbcTemplate 目前只是添加了一个权重，后期可以进行自己的扩展
 * Created by Reilost on 14-3-3.
 */
public class LegoJdbcTemplate extends JdbcTemplate {
    int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
