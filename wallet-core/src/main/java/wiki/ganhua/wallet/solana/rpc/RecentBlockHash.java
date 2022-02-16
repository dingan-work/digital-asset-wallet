package wiki.ganhua.wallet.solana.rpc;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 从账本返回最近的区块哈希值
 * @author Ganhua
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class RecentBlockHash extends RpcResultObject{
    private ValueDTO value;

    @NoArgsConstructor
    @Data
    public static class ValueDTO {
        private String blockhash;
        private FeeCalculatorDTO feeCalculator;

        @NoArgsConstructor
        @Data
        public static class FeeCalculatorDTO {
            private Long lamportsPerSignature;
        }
    }

    public String getBlockHash(){
        return getValue().getBlockhash();
    }

    public Long getLamports(){
        return getValue().getFeeCalculator().getLamportsPerSignature();
    }
}
