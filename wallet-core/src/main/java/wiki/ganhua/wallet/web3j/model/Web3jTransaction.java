package wiki.ganhua.wallet.web3j.model;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * web3j 交易参数信息 transaction parameter information
 *
 * @author Ganhua
 * @date 2022/2/14
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Web3jTransaction {

    /**
     * 链ID
     */
    private long chainId;

    /**
     * 接收人
     */
    private String toAddress;

    /**
     * 发送人
     */
    private String fromAddress;

    /**
     * 发送人私钥
     */
    private String fromPrivateKey;

    /**
     * 余额
     */
    private BigDecimal amount;

    private BigInteger nonce;

    private BigInteger gasLimit;

    /**
     * 合约地址
     */
    private String contractAddress;

    public Web3jTransaction(long chainId, String toAddress, String fromAddress, String fromPrivateKey, BigDecimal amount, BigInteger nonce,BigInteger gasLimit) {
        this.chainId = chainId;
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.fromPrivateKey = fromPrivateKey;
        this.amount = amount;
        this.nonce = nonce;
        this.gasLimit = gasLimit;
    }

}
