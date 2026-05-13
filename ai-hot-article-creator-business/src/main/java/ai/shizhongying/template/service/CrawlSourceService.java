package ai.shizhongying.template.service;

import com.mybatisflex.core.service.IService;
import ai.shizhongying.template.model.entity.CrawlSource;

import java.util.List;

public interface CrawlSourceService extends IService<CrawlSource> {

    List<CrawlSource> listEnabled();
}
