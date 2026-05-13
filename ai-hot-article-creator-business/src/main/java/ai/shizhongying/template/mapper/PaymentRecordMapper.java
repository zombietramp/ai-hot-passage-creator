package ai.shizhongying.template.mapper;

import com.mybatisflex.core.BaseMapper;
import ai.shizhongying.template.model.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper
 *
 * @author super.winner
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
