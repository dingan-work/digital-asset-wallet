package wiki.ganhua.wallet.arweave.rpc;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ar交易状态
 * @author Ganhua
 * @date 2022/3/9
 */
@NoArgsConstructor
@Data
public class ArTxStatus {
    /**
     * 块高度
     */
    @JSONField(name = "block_height")
    private Long blockHeight;
    /**
     * 块hash
     */
    @JSONField(name = "block_indep_hash")
    private String blockIndepHash;
    /**
     * 该交易确认次数
     */
    @JSONField(name = "number_of_confirmations")
    private Long numberOfConfirmations;
}
