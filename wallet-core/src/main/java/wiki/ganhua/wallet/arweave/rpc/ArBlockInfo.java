package wiki.ganhua.wallet.arweave.rpc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 块信息
 * @author Ganhua
 * @date 2022/3/14
 */
@Getter
@Setter
@ToString
public class ArBlockInfo {

    /**
     * 块时间戳
     */
    private Long timestamp;

}
