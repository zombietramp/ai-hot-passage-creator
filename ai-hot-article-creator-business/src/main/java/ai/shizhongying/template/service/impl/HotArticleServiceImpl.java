package ai.shizhongying.template.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ai.shizhongying.template.mapper.HotArticleMapper;
import ai.shizhongying.template.model.dto.hot.HotArticleQueryRequest;
import ai.shizhongying.template.model.entity.CrawlSource;
import ai.shizhongying.template.model.entity.HotArticle;
import ai.shizhongying.template.model.vo.HotArticleVO;
import ai.shizhongying.template.service.CrawlSourceService;
import ai.shizhongying.template.service.HotArticleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotArticleServiceImpl extends ServiceImpl<HotArticleMapper, HotArticle> implements HotArticleService {

    @Resource
    private CrawlSourceService crawlSourceService;

    @Override
    public Page<HotArticleVO> listByPage(HotArticleQueryRequest request) {
        QueryWrapper wrapper = QueryWrapper.create();

        if (request.getSourceId() != null) {
            wrapper.eq("sourceId", request.getSourceId());
        }

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if ("aiScore".equals(sortField)) {
            wrapper.orderBy("aiScore", "ascend".equals(sortOrder));
        } else {
            wrapper.orderBy("crawlTime", false);
        }

        Page<HotArticle> page = this.page(
                new Page<>(request.getPageNum(), request.getPageSize()),
                wrapper
        );

        Map<Long, String> sourceNameMap = crawlSourceService.list().stream()
                .collect(Collectors.toMap(CrawlSource::getId, CrawlSource::getName, (a, b) -> a));
        sourceNameMap.put(-1L, "AIHOT");

        Page<HotArticleVO> voPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        voPage.setRecords(page.getRecords().stream().map(item -> {
            HotArticleVO vo = HotArticleVO.objToVo(item);
            vo.setSourceName(sourceNameMap.getOrDefault(item.getSourceId(), "未知来源"));
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }
}
