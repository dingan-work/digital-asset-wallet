package wiki.ganhua.wallet.solana.rpc;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址相关的交易
 * @author Ganhua
 */
@NoArgsConstructor
@Data
public class ConfirmedForHash{
    private Long blockTime;
    private String confirmationStatus;
    private Object err;
    private Object memo;
    private String signature;
    private Long slot;
}
