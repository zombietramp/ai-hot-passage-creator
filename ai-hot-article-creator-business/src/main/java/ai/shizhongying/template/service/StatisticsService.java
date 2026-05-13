package ai.shizhongying.template.service;

import ai.shizhongying.template.model.vo.StatisticsVO;

/**
 * 统计服务
 *
 * @author super.winner
 */
public interface StatisticsService {

    /**
     * 获取系统统计数据
     *
     * @return 统计数据
     */
    StatisticsVO getStatistics();
}
