package ai.shizhongying.template.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ai.shizhongying.template.mapper.CrawlSourceMapper;
import ai.shizhongying.template.model.entity.CrawlSource;
import ai.shizhongying.template.service.CrawlSourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrawlSourceServiceImpl extends ServiceImpl<CrawlSourceMapper, CrawlSource> implements CrawlSourceService {

    @Override
    public List<CrawlSource> listEnabled() {
        return this.list(QueryWrapper.create().eq("enabled", 1));
    }
}
