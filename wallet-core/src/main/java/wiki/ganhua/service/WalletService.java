package wiki.ganhua.service;

import wiki.ganhua.bean.GasFeeResult;
import wiki.ganhua.bean.WalletResult;

import java.math.BigDecimal;

/**
 * 钱包通用操作的Service
 *
 * @author Ganhua
 * @date 2022/2/9
 */
public interface WalletService {

    /**
     * get create wallet address
     * all wallet addresses can be created using the same mnemonic and different secret keys,
     * and the private key can be recovered later through the secret key and mnemonic.
     * @return 钱包私钥与地址 wallet private key and address
     */
    WalletResult createWalletAddress();

    /**
     * get address balance
     * @param address 钱包地址
     * @return 地址的余额 The balance of the address
     */
    BigDecimal getBalance(String address);

    /**
     * send transaction
     * @param to 接收地址
     * @param amount 发送金额
     * @param from 发送地址
     * @param privateKey 发送人私钥
     * @return 交易hash transaction hash
     */
    String sendTransaction(String to,BigDecimal amount,String from,String privateKey);

    /**
     * get transaction miner fee gas cost
     * @return gas费用
     */
    GasFeeResult getMinerFee();


}
