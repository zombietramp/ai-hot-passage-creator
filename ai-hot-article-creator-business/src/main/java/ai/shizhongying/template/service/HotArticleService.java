package ai.shizhongying.template.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import ai.shizhongying.template.model.dto.hot.HotArticleQueryRequest;
import ai.shizhongying.template.model.entity.HotArticle;
import ai.shizhongying.template.model.vo.HotArticleVO;

public interface HotArticleService extends IService<HotArticle> {

    Page<HotArticleVO> listByPage(HotArticleQueryRequest request);
}
