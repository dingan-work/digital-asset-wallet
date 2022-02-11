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
     * 获取/创建钱包地址
     * @return 钱包私钥与地址 wallet private key and address
     */
    WalletResult createWalletAddress();

    /**
     * 查询地址余额
     * @param address 钱包地址
     * @return 地址的余额 The balance of the address
     */
    BigDecimal getBalance(String address);

    /**
     * 发送交易
     * @param to 接收地址
     * @param amount 发送金额
     * @param from 发送地址
     * @param privateKey 发送人私钥
     * @return 交易hash transaction hash
     */
    String sendTransaction(String to,BigDecimal amount,String from,String privateKey);

    /**
     * 获取交易矿工费 gas费用
     * @return gas费用
     */
    GasFeeResult getMinerFee();


}
