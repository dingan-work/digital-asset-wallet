package wiki.ganhua.wallet.solana.rpc;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * 已确认交易信息
 *
 * @author Ganhua
 */
@NoArgsConstructor
@Data
@Slf4j
public class ConfirmedTransaction {
    private Long blockTime;
    private MetaDTO meta;
    private Integer slot;
    private TransactionDTO transaction;

    private String toAddress;

    /**
     * 发送方
     */
    private String fromAddress;

    /**
     * 交易金额
     */
    private BigDecimal balance;

    /**
     * 交易hash
     */
    private String hashStr;

    @NoArgsConstructor
    @Data
    public static class MetaDTO {
        private Object err;
        private Long fee;
        private List<Long> postBalances;
        private List<Long> preBalances;
    }

    @NoArgsConstructor
    @Data
    public static class TransactionDTO {
        private MessageDTO message;
        @NoArgsConstructor
        @Data
        public static class MessageDTO {
            private List<String> accountKeys;
            private HeaderDTO header;
        }

        @NoArgsConstructor
        @Data
        public static class HeaderDTO{
            private Integer numReadonlySignedAccounts;
            private Integer numReadonlyUnsignedAccounts;
        }
    }

    public Long getBalance(String address){
        // 账户地址i
        int number = 0;
        List<String> accountKeys = getTransaction().getMessage().getAccountKeys();
        for (int i = 0; i < accountKeys.size(); i++) {
            if (accountKeys.get(i).toLowerCase(Locale.ROOT).equals(address.toLowerCase(Locale.ROOT))){
                number = i;
            }
        }
        // 交易发生之前的金额
        Long perBalance = getMeta().getPreBalances().get(number);
        // 交易发生后的余额
        Long postBalance = getMeta().getPostBalances().get(number);
        return NumberUtil.sub(postBalance,perBalance).longValue();
    }

    public boolean initialize(String toAddress) {
        if (toAddress==null){
            // 只读未签名账户
            Integer num = getTransaction().getMessage().getHeader().getNumReadonlyUnsignedAccounts();
            toAddress = getTransaction().getMessage().getAccountKeys().get(num);
        }
        this.toAddress = toAddress;
        // 签名账户下标
        Integer num = getTransaction().getMessage().getHeader().getNumReadonlySignedAccounts();
        String fromAddress = getTransaction().getMessage().getAccountKeys().get(num);
        if (toAddress.equals(fromAddress)){
            return false;
        }
        // 计算是否是签名账户
        // 交易发生之前的金额
        Long perBalance = getMeta().getPreBalances().get(num);
        // 交易发生后的余额
        Long postBalance = getMeta().getPostBalances().get(num);
        // 手续费
        Long fee = getMeta().getFee();
        Long balance = getBalance(toAddress);
        if(balance==0L){
            return false;
        }
        if (NumberUtil.sub(perBalance,fee,balance).compareTo(BigDecimal.valueOf(postBalance))==0){
            this.fromAddress = getTransaction().getMessage().getAccountKeys().get(num);
            this.balance = BigDecimal.valueOf(balance).divide(BigDecimal.TEN.pow(9),8, RoundingMode.DOWN);
            return true;
        }else {
            log.warn(JSONObject.toJSONString(this));
            log.error("初始化失败");
            return false;
        }
    }
}
